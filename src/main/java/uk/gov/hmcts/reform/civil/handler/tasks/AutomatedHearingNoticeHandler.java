package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeSchedulerVars;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AutomatedHearingNoticeHandler extends BaseExternalTaskHandler {

    private static final String SCHEDULER_NAME = "AutomatedHearingNotice";

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final RuntimeService runtimeService;
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public AutomatedHearingNoticeHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        UserService userService,
        SystemUpdateUserConfiguration userConfig,
        HearingsService hearingsService,
        RuntimeService runtimeService,
        ObjectMapper mapper,
        ApplicationEventPublisher applicationEventPublisher,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.userService = userService;
        this.userConfig = userConfig;
        this.hearingsService = hearingsService;
        this.runtimeService = runtimeService;
        this.mapper = mapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return new ExternalTaskData();
        }

        HearingNoticeSchedulerVars schedulerVars = mapper.convertValue(externalTask.getAllVariables(), HearingNoticeSchedulerVars.class);
        List<String> dispatchedHearingIds = getDispatchedHearingIds(schedulerVars);
        UnNotifiedHearingResponse unnotifiedHearings = getUnnotifiedHearings(schedulerVars.getServiceId());
        log.info("Job '{}' found {} dispatched unnotified hearing(s) with ids {}",
                 externalTask.getTopicName(), unnotifiedHearings.getTotalFound(), unnotifiedHearings.getHearingIds());

        unnotifiedHearings.getHearingIds()
            .stream()
            .filter(hearingId -> !hearingNoticeDispatched(hearingId, dispatchedHearingIds))
            .forEach(hearingId -> {
                applicationEventPublisher.publishEvent(new HearingNoticeSchedulerTaskEvent(hearingId));
                dispatchedHearingIds.add(hearingId);
                throttle(unnotifiedHearings.getTotalFound());
            });

        runtimeService.setVariables(
            externalTask.getProcessInstanceId(),
            new HearingNoticeSchedulerVars()
                .setDispatchedHearingIds(dispatchedHearingIds)
                .setTotalNumberOfUnnotifiedHearings(unnotifiedHearings.getTotalFound().intValue())
                .toMap(mapper)
        );

        return new ExternalTaskData();
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
        return new UserAuthContent().setUserToken(userToken).setUserId(userId);
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
