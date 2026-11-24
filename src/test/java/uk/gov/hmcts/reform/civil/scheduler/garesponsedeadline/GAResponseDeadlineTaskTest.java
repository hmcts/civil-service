package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(MockitoExtension.class)
class GAResponseDeadlineTaskTest {

    private static final Long CASE_ID = 123L;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private GAResponseDeadlineTask task;

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);
        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }

    @Test
    void shouldTriggerBothEventsWhenAwaitingResponseAndApplicantIsLip() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).state(AWAITING_RESPONDENT_RESPONSE.name()).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldTriggerBothEventsWhenAwaitingResponseAndRespondentIsLip() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).state(AWAITING_RESPONDENT_RESPONSE.name()).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaRespondentOneLip(YesOrNo.YES);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldOnlyTriggerStateChangeWhenAwaitingResponseAndNeitherPartyIsLip() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).state(AWAITING_RESPONDENT_RESPONSE.name()).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldTriggerNotificationCheckOnlyWhenUrgentApplicationAndPartyIsLip() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .state(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name())
            .build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldNotTriggerAnyEventsWhenUrgentApplicationAndNeitherPartyIsLip() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .state(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name())
            .build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldPropagateExceptionWhenNotificationCheckFailsForAwaitingResponse() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).state(AWAITING_RESPONDENT_RESPONSE.name()).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException("Notification delete failed"))
            .when(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);

        assertThatThrownBy(() -> task.accept(caseDetails))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Notification delete failed");

        verify(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldPropagateExceptionWhenStateChangeFailsForAwaitingResponse() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).state(AWAITING_RESPONDENT_RESPONSE.name()).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException("State change failed"))
            .when(coreCaseDataService).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);

        assertThatThrownBy(() -> task.accept(caseDetails))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("State change failed");

        verify(coreCaseDataService).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldPropagateExceptionWhenNotificationCheckFailsForUrgentApplication() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .state(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name())
            .build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES);

        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException("Notification delete failed"))
            .when(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);

        assertThatThrownBy(() -> task.accept(caseDetails))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Notification delete failed");

        verify(coreCaseDataService).triggerEvent(CASE_ID, RESPONDENT_RESPONSE_DEADLINE_CHECK);
        verify(coreCaseDataService, never()).triggerEvent(CASE_ID, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }
}
