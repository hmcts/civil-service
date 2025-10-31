
package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.FeignException;
import feign.Request;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIMANT_TASK_LIST_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_RESPONDENT_TASK_LIST_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    GAJudgeRevisitTaskHandler.class
})
class GAJudgeRevisitTaskHandlerTest {

    @MockBean
    private ExternalTask externalTask;

    @MockBean
    private ExternalTaskService externalTaskService;

    @MockBean
    private CaseStateSearchService caseStateSearchService;

    @MockBean
    private GaCoreCaseDataService coreCaseDataService;
    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;
    @MockBean
    private GaForLipService gaForLipService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private GAJudgeRevisitTaskHandler gaJudgeRevisitTaskHandler;

    private CaseDetails caseDetailsDirectionOrder;
    private CaseDetails caseDetailsWrittenRepresentationS;
    private CaseDetails caseDetailsWrittenRepresentationC;
    private CaseDetails caseDetailRequestForInformation;

    public static final String EXCEPTION_MESSAGE = "Unprocessable Entity found";
    public static final String UNEXPECTED_RESPONSE_BODY = "Case data validation failed";

    Logger logger = (Logger) LoggerFactory.getLogger(GAJudgeRevisitTaskHandler.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeEach
    void init() {
        caseDetailsDirectionOrder = CaseDetails.builder().id(1L).data(
            Map.of("judicialDecisionMakeOrder", GAJudicialMakeAnOrder.builder()
                .directionsText("Test Direction")
                .reasonForDecisionText("Test Reason")
                .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                .directionsResponseByDate(LocalDate.now())
                .build())).state(AWAITING_DIRECTIONS_ORDER_DOCS.toString()).build();
        caseDetailsWrittenRepresentationC = CaseDetails.builder().id(2L).data(
            Map.of("judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                .writtenOption(CONCURRENT_REPRESENTATIONS)
                .writtenConcurrentRepresentationsBy(LocalDate.now())
                .build())).state(AWAITING_WRITTEN_REPRESENTATIONS.toString()).build();
        caseDetailsWrittenRepresentationS = CaseDetails.builder().id(3L).data(
            Map.of("judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                .writtenOption(SEQUENTIAL_REPRESENTATIONS)
                .sequentialApplicantMustRespondWithin(LocalDate.now())
                .writtenSequentailRepresentationsBy(LocalDate.now())
                .build())).state(AWAITING_WRITTEN_REPRESENTATIONS.toString()).build();
        caseDetailRequestForInformation = CaseDetails.builder().id(4L).data(
            Map.of("judicialDecision", GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build(),
                   "judicialDecisionRequestMoreInfo", GAJudicialRequestMoreInfo.builder()
                       .requestMoreInfoOption(REQUEST_MORE_INFORMATION)
                       .judgeRequestMoreInfoByDate(LocalDate.now())
                       .judgeRequestMoreInfoText("test").build()
            )).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");
    }

    @Test
    void throwException_whenUnprocessableEntityIsFound() {
        listAppender.start();
        logger.addAppender(listAppender);
        doThrow(buildFeignExceptionWithUnprocessableEntity()).when(coreCaseDataService)
            .triggerEvent(any(), any());
        CaseDetails caseDetailRequestForInformation = CaseDetails.builder().id(1L).data(
            Map.of("generalAppConsentOrder", "Yes")).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();
        gaJudgeRevisitTaskHandler.fireEventForStateChange(caseDetailRequestForInformation);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error in GAJudgeRevisitTaskHandler::fireEventForStateChange: "
                         + "feign.FeignException$FeignClientException: Unprocessable Entity found",
                     logsList.get(1).getMessage());
        assertEquals(Level.ERROR, logsList.get(1).getLevel());
    }

