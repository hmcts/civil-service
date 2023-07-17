package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutomatedHearingNoticeHandler implements BaseExternalTaskHandler {

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final RuntimeService runtimeService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper mapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @SuppressWarnings("unchecked")
    public void handleTask(ExternalTask externalTask) {
        if (!featureToggleService.isAutomatedHearingNoticeEnabled()) {
            runtimeService.setVariables(
                externalTask.getProcessInstanceId(),
                HearingNoticeSchedulerVars.builder()
                    .totalNumberOfUnnotifiedHearings(0)
                    .build().toMap(mapper)
            );
            return;
        }

        HearingNoticeSchedulerVars schedulerVars = mapper.convertValue(externalTask.getAllVariables(), HearingNoticeSchedulerVars.class);
        List<String> dispatchedHearingIds = getDispatchedHearingIds(schedulerVars);
        UnNotifiedHearingResponse unnotifiedHearings = getUnnotifiedHearings(schedulerVars.getServiceId());

        log.info("Found [{}] unnotified hearings", unnotifiedHearings.getTotalFound());

        unnotifiedHearings.getHearingIds()
            .stream()
            .filter(hearingId -> !hearingNoticeDispatched(hearingId, dispatchedHearingIds))
            .forEach(hearingId -> {
                applicationEventPublisher.publishEvent(new HearingNoticeSchedulerTaskEvent(hearingId));
                dispatchedHearingIds.add(hearingId);
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
            LocalDateTime.now().minusDays(7),
            null
        );
    }

    private List<String> getDispatchedHearingIds(HearingNoticeSchedulerVars schedulerVars) {
        return schedulerVars.getDispatchedHearingIds() != null
            ? schedulerVars.getDispatchedHearingIds() : new ArrayList<>();
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return UserAuthContent.builder().userToken(userToken).userId(userId).build();
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
