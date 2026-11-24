package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAResponseDeadlineTask implements ScheduledTask<CaseDetails, Long> {

    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        log.info("GAResponseDeadlineTask processing caseId: {}, state: {}", caseId, caseDetails.getState());

        if (isLipApplication(caseData)) {
            cleanUpRespondentDashboardNotification(caseId);
        }

        if (AWAITING_RESPONDENT_RESPONSE.name().equals(caseDetails.getState())) {
            transitionStateToAwaitingJudicialDecision(caseId);
        }
    }

    private void cleanUpRespondentDashboardNotification(Long caseId) {
        log.info("Firing RESPONDENT_RESPONSE_DEADLINE_CHECK for caseId: {}", caseId);
        coreCaseDataService.triggerEvent(caseId, RESPONDENT_RESPONSE_DEADLINE_CHECK);
    }

    private void transitionStateToAwaitingJudicialDecision(Long caseId) {
        log.info("Firing event CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION to change the state from "
                     + "AWAITING_RESPONDENT_RESPONSE to APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION "
                     + "for caseId: {}", caseId);
        coreCaseDataService.triggerEvent(caseId, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
    }

    private boolean isLipApplication(GeneralApplicationCaseData caseData) {
        return YesOrNo.YES == caseData.getIsGaApplicantLip() || YesOrNo.YES == caseData.getIsGaRespondentOneLip();
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
