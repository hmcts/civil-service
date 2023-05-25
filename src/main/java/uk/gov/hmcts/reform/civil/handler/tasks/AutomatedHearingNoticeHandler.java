package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutomatedHearingNoticeHandler implements BaseExternalTaskHandler {

    private static String HEARING_NOTICE_MESSAGE = "NOTIFY_HEARING_PARTIES";
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final RuntimeService runtimeService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper mapper;

    @Override
    @SuppressWarnings("unchecked")
    public void handleTask(ExternalTask externalTask) {
        if (!featureToggleService.isAutomatedHearingNoticeEnabled()) {
            return;
        }

        var schedulerVars = mapper.convertValue(externalTask.getAllVariables(), HearingNoticeSchedulerVars.class);
        var dispatchedHearingIds = getDispatchedHearingIds(schedulerVars);
        var unnotifiedHearings = getUnnotifiedHearings(schedulerVars.getServiceId());

        log.info("Found [{}] unnotified hearings", unnotifiedHearings.getTotalFound());

        unnotifiedHearings.getHearingIds()
            .stream()
            .filter(hearingId -> !hearingNoticeDispatched(hearingId, dispatchedHearingIds))
            .forEach(hearingId -> {
                try {
                    var hearing = hearingsService.getHearingResponse(getSystemUpdateUser().getUserToken(), hearingId);
                    var hearingStatus = hearing.getHearingResponse().getListAssistCaseStatus();
                    log.info("Processing hearing id: [{}] status: [{}]", hearingId, hearingStatus);

                    if (hearingStatus.equals(ListAssistCaseStatus.LISTED)) {
                        var partiesNotified = getLatestPartiesNotifiedResponse(hearingId);
                        if (HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
                            log.info("Dispatching hearing notice task for hearing [{}].",
                                hearingId);
                            triggerHearingNoticeEvent(HearingNoticeMessageVars.builder()
                                                          .hearingId(hearingId)
                                                          .caseId(hearing.getCaseDetails().getCaseRef())
                                                          .triggeredViaScheduler(true)
                                                          .build());
                            dispatchedHearingIds.add(hearingId);

                        } else {
                            notifyHmc(hearingId, hearing, partiesNotified.getServiceData());
                        }
                    } else {
                        notifyHmc(hearingId, hearing, PartiesNotifiedServiceData.builder().build());
                    }
                } catch (Exception e) {
                    log.error("Processing hearingId [{}] failed due to error: {}", hearingId, e.getMessage());
                }
            });

        runtimeService.setVariables(
            externalTask.getProcessInstanceId(),
            HearingNoticeSchedulerVars.builder()
                .dispatchedHearingIds(dispatchedHearingIds)
                .totalNumberOfUnnotifiedHearings(unnotifiedHearings.getTotalFound().intValue())
                .build().toMap(mapper)
        );
    }

    @Override
    public void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();

        try {
            ofNullable(getVariableMap()).ifPresentOrElse(
                variableMap -> externalTaskService.complete(externalTask, variableMap),
                () -> externalTaskService.complete(externalTask)
            );
            log.info("External task '{}' finished with processInstanceId '{}'",
                     topicName, processInstanceId
            );
        } catch (NotFoundException e) {
            log.info("Completing external task '{}' was skipped as process instance '{}' has already completed.",
                      topicName, processInstanceId);
        } catch (Exception ex) {
            log.error("Completing external task '{}' errored  with processInstanceId '{}'",
                      topicName, processInstanceId, ex);
        }
    }

    private UnNotifiedHearingResponse getUnnotifiedHearings(String serviceId) {
        return hearingsService.getUnNotifiedHearingResponses(
            getSystemUpdateUser().getUserToken(),
            serviceId,
            null,
            null
        );
    }

    private List<String> getDispatchedHearingIds(HearingNoticeSchedulerVars schedulerVars) {
        return schedulerVars.getDispatchedHearingIds() != null
            ? schedulerVars.getDispatchedHearingIds() : new ArrayList<>();
    }

    private void triggerHearingNoticeEvent(HearingNoticeMessageVars messageVars) {
        runtimeService
            .createMessageCorrelation(HEARING_NOTICE_MESSAGE)
            .setVariables(messageVars.toMap(mapper))
            .correlateStartMessage();
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
    }

    private void notifyHmc(String hearingId, HearingGetResponse hearing, PartiesNotifiedServiceData serviceData) {
        var partiesNotifiedPayload = PartiesNotified.builder()
            .serviceData(serviceData.toBuilder().hearingNoticeGenerated(false).build())
            .build();
        hearingsService.updatePartiesNotifiedResponse(
            getSystemUpdateUser().getUserToken(),
            hearingId,
            hearing.getRequestDetails().getVersionNumber().intValue(),
            hearing.getHearingResponse().getReceivedDateTime(),
            partiesNotifiedPayload
        );
    }

    private PartiesNotifiedResponse getLatestPartiesNotifiedResponse(String hearingId) {
        var partiesNotified = hearingsService.getPartiesNotifiedResponses(
            getSystemUpdateUser().getUserToken(), hearingId);
        return HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified);
    }

    private boolean hearingNoticeDispatched(String hearingId, List<String> dispatchedHearingIds) {
        if (dispatchedHearingIds.contains(hearingId)) {
            log.info("A process has already been dispatched for hearing [{}]. Skipping...", hearingId);
            return true;
        }
        return false;
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
