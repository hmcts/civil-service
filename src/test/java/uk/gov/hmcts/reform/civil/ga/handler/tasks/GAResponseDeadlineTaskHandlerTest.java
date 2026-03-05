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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(MockitoExtension.class)
class GAResponseDeadlineTaskHandlerTest {

    @Mock
    private ExternalTask externalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private CaseStateSearchService caseSearchService;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(
        ObjectMapperFactory.instance());

    @InjectMocks
    private GAResponseDeadlineTaskHandler gaResponseDeadlineTaskHandler;

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;
    private CaseDetails caseDetails4;

    private final LocalDateTime deadlineCrossed = LocalDateTime.now().minusDays(2);
    private final LocalDateTime deadlineInFuture = LocalDateTime.now().plusDays(2);
    public static final String EXCEPTION_MESSAGE = "Unprocessable Entity";
    public static final String UNEXPECTED_RESPONSE_BODY = "Case data validation failed";

    Logger logger = (Logger) LoggerFactory.getLogger(GAResponseDeadlineTaskHandler.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeEach
    void init() {
        caseDetails1 = CaseDetails.builder().id(1L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineCrossed.toString())).build();
        caseDetails2 = CaseDetails.builder().id(2L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineCrossed.toString())).build();
        caseDetails3 = CaseDetails.builder().id(3L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineInFuture.toString())).build();
        caseDetails4 = CaseDetails.builder().id(4L).data(
            Map.of("generalAppNotificationDeadlineDate", EMPTY)).build();
    }

    @Test
    void throwException_whenUnprocessableEntityIsFound() {

        listAppender.start();
        logger.addAppender(listAppender);

        doThrow(buildFeignExceptionWithUnprocessableEntity()).when(coreCaseDataService)
            .triggerEvent(any(), any());

        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of(caseDetails1, caseDetails2, caseDetails3));

        assertThrows(FeignException.class, () -> coreCaseDataService
            .triggerEvent(any(), any()));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("Error in GAResponseDeadlineTaskHandler::fireEventForStateChange: "
                         + "feign.FeignException$FeignClientException: Unprocessable Entity",
                     logsList.get(4).getMessage());
        assertEquals(Level.ERROR, logsList.get(4).getLevel());
        listAppender.stop();
    }

    @Test
    void throwException_whenUnprocessableEntity() {

        listAppender.start();
        logger.addAppender(listAppender);

        CaseDetails caseDetailsRespondentResponse = CaseDetails.builder().id(6L).data(
            Map.of("generalAppConsentOrder", "maybe")).build();

        when(caseSearchService.getGeneralApplications(any()))
            .thenReturn(Set.of(caseDetailsRespondentResponse));

        gaResponseDeadlineTaskHandler.getAwaitingResponseCasesThatArePastDueDate();

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("GAResponseDeadlineTaskHandler failed: java.lang.IllegalArgumentException: "
                         + "Cannot deserialize value of type `uk.gov.hmcts.reform.civil.enums.YesOrNo` "
                         + "from String \"maybe\": not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.getFirst().getMessage());
        assertEquals(Level.ERROR, logsList.getFirst().getLevel());
        listAppender.stop();
    }

    @Test
    void shouldCatchException_andProceedFurther_withValidData() {

        listAppender.start();
        logger.addAppender(listAppender);

        CaseDetails caseDetailsRespondentResponse = CaseDetails.builder().id(6L).data(
            Map.of("generalAppConsentOrder", "maybe")).build();

        when(caseSearchService.getGeneralApplications(any()))
            .thenReturn(Set.of(caseDetailsRespondentResponse, caseDetails1));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals("GAResponseDeadlineTaskHandler failed: java.lang.IllegalArgumentException: "
                         + "Cannot deserialize value of type `uk.gov.hmcts.reform.civil.enums.YesOrNo` "
                         + "from String \"maybe\": not one of the values accepted for Enum class: [No, Yes]\n"
                         + " at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: "
                         + "uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData"
                         + "[\"generalAppConsentOrder\"])",
                     logsList.getFirst().getMessage());
        assertEquals(Level.ERROR, logsList.getFirst().getLevel());
        verify(caseSearchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verify(coreCaseDataService).triggerEvent(1L, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
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
        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE)).thenReturn(Set.of());

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        verify(caseSearchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesPastDeadlineFound() {
        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of(caseDetails1, caseDetails2, caseDetails3));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        verify(caseSearchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verify(coreCaseDataService).triggerEvent(1L, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
        verify(coreCaseDataService).triggerEvent(2L, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());

    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesPastDeadlineFound2() {
        CaseDetails caseDetails6 = CaseDetails.builder().id(1L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineCrossed.toString(),
                   "isGaApplicantLip",  YesOrNo.YES)).build();
        CaseDetails caseDetails7 = CaseDetails.builder().id(2L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineCrossed.toString(),
                   "isGaApplicantLip", YesOrNo.YES)).build();
        CaseDetails caseDetails8 = CaseDetails.builder().id(3L).data(
            Map.of("generalAppNotificationDeadlineDate", deadlineInFuture.toString(),
                   "isGaApplicantLip",  YesOrNo.YES)).build();
        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE))
            .thenReturn(Set.of());
        when(caseSearchService.getGeneralApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION))
            .thenReturn(Set.of(caseDetails6, caseDetails7, caseDetails8));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        verify(caseSearchService).getGeneralApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
        verify(coreCaseDataService).triggerEvent(1L, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService).triggerEvent(2L, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verifyNoMoreInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesPastDeadlineNotFound() {
        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE)).thenReturn(Set.of(caseDetails3));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        verify(caseSearchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCasesFoundWithNullDeadlineDate() {
        when(caseSearchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE)).thenReturn(Set.of(caseDetails4));

        gaResponseDeadlineTaskHandler.execute(externalTask, externalTaskService);

        verify(caseSearchService).getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);
        verifyNoInteractions(coreCaseDataService);
        verify(externalTaskService).complete(any(), any());
    }

    @Test
    void getMaxAttemptsShouldAlwaysReturn1() {
        assertThat(gaResponseDeadlineTaskHandler.getMaxAttempts()).isEqualTo(1);
    }
}
