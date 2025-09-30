package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.search.CaseStateSearchService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIMANT_TASK_LIST_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_RESPONDENT_TASK_LIST_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${judge.revisit.check.event.emitter.enabled:true}")
public class GAJudgeRevisitTaskHandler extends BaseExternalTaskHandler {

    private final CaseStateSearchService caseStateSearchService;

    private final CoreCaseDataService coreCaseDataService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaForLipService gaForLipService;
    private final DocUploadDashboardNotificationService dashboardNotificationService;

    private final FeatureToggleService featureToggleService;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> writtenRepresentationCases = caseStateSearchService
            .getGeneralApplications(AWAITING_WRITTEN_REPRESENTATIONS);
        List<CaseDetails> claimantNotificationCases = filterForClaimantWrittenRepExpired(writtenRepresentationCases);
        log.info("Job '{}' found {} written representation case(s) with claimant deadline expired {}",
                 externalTask.getTopicName(), claimantNotificationCases.size(), claimantNotificationCases.stream().map(CaseDetails::getId).sorted().toList());
        if (featureToggleService.isGaForLipsEnabled()) {
            claimantNotificationCases.forEach(this::fireEventForDeleteClaimantNotification);
        }

        List<CaseDetails> defendantNotificationCases = filterForDefendantWrittenRepExpired(writtenRepresentationCases);
        log.info("Job '{}' found {} written representation case(s) with defendant deadline expired {}",
                 externalTask.getTopicName(), defendantNotificationCases.size(), defendantNotificationCases.stream().map(CaseDetails::getId).sorted().toList());
        if (featureToggleService.isGaForLipsEnabled()) {
            defendantNotificationCases.forEach(this::fireEventForDeleteDefendantNotification);
        }

        // Change state for all cases where both deadlines have passed
        claimantNotificationCases.stream().filter(defendantNotificationCases::contains)
            .forEach(casDetails -> {
                fireEventForStateChange(casDetails);
                fireEventForUpdatingTaskList(casDetails);

            });
        List<CaseDetails> directionOrderCases = getDirectionOrderCaseReadyToJudgeRevisit();
        log.info("Job '{}' found {} direction order case(s) {}",
                 externalTask.getTopicName(), directionOrderCases.size(), directionOrderCases.stream().map(CaseDetails::getId).sorted().toList());
        directionOrderCases.forEach(this::fireEventForStateChange);

