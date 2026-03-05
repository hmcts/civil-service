package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
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

@ExtendWith(MockitoExtension.class)
class GaPaymentRequestUpdateCallbackServiceTest {

    private static final String PAID = "Paid";
    private static final String CASE_ID = "12345";
    public static final String REFERENCE = "123445";
    public static final String ACCOUNT_NUMBER = "123445555";
    public static final String TOKEN = "1234";
    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();
    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private GeneralApplicationCreationNotificationService gaNotificationService;

    @Mock
    private JudicialNotificationService judicialNotificationService;
    @Mock
    Time time;
    @InjectMocks
    GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    @Mock
    StateGeneratorService stateGeneratorService;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Test
    public void shouldStartAndSubmitEventWithCaseDetails() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));

        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());

    }

    @Test
    public void shouldProceed_WhenGeneralAppParentCaseLink() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppParentCaseLink(null).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
    }

    @Test
    public void shouldProceed_WhenAdditionalPaymentExist_WithPaymentFail() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setStatus(FAILED)
                                                                    .setCustomerReference(null)
                                                                    .setReference(REFERENCE)
                                                                    .setErrorCode(null)
                                                                    .setErrorMessage(null)
                                                                    )
                                      )
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
        verify(judicialNotificationService, times(1)).sendNotification(any(), any());
    }

    @Test
    public void shouldNotProceed_WhenAdditionalPaymentExist_WithPaymentFail_AndNotificationServiceIsDown() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setStatus(FAILED)
                                                                    .setCustomerReference(null)
                                                                    .setReference(REFERENCE)
                                                                    .setErrorCode(null)
                                                                    .setErrorMessage(null)
                                                                    )
                                      )
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        doThrow(buildNotificationException())
            .when(judicialNotificationService)
            .sendNotification(caseData, "respondent");

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    @Test
    public void shouldNotSendEmailToRespondent_When_ConsentOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setStatus(SUCCESS)
                                                                    .setCustomerReference(null)
                                                                    .setReference(REFERENCE)
                                                                    .setErrorCode(null)
                                                                    .setErrorMessage(null)
                                                                    )
                                      )
            .generalAppConsentOrder(YesOrNo.NO)
            .build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,
                               END_JUDGE_BUSINESS_PROCESS_GASPEC));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
        verify(judicialNotificationService, never()).sendNotification(any(), any());
    }

    @Test
    public void shouldNotDoProceed_WhenApplicationNotIn_AdditionalPayment_Status() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(PENDING_CASE_ISSUED).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

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
        return new ServiceRequestUpdateDto()
            .setCcdCaseNumber(CASE_ID)
            .setServiceRequestStatus(status)
            .setPayment(PaymentDto.builder()
                .amount(new BigDecimal(167))
                .paymentReference(REFERENCE)
                .caseReference(REFERENCE)
                .accountNumber(ACCOUNT_NUMBER)
                .build());
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

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy().build();
        caseData = caseData.copy().ccdState(AWAITING_APPLICATION_PAYMENT).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails))
            .thenReturn(caseData);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenReturn(
            startEventResponse(caseDetails,

                               INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);
        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);
        CaseState c = caseData.getCcdState();
        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
    }

    @Test
    public void shouldLogErrorWhenCcdStateIsNotAwaitingPayment() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy().build();
        caseData = caseData.copy().ccdState(AWAITING_RESPONDENT_RESPONSE).build();
        CaseDetails caseDetails = buildCaseDetails(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());

    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }

    @Test
    public void shouldProcessHwf() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1L)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                        .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.ONE)))
                .generalAppHelpWithFees(new HelpWithFees()
                        .setHelpWithFeesReferenceNumber("ref"))
                .build();
        GeneralApplicationCaseData updatedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        assertThat(updatedCaseData).isNotNull();
    }

    @Test
    public void shouldNotProcessHwf() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(PENDING_APPLICATION_ISSUED)
                .ccdCaseReference(1L)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                        .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.ONE)))
                .generalAppHelpWithFees(new HelpWithFees()
                        .setHelpWithFeesReferenceNumber("ref"))
                .build();
        GeneralApplicationCaseData updatedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        assertThat(updatedCaseData).isNull();
    }
}
