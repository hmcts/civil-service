package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.FeeType.APPLICATION;

@ExtendWith(MockitoExtension.class)
class PaymentStatusRetryServiceTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentStatusRetryService service;

    private CaseData caseData;

    @BeforeEach
    void setup() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldUpdatePaymentStatusUsingCaseData() {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(coreCaseDataService.startUpdate("123", CaseEvent.CITIZEN_CLAIM_ISSUE_PAYMENT))
            .thenReturn(startEventResponse);

        service.updatePaymentStatus(FeeType.CLAIMISSUED, "123", caseData);

        verify(coreCaseDataService).submitUpdate(eq("123"), any());
    }

    @Test
    void shouldUpdatePaymentStatusUsingCardPaymentResponse() {
        CaseDetails caseDetails = mock(CaseDetails.class);

        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token("token")
            .eventId("eventId")
            .build();
        when(coreCaseDataService.startUpdate("123", CaseEvent.CREATE_CLAIM_AFTER_PAYMENT))
            .thenReturn(startEventResponse);

        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("success")
            .paymentReference("ref")
            .errorCode("err")
            .errorDescription("desc")
            .build();
        service.updatePaymentStatus(FeeType.CLAIMISSUED, "123", response);

        verify(coreCaseDataService).submitUpdate(eq("123"), any());
    }

    @Test
    void shouldResolvePaymentStatus() {
        PaymentStatus status = service.resolvePaymentStatus("success");
        assertThat(status).isEqualTo(PaymentStatus.SUCCESS);
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
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("failed")
            .paymentReference("ref")
            .errorCode("code")
            .errorDescription("desc")
            .build();

        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
        when(caseData.setClaimIssuedPaymentDetails(any())).thenReturn(caseData);

        CaseData result = service.updateCaseDataWithPaymentDetails(response, caseData, FeeType.CLAIMISSUED);

        assertThat(result).isEqualTo(caseData);
        verify(caseData).setClaimIssuedPaymentDetails(any(PaymentDetails.class));
    }

    @Test
    void shouldLogAndRecoverWhenUpdatePaymentStatusCaseDataFails() {
        PaymentStatusRetryService spyService = spy(service);

        assertThrows(Exception.class, () ->
            spyService.updatePaymentStatus(FeeType.CLAIMISSUED, "123", caseData)
        );

        spyService.recover(new CaseDataUpdateException(), FeeType.CLAIMISSUED, "123", caseData);
    }

    @Test
    void shouldLogAndRecoverWhenUpdatePaymentStatusCardPaymentFails() {
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("success")
            .paymentReference("ref")
            .build();

        when(coreCaseDataService.getCase(123L)).thenThrow(new RuntimeException());

        PaymentStatusRetryService spyService = spy(service);

        assertThrows(CaseDataUpdateException.class, () ->
            spyService.updatePaymentStatus(FeeType.CLAIMISSUED, "123", response)
        );

        CardPaymentStatusResponse recoverResponse = CardPaymentStatusResponse.builder()
            .status("FAILED")
            .paymentReference("REF123")
            .errorCode("ERR001")
            .errorDescription("Payment failed")
            .build();

        spyService.recover(new CaseDataUpdateException(), FeeType.CLAIMISSUED, "123", recoverResponse);
    }

    @Test
    void shouldReturnSpecEventForSpecClaim() {
        CaseData specCaseData = mock(CaseData.class);
        when(specCaseData.isLipvLipOneVOne()).thenReturn(false);
        when(specCaseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        CaseEvent event = service.determineEventFromFeeType(specCaseData, FeeType.CLAIMISSUED);

        assertThat(event).isEqualTo(CREATE_CLAIM_SPEC_AFTER_PAYMENT);
    }

    @Test
    void shouldThrowExceptionForUnsupportedFeeType() {
        CaseData dummyCaseData = mock(CaseData.class);
        when(dummyCaseData.isLipvLipOneVOne()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
            service.determineEventFromFeeType(dummyCaseData, APPLICATION)
        );
    }

    @Test
    void shouldLogRetryAndRecoverForCaseData() {
        CaseDataUpdateException ex = new CaseDataUpdateException();
        CaseData caseData = mock(CaseData.class);

        assertThrows(
            CaseDataUpdateException.class,
            () -> service.updatePaymentStatus(FeeType.CLAIMISSUED, "123", caseData)
        );

        service.recover(ex, FeeType.CLAIMISSUED, "123", caseData);
    }

    @Test
    void recoverShouldLogErrorForCardPaymentResponse() {
        CaseDataUpdateException ex = new CaseDataUpdateException();

        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder()
            .status("FAILED")
            .errorCode("ERR123")
            .errorDescription("Payment failed")
            .paymentReference("PAY123")
            .build();

        service.recover(ex, FeeType.CLAIMISSUED, "12345", response);
    }
}


