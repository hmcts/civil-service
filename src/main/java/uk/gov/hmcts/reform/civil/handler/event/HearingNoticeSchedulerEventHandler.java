package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingNoticeSchedulerEventHandler {

    private static String HEARING_NOTICE_MESSAGE = "NOTIFY_HEARING_PARTIES";
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final RuntimeService runtimeService;
    private final ObjectMapper mapper;

    @Async("asyncHandlerExecutor")
    @EventListener
    public void handle(HearingNoticeSchedulerTaskEvent event) {
        String hearingId = event.getHearingId();

        try {
            HearingGetResponse hearing = hearingsService.getHearingResponse(getSystemUpdateUser().getUserToken(), hearingId);
            ListAssistCaseStatus hearingStatus = hearing.getHearingResponse().getLaCaseStatus();
            log.info("Processing hearing id: [{}] status: [{}]", hearingId, hearingStatus);

            if (hearingStatus.equals(ListAssistCaseStatus.LISTED)) {
                PartiesNotifiedResponse partiesNotified = getLatestPartiesNotifiedResponse(hearingId);
                if (HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
                    log.info("Dispatching hearing notice task for hearing [{}].",
                             hearingId);
                    triggerHearingNoticeEvent(HearingNoticeMessageVars.builder()
                                                  .hearingId(hearingId)
                                                  .caseId(hearing.getCaseDetails().getCaseRef())
                                                  .triggeredViaScheduler(true)
                                                  .build());

                } else {
                    notifyHmc(hearingId, hearing, partiesNotified.getServiceData());
                }
            } else {
                notifyHmc(hearingId, hearing, PartiesNotifiedServiceData.builder().build());
            }
        } catch (Exception e) {
            log.error("Processing hearingId [{}] failed due to error: {}", hearingId, e.getMessage());
        }
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

}
