package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.FeeType.APPLICATION;

@ExtendWith(MockitoExtension.class)
class PaymentStatusRetryServiceTest {

    private static final Long CASE_ID = 123L;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentStatusRetryService service;

    private CaseData caseData;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setup() {
        caseData = mock(CaseData.class);
        logger = (Logger) LoggerFactory.getLogger(PaymentStatusRetryService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldUpdatePaymentStatusUsingCaseData() {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(coreCaseDataService.startUpdate(CASE_ID.toString(), CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT))
            .thenReturn(startEventResponse);

        service.updatePaymentStatus(FeeType.CLAIMISSUED, CASE_ID.toString(), caseData);

        verify(coreCaseDataService).submitUpdate(eq(CASE_ID.toString()), any());
    }

    @Test
    void shouldUpdatePaymentStatusUsingCardPaymentResponse() {
        CaseDetails caseDetails = mock(CaseDetails.class);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        when(caseData.isLipvLROneVOne()).thenReturn(false);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();
        when(coreCaseDataService.startUpdate(CASE_ID.toString(), CaseEvent.CREATE_CLAIM_AFTER_PAYMENT))
            .thenReturn(startEventResponse);

        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setStatus("success")
            .setPaymentReference("ref")
            .setErrorCode("err")
            .setErrorDescription("desc");
        service.updatePaymentStatus(FeeType.CLAIMISSUED, CASE_ID.toString(), response);

        verify(coreCaseDataService).submitUpdate(eq(CASE_ID.toString()), any());
    }

    @Test
    void shouldApplyPaymentDetailsForHearingFee() {
        PaymentDetails paymentDetails = new PaymentDetails();

        when(caseData.setHearingFeePaymentDetails(paymentDetails)).thenReturn(caseData);

        CaseData result = service.applyPaymentDetails(caseData, FeeType.HEARING, paymentDetails);

        assertThat(result).isEqualTo(caseData);
        verify(caseData).setHearingFeePaymentDetails(paymentDetails);
    }

    @Test
    void shouldUpdateCaseDataWithPaymentDetails() {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setStatus("failed")
            .setPaymentReference("ref")
            .setErrorCode("code")
            .setErrorDescription("desc");

        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
        when(caseData.setClaimIssuedPaymentDetails(any())).thenReturn(caseData);

        CaseData result = service.updateCaseDataWithPaymentDetails(response, caseData, FeeType.CLAIMISSUED);

        assertThat(result).isEqualTo(caseData);
        verify(caseData).setClaimIssuedPaymentDetails(any(PaymentDetails.class));
    }

    @Test
    void shouldLogAndRecoverWhenUpdatePaymentStatusCaseDataFails() {
        CaseDataUpdateException ex = new CaseDataUpdateException("test error", new RuntimeException());
        service.recover(ex, FeeType.CLAIMISSUED, CASE_ID.toString(), caseData);

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.getFirst().getFormattedMessage())
            .contains("Payment status update (CaseData) failed after retries for case 123 and fee type CLAIMISSUED");
    }

    @Test
    void shouldLogAndRecoverWhenUpdatePaymentStatusCardPaymentFails() {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setStatus("success")
            .setPaymentReference("ref");

        when(coreCaseDataService.getCase(CASE_ID)).thenThrow(new RuntimeException());

        PaymentStatusRetryService spyService = spy(service);

        assertThrows(CaseDataUpdateException.class, () ->
            spyService.updatePaymentStatus(FeeType.CLAIMISSUED, CASE_ID.toString(), response)
        );

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.getFirst().getLevel()).isEqualTo(Level.INFO);
        assertThat(listAppender.list.getFirst().getFormattedMessage())
            .contains("Retrying payment status update for case 123");

        CardPaymentStatusResponse recoverResponse = new CardPaymentStatusResponse()
            .setStatus("FAILED")
            .setPaymentReference("REF123")
            .setErrorCode("ERR001")
            .setErrorDescription("Payment failed");

        CaseDataUpdateException ex = new CaseDataUpdateException("test error", new RuntimeException());
        service.recover(ex, FeeType.CLAIMISSUED, CASE_ID.toString(), recoverResponse);

        assertThat(listAppender.list).hasSize(2);
        assertThat(listAppender.list.get(1).getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.get(1).getFormattedMessage())
            .contains("Payment status update failed after retries for case 123 and fee type CLAIMISSUED. Status: FAILED, ErrorCode: ERR001");
    }

    @Test
    public void shouldThrowExceptionIfInvalidPaymentStatus() {

        CaseData caseData = new CaseDataBuilder().build();
        CaseDetails caseDetails = mock(CaseDetails.class);

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setPaymentReference("1234")
            .setStatus("Invalid");

        assertThatThrownBy(() -> service.updatePaymentStatus(FeeType.CLAIMISSUED, CASE_ID.toString(), response))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid payment status: Invalid");

        verify(coreCaseDataService, times(1)).getCase(CASE_ID);
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldReturnSpecEventForSpecClaim() {
        CaseData specCaseData = mock(CaseData.class);
        when(specCaseData.isLipvLipOneVOne()).thenReturn(false);
        when(specCaseData.isLipvLROneVOne()).thenReturn(false);
        when(specCaseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        CaseEvent event = service.determineEventFromFeeType(specCaseData, FeeType.CLAIMISSUED);

        assertThat(event).isEqualTo(CREATE_CLAIM_SPEC_AFTER_PAYMENT);
    }

    @Test
    void shouldReturnCitizenHearingFeePaymentEventForLipVsLRCase() {
        CaseData lipVsLRCaseData = mock(CaseData.class);
        when(lipVsLRCaseData.isLipvLipOneVOne()).thenReturn(false);
        when(lipVsLRCaseData.isLipvLROneVOne()).thenReturn(true);

        CaseEvent event = service.determineEventFromFeeType(lipVsLRCaseData, FeeType.HEARING);

        assertThat(event).isEqualTo(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT);
    }

    @Test
    void shouldReturnCitizenHearingFeePaymentEventForLipVsLipCase() {
        CaseData lipVsLipCaseData = mock(CaseData.class);
        when(lipVsLipCaseData.isLipvLipOneVOne()).thenReturn(true);

        CaseEvent event = service.determineEventFromFeeType(lipVsLipCaseData, FeeType.HEARING);

        assertThat(event).isEqualTo(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT);
    }

    @Test
    void shouldThrowExceptionForUnsupportedFeeType() {
        CaseData dummyCaseData = mock(CaseData.class);
        when(dummyCaseData.isLipvLipOneVOne()).thenReturn(false);
        when(dummyCaseData.isLipvLROneVOne()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
            service.determineEventFromFeeType(dummyCaseData, APPLICATION)
        );
    }

}

