package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_JUDGE_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@SpringBootTest(classes = {
    GaPaymentRequestUpdateCallbackService.class,
    JacksonAutoConfiguration.class,

})
class GaPaymentRequestUpdateCallbackServiceTest {

    private static final String PAID = "Paid";
    private static final String NOT_PAID = "NotPaid";
    private static final String CASE_ID = "12345";
    public static final String REFERENCE = "123445";
    public static final String ACCOUNT_NUMBER = "123445555";
    public static final String TOKEN = "1234";
    @Mock
    ObjectMapper objectMapper;
    @MockBean
    private GaCoreCaseDataService coreCaseDataService;

    @MockBean
    private GeneralApplicationCreationNotificationService gaNotificationService;

    @MockBean
    private JudicialNotificationService judicialNotificationService;
    @MockBean
    Time time;
    @Autowired
    GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    @MockBean
    StateGeneratorService stateGeneratorService;

    @MockBean
    CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    public void setup() {
        when(time.now()).thenReturn(LocalDateTime
                                        .of(2020, 1, 1, 12, 0, 0));
    }

    @Test
    public void shouldStartAndSubmitEventWithCaseDetails() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));

        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());

    }

    @Test
    public void shouldProceed_WhenGeneralAppParentCaseLink() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppParentCaseLink(null).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
    }

    @Test
    public void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFail() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .status(FAILED)
                                                                    .customerReference(null)
                                                                    .reference(REFERENCE)
                                                                    .errorCode(null)
                                                                    .errorMessage(null)
                                                                    .build())
                                      .build())
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
        verify(judicialNotificationService, times(1)).sendNotification(any(), any());
    }

    @Test
    public void shouldNotProceed_WhenAdditionalPaymentExist_WithPaymentFail_AndNotificationServiceIsDown() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .status(FAILED)
                                                                    .customerReference(null)
                                                                    .reference(REFERENCE)
                                                                    .errorCode(null)
                                                                    .errorMessage(null)
                                                                    .build())
                                      .build())
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        doThrow(buildNotificationException())
            .when(judicialNotificationService)
            .sendNotification(caseData, "respondent");

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    @Test
    public void shouldNotSendEmailToRespondent_When_ConsentOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .status(SUCCESS)
                                                                    .customerReference(null)
                                                                    .reference(REFERENCE)
                                                                    .errorCode(null)
                                                                    .errorMessage(null)
                                                                    .build())
                                      .build())
            .generalAppConsentOrder(YesOrNo.NO)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
        verify(judicialNotificationService, never()).sendNotification(any(), any());
    }

    @Test
    public void shouldNotProceed_WhenPaymentFailed() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(APPLICATION_ADD_PAYMENT).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(NOT_PAID));

        verify(coreCaseDataService, never()).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    @Test
    public void shouldNotDoProceed_WhenApplicationNotIn_AdditionalPayment_Status() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.toBuilder().ccdState(PENDING_CASE_ISSUED).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    private CaseDetails buildCaseDetails(GeneralApplicationCaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData,
                    new TypeReference<Map<String, Object>>() {})).id(Long.valueOf(CASE_ID)).build();
    }

    private ServiceRequestUpdateDto buildServiceDto(String status) {
        return ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(CASE_ID)
            .serviceRequestStatus(status)
            .payment(PaymentDto.builder()
                         .amount(new BigDecimal(167))
                         .paymentReference(REFERENCE)
                         .caseReference(REFERENCE)
                         .accountNumber(ACCOUNT_NUMBER)
                         .build())
            .build();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails,
                                                  CaseEvent caseEvent) {
        return StartEventResponse.builder()
            .token(TOKEN)
            .eventId(caseEvent.name())
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void shouldProceedAfterInitialPaymentIsSuccess() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder().build();
        caseData = caseData.toBuilder().ccdState(AWAITING_APPLICATION_PAYMENT).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);
        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,

                               INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);
        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));
        CaseState c = caseData.getCcdState();
        verify(coreCaseDataService, times(1)).getCase(Long.valueOf(CASE_ID));
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
    }

    @Test
    public void shouldLogErrorWhenCcdStateIsNotAwaitingPayment() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder().build();
        caseData = caseData.toBuilder().ccdState(AWAITING_RESPONDENT_RESPONSE).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.getCase(Long.valueOf(CASE_ID))).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);

        paymentRequestUpdateCallbackService.processCallback(buildServiceDto(PAID));

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());

    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }

    @Test
    public void shouldProcessHwf() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1L)
                .generalAppPBADetails(GAPbaDetails.builder()
                        .fee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build()).build())
                .generalAppHelpWithFees(HelpWithFees.builder()
                        .helpWithFeesReferenceNumber("ref").build())
                .build();
        GeneralApplicationCaseData updatedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        assertThat(updatedCaseData).isNotNull();
    }

    @Test
    public void shouldNotProcessHwf() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(PENDING_APPLICATION_ISSUED)
                .ccdCaseReference(1L)
                .generalAppPBADetails(GAPbaDetails.builder()
                        .fee(Fee.builder().calculatedAmountInPence(BigDecimal.ONE).build()).build())
                .generalAppHelpWithFees(HelpWithFees.builder()
                        .helpWithFeesReferenceNumber("ref").build())
                .build();
        GeneralApplicationCaseData updatedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        assertThat(updatedCaseData).isNull();
    }
}