    @Test
    void throwException_whenUnprocessableEntity() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails caseDetailRequestForInformation = caseDetailsDirectionOrder.toBuilder().data(
            Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformation));

        gaJudgeRevisitTaskHandler.getRequestForInformationCaseReadyToJudgeRevisit();

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("GAJudgeRevisitTaskHandler failed: java.lang.IllegalArgumentException: "
                         + "Cannot deserialize value of type `uk.gov.hmcts.reform.civil.enums.YesOrNo` "
                         + "from String \"maybe\": not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] "
                         + "(through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(0).getMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        listAppender.stop();
    }

    @Test
    void throwException_whenUnprocessableEntity_writtenRep() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails caseDetailsWrittenRepresentation = caseDetailsWrittenRepresentationC.toBuilder().data(
            Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_WRITTEN_REPRESENTATIONS.toString())
            .build();

        gaJudgeRevisitTaskHandler.filterForClaimantWrittenRepExpired(Set.of(caseDetailsWrittenRepresentation));

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error GAJudgeRevisitTaskHandler::getWrittenRepCaseReadyToJudgeRevisit : "
                         + "java.lang.IllegalArgumentException: Cannot deserialize value of type "
                         + "`uk.gov.hmcts.reform.civil.enums.YesOrNo` from String \"maybe\": "
                         + "not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(0).getMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        listAppender.stop();
    }

    @Test
    void shouldCatchException_andProceedFurther_withValidData_writtenRep() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails caseDetailsWrittenRepresentation = CaseDetails.builder().data(
            Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_WRITTEN_REPRESENTATIONS.toString())
            .build();

        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentation, caseDetailsWrittenRepresentationC));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error GAJudgeRevisitTaskHandler::getWrittenRepCaseReadyToJudgeRevisit : "
                         + "java.lang.IllegalArgumentException: Cannot deserialize value of type "
                         + "`uk.gov.hmcts.reform.civil.enums.YesOrNo` from String \"maybe\": "
                         + "not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(0).getMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService).triggerEvent(2L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

        listAppender.stop();
    }

    @Test
    void throwException_whenUnprocessableEntity_directionOrder() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails caseDetailsDirectionOrderCase = caseDetailsDirectionOrder.toBuilder().data(
                Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_DIRECTIONS_ORDER_DOCS.toString())
            .build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS))
            .thenReturn(Set.of(caseDetailsDirectionOrderCase));

        gaJudgeRevisitTaskHandler.getDirectionOrderCaseReadyToJudgeRevisit();

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error GAJudgeRevisitTaskHandler::getDirectionOrderCaseReadyToJudgeRevisit : "
                         + "java.lang.IllegalArgumentException: Cannot deserialize value of type "
                         + "`uk.gov.hmcts.reform.civil.enums.YesOrNo` from String \"maybe\": "
                         + "not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(0).getMessage());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        listAppender.stop();
    }

    @Test
    void shouldCatchException_andProceedFurther_withValidData_directionOrder() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails caseDetailsDirectionOrderCase = CaseDetails.builder().data(
                Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_DIRECTIONS_ORDER_DOCS.toString())
            .build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS))
            .thenReturn(Set.of(caseDetailsDirectionOrderCase, caseDetailsDirectionOrder));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error GAJudgeRevisitTaskHandler::getDirectionOrderCaseReadyToJudgeRevisit : "
                         + "java.lang.IllegalArgumentException: Cannot deserialize value of type "
                         + "`uk.gov.hmcts.reform.civil.enums.YesOrNo` from String \"maybe\": "
                         + "not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(2).getMessage());
        assertEquals(Level.ERROR, logsList.get(2).getLevel());

        verify(caseStateSearchService).getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);
        verify(coreCaseDataService).triggerEvent(1L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
        listAppender.stop();
    }

    @Test
    void shouldCatchException_andProceedFurther_withValidData() {
        listAppender.start();
        logger.addAppender(listAppender);
        CaseDetails requestForInformation = caseDetailsDirectionOrder.toBuilder().data(
            Map.of("generalAppConsentOrder", "maybe")).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformation, requestForInformation));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("GAJudgeRevisitTaskHandler failed: java.lang.IllegalArgumentException: "
                         + "Cannot deserialize value of type `uk.gov.hmcts.reform.civil.enums.YesOrNo` "
                         + "from String \"maybe\": not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] "
                         + "(through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData$GeneralApplicationCaseDataBuilderImpl"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.get(3).getMessage());
        assertEquals(Level.ERROR, logsList.get(3).getLevel());

        verify(caseStateSearchService).getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);
        verify(coreCaseDataService, times(1)).triggerEvent(any(), any());
        verify(coreCaseDataService).triggerEvent(4L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
        listAppender.stop();
    }

    private FeignException buildFeignExceptionWithUnprocessableEntity() {
        return buildFeignException(422, UNEXPECTED_RESPONSE_BODY.getBytes(UTF_8));
    }

    private FeignException.FeignClientException buildFeignException(int status, byte[] body) {
        return new FeignException.FeignClientException(
            status,
            EXCEPTION_MESSAGE,
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            body,
            Map.of()
        );
    }

    @Test
    void shouldNotSendMessageAndTriggerEvent_whenZeroCasesFound() {
        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS)).thenReturn(Set.of());

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenDirectionOrderDateIsToday() {
        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS))
            .thenReturn(Set.of(caseDetailsDirectionOrder));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);
        verify(coreCaseDataService).triggerEvent(1L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenDirectionOrderDateIsPast() {

        CaseDetails caseDetailsDirectionOrderWithPastDate = caseDetailsDirectionOrder.toBuilder().data(
            Map.of("judicialDecisionMakeOrder", GAJudicialMakeAnOrder.builder()
                .directionsText("Test Direction")
                .reasonForDecisionText("Test Reason")
                .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                .directionsResponseByDate(LocalDate.now().minusDays(2))
                .build())).state(AWAITING_DIRECTIONS_ORDER_DOCS.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS))
            .thenReturn(Set.of(caseDetailsDirectionOrderWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);
        verify(coreCaseDataService).triggerEvent(1L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotEmitBusinessProcessEvent_whenDirectionOrderDateIsFuture() {

        CaseDetails caseDetailsDirectionOrderWithPastDate = caseDetailsDirectionOrder.toBuilder().data(
            Map.of("judicialDecisionMakeOrder", GAJudicialMakeAnOrder.builder()
                .directionsText("Test Direction")
                .reasonForDecisionText("Test Reason")
                .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                .directionsResponseByDate(LocalDate.now().plusDays(2))
                .build())).state(AWAITING_DIRECTIONS_ORDER_DOCS.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS))
            .thenReturn(Set.of(caseDetailsDirectionOrderWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);
        verify(coreCaseDataService, times(0)).triggerEvent(1L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenWrittenRepConcurrentDateIsToday() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationC));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService).triggerEvent(2L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenWrittenRepConcurrentDateIsPast() {

        CaseDetails caseDetailsWrittenRepresentationConWithPastDate = caseDetailsWrittenRepresentationC.toBuilder().data(
            Map.of("judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                .writtenOption(CONCURRENT_REPRESENTATIONS)
                .writtenConcurrentRepresentationsBy(LocalDate.now().minusDays(1))
                .build())).state(AWAITING_WRITTEN_REPRESENTATIONS.toString()).build();

        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationConWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService).triggerEvent(2L, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService).triggerEvent(2L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotEmitBusinessProcessEvent_whenWrittenRepConcurrentDateIsFuture() {

        CaseDetails caseDetailsWrittenRepresentationConWithPastDate = caseDetailsWrittenRepresentationC.toBuilder().data(
            Map.of("judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                .writtenOption(CONCURRENT_REPRESENTATIONS)
                .writtenConcurrentRepresentationsBy(LocalDate.now().plusDays(1))
                .build())).state(AWAITING_WRITTEN_REPRESENTATIONS.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationConWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService, times(0)).triggerEvent(2L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenWrittenRepSequentialDateIsToday_LipCase() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationS));
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService)
            .triggerEvent(3L, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService)
            .triggerEvent(3L, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService)
            .triggerEvent(3L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verify(coreCaseDataService)
            .getSystemUpdateUserToken();
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("RESPONDENT"), anyString());
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("APPLICANT"), anyString());
        verify(coreCaseDataService).triggerEvent(3L, UPDATE_CLAIMANT_TASK_LIST_GA);
        verify(coreCaseDataService).triggerEvent(3L, UPDATE_RESPONDENT_TASK_LIST_GA);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenWrittenRepSequentialDateIsToday() {
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationS));
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService)
            .triggerEvent(3L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(dashboardNotificationService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenWrittenRepSequentialDateIsPast() {
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");
        CaseDetails caseDetailsWrittenRepresentationSeqWithPastDate = caseDetailsWrittenRepresentationS.toBuilder()
            .data(Map.of(
                "judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                    .writtenOption(SEQUENTIAL_REPRESENTATIONS)
                    .sequentialApplicantMustRespondWithin(LocalDate.now().minusDays(1))
                    .writtenSequentailRepresentationsBy(LocalDate.now().minusDays(1))
                    .build(),
                "isGaApplicantLip", "Yes"
            ))
            .state(AWAITING_WRITTEN_REPRESENTATIONS.toString())
            .build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationSeqWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService)
            .triggerEvent(3L, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService)
            .triggerEvent(3L, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        verify(coreCaseDataService)
            .triggerEvent(3L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verify(coreCaseDataService)
            .getSystemUpdateUserToken();
        verify(coreCaseDataService).triggerEvent(3L, UPDATE_CLAIMANT_TASK_LIST_GA);
        verify(coreCaseDataService).triggerEvent(3L, UPDATE_RESPONDENT_TASK_LIST_GA);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotEmitBusinessProcessEvent_whenWrittenRepSequentialDateIsFuture() {

        CaseDetails caseDetailsWrittenRepresentationSeqWithPastDate = caseDetailsWrittenRepresentationS.toBuilder().data(
            Map.of("judicialDecisionMakeAnOrderForWrittenRepresentations", GAJudicialWrittenRepresentations.builder()
                .writtenOption(SEQUENTIAL_REPRESENTATIONS)
                .sequentialApplicantMustRespondWithin(LocalDate.now().plusDays(1))
                .build())).state(AWAITING_WRITTEN_REPRESENTATIONS.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationSeqWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService, times(0))
            .triggerEvent(3L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenRequestForInformationDateIsToday() {
        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformation));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);
        verify(coreCaseDataService).triggerEvent(4L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenRequestForInformationDateIsPast() {

        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");
        CaseDetails caseDetailRequestForInformationWithPastDate = caseDetailRequestForInformation.toBuilder().data(
            Map.of("judicialDecision", GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build(),
                   "judicialDecisionRequestMoreInfo", GAJudicialRequestMoreInfo.builder()
                       .requestMoreInfoOption(REQUEST_MORE_INFORMATION)
                       .judgeRequestMoreInfoByDate(LocalDate.now().minusDays(1))
                       .judgeRequestMoreInfoText("test").build()
            )).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformationWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);
        verify(coreCaseDataService).triggerEvent(4L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(dashboardNotificationService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenRequestForInformationDateIsPast_whenLipCase() {

        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(coreCaseDataService.getSystemUpdateUserToken()).thenReturn("userToken");
        CaseDetails caseDetailRequestForInformationWithPastDate = caseDetailRequestForInformation.toBuilder().data(
            Map.of("judicialDecision", GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build(),
                   "judicialDecisionRequestMoreInfo", GAJudicialRequestMoreInfo.builder()
                       .requestMoreInfoOption(REQUEST_MORE_INFORMATION)
                       .judgeRequestMoreInfoByDate(LocalDate.now().minusDays(1))
                       .judgeRequestMoreInfoText("test").build(),
                   "isGaApplicantLip", "Yes"
            )).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformationWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);
        verify(coreCaseDataService).triggerEvent(4L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verify(coreCaseDataService)
            .getSystemUpdateUserToken();
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("RESPONDENT"), anyString());
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("APPLICANT"), anyString());
        verify(coreCaseDataService).triggerEvent(4L, UPDATE_CLAIMANT_TASK_LIST_GA);
        verify(coreCaseDataService).triggerEvent(4L, UPDATE_RESPONDENT_TASK_LIST_GA);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldNotEmitBusinessProcessEvent_whenRequestForInformationDateIsFuture() {

        CaseDetails caseDetailRequestForInformationWithPastDate = caseDetailRequestForInformation.toBuilder().data(
            Map.of("judicialDecision", GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build(),
                   "judicialDecisionRequestMoreInfo", GAJudicialRequestMoreInfo.builder()
                       .requestMoreInfoOption(REQUEST_MORE_INFORMATION)
                       .judgeRequestMoreInfoByDate(LocalDate.now().plusDays(1))
                       .judgeRequestMoreInfoText("test").build()
            )).state(AWAITING_ADDITIONAL_INFORMATION.toString()).build();

        when(caseStateSearchService.getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION))
            .thenReturn(Set.of(caseDetailRequestForInformationWithPastDate));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);
        verify(coreCaseDataService, times(0)).triggerEvent(4L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldNotEmitNotificationEvents_whenGAForLipsDisabled() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(false);
        when(caseStateSearchService.getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS))
            .thenReturn(Set.of(caseDetailsWrittenRepresentationS));

        gaJudgeRevisitTaskHandler.execute(externalTask, externalTaskService);

        verify(caseStateSearchService).getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        verify(coreCaseDataService)
            .triggerEvent(3L, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void getMaxAttemptsShouldAlwaysReturn1() {
        assertThat(gaJudgeRevisitTaskHandler.getMaxAttempts()).isEqualTo(1);
    }
}
