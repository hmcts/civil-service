package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
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

        log.info(String.format("Found (%d) unnotified hearings", unnotifiedHearings.getTotalFound()));

        unnotifiedHearings.getHearingIds()
            .stream()
            .filter(hearingId -> !hearingNoticeDispatched(hearingId, dispatchedHearingIds))
            .forEach(hearingId -> {
                try {
                    var hearing = hearingsService.getHearingResponse(getSystemUpdateUser().getUserToken(), hearingId);
                    var hearingStatus = hearing.getHearingResponse().getListAssistCaseStatus();
                    log.info(String.format("Processing hearing id: %s status: %s", hearingId, hearingStatus));

                    if (hearingStatus.equals(ListAssistCaseStatus.LISTED)) {
                        var partiesNotified = getLatestPartiesNotifiedResponse(hearingId);
                        if (HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
                            log.info(String.format(
                                "Dispatching new camunda process to generate hearing notice for hearing: %s.",
                                hearingId
                            ));
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
                    log.error(String.format("An error occured when processing hearingId [%s]: %s",
                                            hearingId, e.getMessage()
                    ));
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
            hearing.getRequestDetails().getVersionNumber(),
            hearing.getHearingResponse().getReceivedDateTime(),
            partiesNotifiedPayload
        );
    }

    private PartiesNotifiedResponse getLatestPartiesNotifiedResponse(String hearingId) {
        var partiesNotified = hearingsService.getPartiesNotifiedResponses(
            getSystemUpdateUser().getUserToken(), hearingId);
        return HmcDataUtils.getLatestPartiesNotifiedResponse(partiesNotified);
    }

    private boolean hearingNoticeDispatched(String hearingId, List<String> dispatchedHearingIds) {
        if (dispatchedHearingIds.contains(hearingId)) {
            log.info(String.format(
                "A process has already been dispatched for hearing: %s. Skipping...",
                hearingId
            ));
            return true;
        }
        return false;
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}

