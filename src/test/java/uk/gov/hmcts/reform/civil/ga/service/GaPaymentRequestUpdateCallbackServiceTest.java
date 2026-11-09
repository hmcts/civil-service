package uk.gov.hmcts.reform.civil.ga.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.retry.annotation.AnnotationAwareRetryOperationsInterceptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.exceptions.GaNotificationHistoryException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private GaNotificationHistoryService notificationHistoryService;
    @Mock
    Time time;
    @InjectMocks
    GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    @Mock
    StateGeneratorService stateGeneratorService;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setup() {
        logger = (Logger) LoggerFactory.getLogger(GaPaymentRequestUpdateCallbackService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    public void shouldStartAndSubmitEventWithCaseDetails() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails().setStatus(FAILED)))
            .build();
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
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails().setStatus(FAILED)))
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
        verify(judicialNotificationService, times(1)).sendNotificationAndGetSummary(any(), any());
    }

    @Test
    public void shouldApplyPaymentBeforeAttemptingNotificationWhenNotificationServiceIsDown() {

        final GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialOrderMadeWithUncloakApplication(YesOrNo.NO)
            .build().copy().ccdState(APPLICATION_ADD_PAYMENT)
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

        CaseDetails details = buildCaseDetails(caseData);
        when(coreCaseDataService.startGaUpdate(any(), eq(CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID)))
            .thenReturn(startEventResponse(details, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID));
        when(caseDetailsConverter.toGeneralApplicationCaseData(details)).thenReturn(caseData.copy().build());
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(caseData);
        String summary = "Attempted: respondent@example.com : ref : template || Errors: Notify unavailable";
        when(judicialNotificationService.sendNotificationAndGetSummary(caseData, "respondent")).thenReturn(summary);
        ServiceRequestUpdateDto serviceRequestUpdateDto = buildServiceDto(PAID);

        paymentRequestUpdateCallbackService.processServiceRequest(serviceRequestUpdateDto, caseData, false);

        var ordered = inOrder(coreCaseDataService, judicialNotificationService, notificationHistoryService);
        ordered.verify(coreCaseDataService).startGaUpdate(any(), any());
        ordered.verify(coreCaseDataService).submitGaUpdate(any(), any());
        ordered.verify(judicialNotificationService).sendNotificationAndGetSummary(caseData, "respondent");
        ordered.verify(notificationHistoryService).recordNotifications(CASE_ID, summary);
    }

    @Test
    public void shouldNotSendEmailToRespondent_When_ConsentOrder() {
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
        verify(judicialNotificationService, never()).sendNotificationAndGetSummary(any(), any());
    }

    @Test
    public void shouldNotDoProceed_WhenApplicationNotIn_AdditionalPayment_Status() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build();
        caseData = caseData.copy().ccdState(PENDING_CASE_ISSUED).build();

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());
    }

    @Test
    void shouldRecover_whenCaseDataUpdateExceptionThrown() {
        CaseDataUpdateException ex = new CaseDataUpdateException("Test Error", new RuntimeException());
        ServiceRequestUpdateDto dto = buildServiceDto(PAID);

        paymentRequestUpdateCallbackService.recover(ex, dto);

        assertThat(listAppender.list.stream()
                       .anyMatch(event -> event.getFormattedMessage()
                           .contains("Payment status update failed after retries for case 12345. Status: Paid")))
            .isTrue();
        assertThat(listAppender.list.stream()
                       .anyMatch(event -> event.getLevel().equals(Level.ERROR)))
            .isTrue();
    }

    @Test
    void shouldRecover_whenCaseDataUpdateExceptionThrownAndDtoIsNull() {
        CaseDataUpdateException ex = new CaseDataUpdateException("Test Error", new RuntimeException());

        paymentRequestUpdateCallbackService.recover(ex, null);

        assertThat(listAppender.list.stream()
                       .anyMatch(event -> event.getFormattedMessage()
                           .contains("Payment status update failed after retries for case N/A. Status: N/A")))
            .isTrue();
        assertThat(listAppender.list.stream()
                       .anyMatch(event -> event.getLevel().equals(Level.ERROR)))
            .isTrue();
    }

    @Test
    void shouldNotSubmitUpdate_whenPaymentAlreadyApplied() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData.setCcdState(AWAITING_APPLICATION_PAYMENT);
        caseData.setGeneralAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setPaymentDetails(new PaymentDetails()
                                                             .setStatus(SUCCESS)
                                                             .setReference(REFERENCE)));
        CaseDetails freshDetails = buildCaseDetails(caseData);

        when(coreCaseDataService.startGaUpdate(any(), eq(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT)))
            .thenReturn(startEventResponse(freshDetails, INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));
        when(caseDetailsConverter.toGeneralApplicationCaseData(freshDetails)).thenReturn(caseData);

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
    }

    @Test
    void shouldSubmitUpdate_whenFreshPbaIsNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData.setCcdState(AWAITING_APPLICATION_PAYMENT);
        caseData.setGeneralAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setPaymentDetails(new PaymentDetails()
                                                             .setStatus(FAILED)
                                                             .setReference("123")));

        GeneralApplicationCaseData freshData = caseData.copy();
        freshData.setGeneralAppPBADetails(null);
        CaseDetails freshCaseDetails = buildCaseDetails(freshData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(freshCaseDetails)).thenReturn(freshData);
        when(coreCaseDataService.startGaUpdate(any(), eq(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT)))
            .thenReturn(startEventResponse(freshCaseDetails, INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).submitGaUpdate(any(), any());
    }

    @Test
    void shouldHandleInvalidState_returnsNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData.setCcdState(PENDING_CASE_ISSUED);

        GeneralApplicationCaseData result = paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        assertThat(result).isNull();
    }

    @Test
    void shouldProcessHwf() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(Long.valueOf(CASE_ID));
        caseData.setCcdState(AWAITING_APPLICATION_PAYMENT);
        caseData.setGeneralAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber("HWF-REF"));
        caseData.setGeneralAppPBADetails(new GeneralApplicationPbaDetails());

        GeneralApplicationCaseData result = paymentRequestUpdateCallbackService.processHwf(caseData);

        assertThat(result).isNotNull();
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
    }

    private CaseDetails buildCaseDetails(GeneralApplicationCaseData caseData) {
        return CaseDetails.builder()
            .data(objectMapper.convertValue(caseData,
                                           new TypeReference<Map<String, Object>>() {}))
            .id(Long.valueOf(CASE_ID))
            .caseTypeId("GENERALAPPLICATION")
            .build();
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
        caseData = caseData.copy().ccdState(AWAITING_APPLICATION_PAYMENT)
            .generalAppPBADetails(caseData.getGeneralAppPBADetails().copy()
                                      .setPaymentDetails(new PaymentDetails().setStatus(FAILED)))
            .build();
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

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any());

    }

    @Test
    void shouldSendOnceWhenPaymentSubmissionIsRetried() {
        var data = additionalPaymentCaseData();
        var details = buildCaseDetails(data);
        when(coreCaseDataService.startGaUpdate(any(), eq(CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID)))
            .thenReturn(startEventResponse(details, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID));
        when(caseDetailsConverter.toGeneralApplicationCaseData(details)).thenAnswer(invocation ->
            data.copy().generalAppPBADetails(data.getGeneralAppPBADetails().copy()).build());
        when(coreCaseDataService.submitGaUpdate(any(), any()))
            .thenThrow(new RuntimeException("CCD update conflict"))
            .thenReturn(data);
        var dto = buildServiceDto(PAID);
        var retry = new RetryTemplate();
        retry.setBackOffPolicy(new NoBackOffPolicy());

        retry.execute(context -> paymentRequestUpdateCallbackService.processServiceRequest(dto, data, false));

        verify(coreCaseDataService, times(2)).submitGaUpdate(any(), any());
        var ordered = inOrder(coreCaseDataService, judicialNotificationService);
        ordered.verify(coreCaseDataService).startGaUpdate(any(), any());
        ordered.verify(coreCaseDataService).submitGaUpdate(any(), any());
        ordered.verify(coreCaseDataService).startGaUpdate(any(), any());
        ordered.verify(coreCaseDataService).submitGaUpdate(any(), any());
        ordered.verify(judicialNotificationService).sendNotificationAndGetSummary(data, "respondent");
        verify(judicialNotificationService).sendNotificationAndGetSummary(data, "respondent");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRetryOnlyHistoryAndSurfaceExhaustedFailures(boolean failPermanently) {
        var data = additionalPaymentCaseData();
        var details = buildCaseDetails(data);
        when(coreCaseDataService.startGaUpdate(CASE_ID, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID))
            .thenReturn(startEventResponse(details, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID));
        when(caseDetailsConverter.toGeneralApplicationCaseData(details)).thenReturn(data.copy().build());
        when(coreCaseDataService.submitGaUpdate(any(), any())).thenReturn(data);
        when(coreCaseDataService.startGaUpdate(CASE_ID, CaseEvent.RECORD_NOTIFICATIONS))
            .thenReturn(historyStart("history-token-1"), historyStart("history-token-2"), historyStart("history-token-3"));
        String summary = "Attempted: respondent@example.com : ref : template || Errors: send failed";
        when(judicialNotificationService.sendNotificationAndGetSummary(data, "respondent")).thenReturn(summary);
        var failure = new RuntimeException("CCD history update conflict");
        var historySubmission = when(coreCaseDataService.submitGaUpdate(eq(CASE_ID), argThat(content ->
            content != null && CaseEvent.RECORD_NOTIFICATIONS.name().equals(content.getEvent().getId()))));
        if (failPermanently) {
            historySubmission.thenThrow(failure);
        } else {
            historySubmission.thenThrow(failure).thenReturn(data);
        }
        var historyService = retryProxy(new GaNotificationHistoryService(coreCaseDataService));
        var service = retryProxy(new GaPaymentRequestUpdateCallbackService(caseDetailsConverter, coreCaseDataService,
            judicialNotificationService, historyService, objectMapper, time, stateGeneratorService));
        var dto = buildServiceDto(PAID);

        if (failPermanently) {
            assertThatThrownBy(() -> service.processServiceRequest(dto, data, false))
                .isInstanceOf(GaNotificationHistoryException.class).hasCause(failure).hasMessageContaining(CASE_ID);
        } else {
            assertThat(service.processServiceRequest(dto, data, false)).isNotNull();
        }

        int historyAttempts = failPermanently ? 3 : 2;
        verify(coreCaseDataService).startGaUpdate(CASE_ID, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);
        verify(coreCaseDataService, times(historyAttempts)).startGaUpdate(CASE_ID, CaseEvent.RECORD_NOTIFICATIONS);
        verify(judicialNotificationService).sendNotificationAndGetSummary(data, "respondent");
        var submitted = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataService, times(historyAttempts + 1)).submitGaUpdate(eq(CASE_ID), submitted.capture());
        var history = submitted.getAllValues().stream()
            .filter(content -> CaseEvent.RECORD_NOTIFICATIONS.name().equals(content.getEvent().getId())).toList();
        assertThat(history).hasSize(historyAttempts).allSatisfy(content -> {
            assertThat(content.getEvent().getSummary()).isEqualTo("respondent@example.com : ref : template");
            assertThat(content.getEvent().getDescription()).isEqualTo("Errors: send failed");
        });
        assertThat(history.getLast().getEventToken()).isEqualTo("history-token-" + historyAttempts);
    }

    @SuppressWarnings("unchecked")
    private <T> T retryProxy(T target) {
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvisor(new DefaultIntroductionAdvisor(new AnnotationAwareRetryOperationsInterceptor(),
            org.springframework.retry.interceptor.Retryable.class));
        return (T) proxy.getProxy();
    }

    private StartEventResponse historyStart(String token) {
        return StartEventResponse.builder().eventId(CaseEvent.RECORD_NOTIFICATIONS.name()).token(token)
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build();
    }

    @Test
    void shouldKeepAdditionalHwfNotificationsWithoutStartingANestedCcdEvent() {
        var data = additionalPaymentCaseData();
        String summary = "Attempted: respondent@example.com : ref : template || Errors: ";
        when(judicialNotificationService.sendNotificationAndGetSummary(data, "respondent")).thenReturn(summary);

        var result = paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), data, true);

        assertThat(result.getGeneralAppPBADetails().getAdditionalPaymentDetails().getStatus()).isEqualTo(SUCCESS);
        verify(judicialNotificationService).sendNotificationAndGetSummary(data, "respondent");
        assertThat(result.getNotificationSummary()).isEqualTo(summary);
        verify(coreCaseDataService, never()).startGaUpdate(any(), any());
        verifyNoInteractions(notificationHistoryService);
    }

    @Test
    void shouldNotRecordAStaleSummaryForConsentOrderHwfPayments() {
        var data = additionalPaymentCaseData().copy().generalAppConsentOrder(YesOrNo.YES)
            .notificationSummary("Attempted: previous@example.com : ref : template || Errors: ").build();
        var result = paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), data, true);
        assertThat(result.getNotificationSummary()).isNull();
        verifyNoInteractions(judicialNotificationService, notificationHistoryService);
    }

    private GeneralApplicationCaseData additionalPaymentCaseData() {
        return GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(YesOrNo.NO).build()
            .copy().ccdCaseReference(Long.valueOf(CASE_ID)).ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                .setAdditionalPaymentDetails(new PaymentDetails().setStatus(FAILED).setReference(REFERENCE)))
            .build();
    }

    @Test
    public void shouldNotProcessHwf() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData();
        caseData.setCcdState(PENDING_APPLICATION_ISSUED);
        caseData.setCcdCaseReference(1L);
        caseData.setGeneralAppPBADetails(new GeneralApplicationPbaDetails());
        caseData.setGeneralAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber("ref"));

        GeneralApplicationCaseData updatedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        assertThat(updatedCaseData).isNull();
    }

    @Test
    void shouldThrowRetryableException_whenStartGaUpdateFails() {
        final GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .buildPaymentSuccessfulCaseData().copy()
            .ccdState(APPLICATION_ADD_PAYMENT).build();

        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.APPLICATION_ADD_PAYMENT);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenThrow(new RuntimeException("Lock conflict"));

        ServiceRequestUpdateDto dto = buildServiceDto(PAID);
        assertThatThrownBy(() -> paymentRequestUpdateCallbackService.processServiceRequest(dto, caseData, false))
            .isInstanceOf(CaseDataUpdateException.class);
    }

    @Test
    public void shouldNotSubmitEvent_WhenInitialPaymentAlreadyApplied() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails())
            .build();

        // fresh data from CCD already has the payment
        GeneralApplicationCaseData freshData = caseData.copy()
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setPaymentDetails(new PaymentDetails()
                                                             .setStatus(SUCCESS)
                                                             .setReference(REFERENCE)))
            .build();
        CaseDetails freshDetails = buildCaseDetails(freshData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(freshDetails)).thenReturn(freshData);
        when(coreCaseDataService.startGaUpdate(any(), eq(INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT)))
            .thenReturn(startEventResponse(freshDetails, INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT));

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
    }

    @Test
    public void shouldNotSubmitEvent_WhenAdditionalPaymentAlreadyApplied() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
        caseData = caseData.copy()
            .ccdState(APPLICATION_ADD_PAYMENT)
            .generalAppPBADetails(new GeneralApplicationPbaDetails())
            .build();

        // fresh data from CCD already has the payment
        GeneralApplicationCaseData freshData = caseData.copy()
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                       .setStatus(SUCCESS)
                                                                       .setReference(REFERENCE)))
            .build();
        CaseDetails freshDetails = buildCaseDetails(freshData);

        when(caseDetailsConverter.toGeneralApplicationCaseData(freshDetails)).thenReturn(freshData);
        when(coreCaseDataService.startGaUpdate(any(), eq(CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID)))
            .thenReturn(startEventResponse(freshDetails, CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID));

        paymentRequestUpdateCallbackService.processServiceRequest(buildServiceDto(PAID), caseData, false);

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
        verify(coreCaseDataService, never()).submitGaUpdate(any(), any());
        verify(judicialNotificationService, never()).sendNotificationAndGetSummary(any(), any());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenIllegalArgumentExceptionIsThrown() {
        final GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .buildPaymentSuccessfulCaseData().copy()
            .ccdState(APPLICATION_ADD_PAYMENT).build();

        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any())).thenReturn(CaseState.APPLICATION_ADD_PAYMENT);
        when(coreCaseDataService.startGaUpdate(any(), any())).thenThrow(new IllegalArgumentException("Invalid argument"));

        ServiceRequestUpdateDto dto = buildServiceDto(PAID);
        assertThatThrownBy(() -> paymentRequestUpdateCallbackService.processServiceRequest(dto, caseData, false))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid argument");

        verify(coreCaseDataService, times(1)).startGaUpdate(any(), any());
    }
}