        if (featureToggleService.isGaForLipsEnabled()) {
            directionOrderCases.forEach(this::fireEventForUpdatingTaskList);
        }
        List<CaseDetails> requestForInformationCases = getRequestForInformationCaseReadyToJudgeRevisit();
        log.info("Job '{}' found {} request for information case(s) {}",
                 externalTask.getTopicName(), requestForInformationCases.size(), requestForInformationCases.stream().map(CaseDetails::getId).sorted().toList());
        requestForInformationCases.forEach(this::fireEventForStateChange);
        if (featureToggleService.isGaForLipsEnabled()) {
            requestForInformationCases.forEach(this::fireEventForUpdatingTaskList);
        }
        return ExternalTaskData.builder().build();
    }

    protected void fireEventForStateChange(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("Firing event CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED "
                     + "for caseId: {}", caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        try {
            coreCaseDataService.triggerEvent(caseId, CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
            // Generate Dashboard Notification for Lip Party
            if (gaForLipService.isGaForLip(caseData)) {
                String userToken = coreCaseDataService.getSystemUpdateUserToken();
                dashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", userToken);
                dashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", userToken);

            }
        } catch (Exception exception) {
            log.error("Error in GAJudgeRevisitTaskHandler::fireEventForStateChange: " + exception);
        }
    }

    protected void fireEventForDeleteClaimantNotification(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("Firing event DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION to delete "
                     + "written representations notification"
                     + "for caseId: {}", caseId);
        try {
            coreCaseDataService.triggerEvent(caseId, DELETE_CLAIMANT_WRITTEN_REPS_NOTIFICATION);
        } catch (Exception exception) {
            log.error("Error in GAJudgeRevisitTaskHandler::fireEventForDeleteClaimantNotification: " + exception);
        }
    }

    protected void fireEventForDeleteDefendantNotification(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("Firing event DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION to delete "
                     + "written representations notification"
                     + "for caseId: {}", caseId);
        try {
            coreCaseDataService.triggerEvent(caseId, DELETE_DEFENDANT_WRITTEN_REPS_NOTIFICATION);
        } catch (Exception exception) {
            log.error("Error in GAJudgeRevisitTaskHandler::fireEventForDeleteDefendantNotification: " + exception);
        }
    }

    protected void fireEventForUpdatingTaskList(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("Firing event UPDATE_CLAIMANT_TASK_LIST_GA "
                     + "for judge direction order"
                     + "for caseId: {}", caseId);
        try {
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            if (gaForLipService.isGaForLip(caseData)) {
                coreCaseDataService.triggerEvent(caseId, UPDATE_CLAIMANT_TASK_LIST_GA);
                coreCaseDataService.triggerEvent(caseId, UPDATE_RESPONDENT_TASK_LIST_GA);
            }
        } catch (Exception exception) {
            log.error("Error in GAJudgeRevisitTaskHandler::fireEventForUpdatingClaimantTaskList: " + exception);
        }
    }

    protected List<CaseDetails> filterForClaimantWrittenRepExpired(Set<CaseDetails> writtenRepCases) {
        return writtenRepCases.stream()
            .filter(a -> {
                try {
                    return (caseDetailsConverter.toCaseData(a).getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenOption().equals(CONCURRENT_REPRESENTATIONS))
                        && (!LocalDate.now().isBefore(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                                          .getWrittenConcurrentRepresentationsBy()))
                        || caseDetailsConverter.toCaseData(a).getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenOption().equals(SEQUENTIAL_REPRESENTATIONS)
                        && (!LocalDate.now().isBefore(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                                          .getSequentialApplicantMustRespondWithin()));
                } catch (Exception e) {
                    log.error("Error GAJudgeRevisitTaskHandler::getWrittenRepCaseReadyToJudgeRevisit : " + e);
                }
                return false;
            }).toList();
    }

    protected List<CaseDetails> filterForDefendantWrittenRepExpired(Set<CaseDetails> writtenRepCases) {
        return writtenRepCases.stream()
            .filter(a -> {
                try {
                    return (caseDetailsConverter.toCaseData(a).getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenOption().equals(CONCURRENT_REPRESENTATIONS))
                        && (LocalDate.now().compareTo(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                                          .getWrittenConcurrentRepresentationsBy()) >= 0)
                        || caseDetailsConverter.toCaseData(a).getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenOption().equals(SEQUENTIAL_REPRESENTATIONS)
                        && (LocalDate.now().compareTo(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                                                          .getWrittenSequentailRepresentationsBy()) >= 0);
                } catch (Exception e) {
                    log.error("Error GAJudgeRevisitTaskHandler::getWrittenRepCaseReadyToJudgeRevisit : " + e);
                }
                return false;
            }).toList();
    }

    protected List<CaseDetails> getDirectionOrderCaseReadyToJudgeRevisit() {
        Set<CaseDetails> judgeReadyToRevisitDirectionOrderCases = caseStateSearchService
            .getGeneralApplications(AWAITING_DIRECTIONS_ORDER_DOCS);

        return judgeReadyToRevisitDirectionOrderCases.stream()
            .filter(a -> {
                try {
                    return (caseDetailsConverter.toCaseData(a).getJudicialDecisionMakeOrder().getMakeAnOrder()
                        .equals(GIVE_DIRECTIONS_WITHOUT_HEARING))
                        && (!LocalDate.now().isBefore(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionMakeOrder()
                                                          .getDirectionsResponseByDate()));
                } catch (Exception e) {
                    log.error("Error GAJudgeRevisitTaskHandler::getDirectionOrderCaseReadyToJudgeRevisit : " + e);
                }
                return false;
            }).toList();
    }

    protected List<CaseDetails> getRequestForInformationCaseReadyToJudgeRevisit() {
        Set<CaseDetails> judgeReadyToRevisitRequestForInfoCases = caseStateSearchService
            .getGeneralApplications(AWAITING_ADDITIONAL_INFORMATION);

        return judgeReadyToRevisitRequestForInfoCases.stream()
            .filter(a -> {
                try {
                    return caseDetailsConverter.toCaseData(a)
                        .getJudicialDecisionRequestMoreInfo()
                        .getJudgeRequestMoreInfoByDate() != null
                        && (!LocalDate.now().isBefore(caseDetailsConverter.toCaseData(a)
                                                          .getJudicialDecisionRequestMoreInfo()
                                                          .getJudgeRequestMoreInfoByDate()));
                } catch (Exception e) {
                    log.error("GAJudgeRevisitTaskHandler failed: " + e);
                }
                return false;
            })
            .toList();
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
