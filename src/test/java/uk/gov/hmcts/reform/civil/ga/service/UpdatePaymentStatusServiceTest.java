package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusServiceTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private GaCoreCaseDataService gaCoreCaseDataService;
    @InjectMocks
    private UpdatePaymentStatusService updatePaymentStatusService;

    private static final String BUSINESS_PROCESS = "JUDICIAL_REFERRAL";
    private static final Long CASE_ID = 1594901956117591L;
    private static final String TOKEN = "1234";

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setup() {
        logger = (Logger) LoggerFactory.getLogger(UpdatePaymentStatusService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    public void shouldLogAndRecoverWhenUpdatePaymentStatusFails() {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse()
            .setStatus("FAILED")
            .setErrorCode("ERR123")
            .setPaymentReference("PAY123");

        CaseDataUpdateException ex = new CaseDataUpdateException("Test Error", new RuntimeException());

        updatePaymentStatusService.recover(ex, String.valueOf(CASE_ID), response);

        assertThat(listAppender.list).hasSize(1);
        assertThat(listAppender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(listAppender.list.getFirst().getFormattedMessage())
            .contains("GA Payment status update failed after retries for case 1594901956117591. Status: FAILED, ErrorCode: ERR123");
    }

    @Test
    public void shouldSubmitCitizenApplicationFeePaymentEvent() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(BUSINESS_PROCESS))
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setPaymentDetails(new PaymentDetails()
                    .setCustomerReference("RC-1604-0739-2145-4711")
                    ))
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(gaCoreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(gaCoreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT
        ));
        when(gaCoreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(gaCoreCaseDataService, times(1)).getCase(CASE_ID);
        verify(gaCoreCaseDataService).startUpdate(String.valueOf(CASE_ID), INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT);
        verify(gaCoreCaseDataService).submitUpdate(any(), any());

    }

    @Test
    public void shouldSubmitCitizenAdditionalFeePaymentEvent() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(CASE_PROGRESSION)
            .businessProcess(new BusinessProcess()
                                 .setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(BUSINESS_PROCESS))
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setAdditionalPaymentDetails(new PaymentDetails()
                                                                            .setCustomerReference("RC-1604-0739-2145-4711")
                                                                            )
                                      .setAdditionalPaymentServiceRef("2023-1701090705600"))
            .applicationFeeAmountInPence(new BigDecimal("10000"))
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(gaCoreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(gaCoreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse(
            caseDetails,
            MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID
        ));
        when(gaCoreCaseDataService.submitUpdate(any(), any())).thenReturn(caseData);

        updatePaymentStatusService.updatePaymentStatus(String.valueOf(CASE_ID), getCardPaymentStatusResponse());

        verify(gaCoreCaseDataService, times(1)).getCase(CASE_ID);
        verify(gaCoreCaseDataService).startUpdate(String.valueOf(CASE_ID), MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);
        verify(gaCoreCaseDataService).submitUpdate(any(), any());

    }

    @Test
    public void shouldThrowExceptionIfInvalidPaymentStatus() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(gaCoreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        CardPaymentStatusResponse cardPaymentStatusResponse = new CardPaymentStatusResponse()
            .setPaymentReference("1234")
            .setStatus("Invalid");

        assertThatThrownBy(() -> updatePaymentStatusService.updatePaymentStatus(String.valueOf(CASE_ID), cardPaymentStatusResponse))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid payment status: Invalid");

        verify(gaCoreCaseDataService, times(1)).getCase(CASE_ID);
        verifyNoMoreInteractions(gaCoreCaseDataService);
    }

    private CaseDetails buildCaseDetails(GeneralApplicationCaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(
                caseData,
                new TypeReference<>() {
                }
            ))
            .id(CASE_ID).build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails, CaseEvent event) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(event.name())
            .caseDetails(caseDetails)
            .build();
    }

    private CardPaymentStatusResponse getCardPaymentStatusResponse() {
        return new CardPaymentStatusResponse()
            .setPaymentReference("1234")
            .setStatus(PaymentStatus.SUCCESS.name());
    }
}
