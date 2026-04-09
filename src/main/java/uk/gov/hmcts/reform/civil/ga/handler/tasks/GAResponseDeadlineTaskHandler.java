package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;

import java.util.List;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${response.deadline.check.event.emitter.enabled:true}")
public class GAResponseDeadlineTaskHandler extends BaseExternalTaskHandler {

    private final CaseStateSearchService caseSearchService;

    private final GaCoreCaseDataService coreCaseDataService;

    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = getAwaitingResponseCasesThatArePastDueDate();
        List<Long> ids = cases.stream().map(CaseDetails::getId).toList();
        log.info("GAResponseDeadlineTaskHandler Job '{}' found {} case(s) with ids {}", externalTask.getTopicName(), cases.size(), ids);

        cases.forEach(this::deleteDashboardNotifications);
        cases.forEach(this::fireEventForStateChange);

        Set<CaseDetails> caseList = getUrgentApplicationCasesThatArePastDueDate();
        List<Long> ids2 = caseList.stream().map(CaseDetails::getId).toList();
        log.info("GAResponseDeadlineTaskHandler Job '{}' found {} case(s) with ids {}", externalTask.getTopicName(), caseList.size(), ids2);
        caseList.forEach(this::deleteDashboardNotifications);

        return new ExternalTaskData();
    }

    private void deleteDashboardNotifications(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        log.info("Firing Event to delete dashboard notification caseId: {}", caseId);
        if (YesOrNo.YES == caseData.getIsGaApplicantLip() || YesOrNo.YES == caseData.getIsGaRespondentOneLip()) {
            try {
                log.info("calling triggerEvent");
                coreCaseDataService.triggerEvent(caseId, RESPONDENT_RESPONSE_DEADLINE_CHECK);
            } catch (Exception e) {
                log.error("Error in GAResponseDeadlineTaskHandler::deleteDashboardNotifications: " + e);
            }
        }
    }

    private void fireEventForStateChange(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("Firing event CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION to change the state from "
                     + "AWAITING_RESPONDENT_RESPONSE to APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION "
                     + "for caseId: {}", caseId);
        try {
            coreCaseDataService.triggerEvent(caseId, CHANGE_STATE_TO_AWAITING_JUDICIAL_DECISION);
        } catch (Exception e) {
            log.error("Error in GAResponseDeadlineTaskHandler::fireEventForStateChange: " + e);
        }
    }

    protected Set<CaseDetails> getAwaitingResponseCasesThatArePastDueDate() {
        Set<CaseDetails> awaitingResponseCases = caseSearchService
            .getGeneralApplications(AWAITING_RESPONDENT_RESPONSE);

        return awaitingResponseCases.stream()
            .filter(a -> {
                try {
                    return caseDetailsConverter.toGeneralApplicationCaseData(a).getGeneralAppNotificationDeadlineDate() != null
                        && now().isAfter(
                        caseDetailsConverter.toGeneralApplicationCaseData(a).getGeneralAppNotificationDeadlineDate());
                } catch (Exception e) {
                    log.error("GAResponseDeadlineTaskHandler failed: " + e);
                }
                return false;
            })
            .collect(toSet());
    }

    protected Set<CaseDetails> getUrgentApplicationCasesThatArePastDueDate() {
        Set<CaseDetails> awaitingResponseCases = caseSearchService
            .getGeneralApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);

        return awaitingResponseCases.stream()
            .filter(a -> {
                try {
                    return caseDetailsConverter.toGeneralApplicationCaseData(a).getGeneralAppNotificationDeadlineDate() != null
                        && now().isAfter(
                        caseDetailsConverter.toGeneralApplicationCaseData(a).getGeneralAppNotificationDeadlineDate());
                } catch (Exception e) {
                    log.error("GAResponseDeadlineTaskHandler failed: " + e);
                }
                return false;
            })
            .collect(toSet());
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
