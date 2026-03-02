package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.event.HearingNoticeSchedulerTaskEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.variables.HearingNoticeMessageVars;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.data.UserAuthContent;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.Arrays;

import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingNoticeSchedulerEventHandler {

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;
    private final RuntimeService runtimeService;
    private final ObjectMapper mapper;
    private final CoreCaseDataService coreCaseDataService;
    static final CaseState[] DISALLOWED_CASE_STATES = {
        CASE_SETTLED,
        PROCEEDS_IN_HERITAGE_SYSTEM,
        CASE_STAYED,
        CASE_DISCONTINUED,
        CASE_DISMISSED,
        CLOSED,
        All_FINAL_ORDERS_ISSUED
    };

    @Async("asyncHandlerExecutor")
    @EventListener
    public void handle(HearingNoticeSchedulerTaskEvent event) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            String hearingId = event.getHearingId();
            try {
                processHearing(hearingId);
                break;
            } catch (Exception e) {
                log.error("Processing hearingId [{}] failed due to error: {}", hearingId, e.getMessage());
            }
        }
    }

    private void processHearing(String hearingId) {
        HearingGetResponse hearing = hearingsService.getHearingResponse(getSystemUpdateUser().getUserToken(), hearingId);
        ListAssistCaseStatus hearingStatus = hearing.getHearingResponse().getLaCaseStatus();
        int requestedHearingVersion = hearing.getRequestDetails().getVersionNumber().intValue();
        log.info("Processing hearing id: [{}] status: [{}] and requested version: [{}]", hearingId, hearingStatus, requestedHearingVersion);

        if (hearingStatus.equals(ListAssistCaseStatus.LISTED)) {
            PartiesNotifiedResponse partiesNotified = getLatestPartiesNotifiedResponse(hearingId);
            String caseReference = hearing.getCaseDetails().getCaseRef();
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseReference));

            boolean stateNotAllowedToGenerateHearingNotice = isNotAllowedState(caseDetails.getState(), caseReference);

            if (!stateNotAllowedToGenerateHearingNotice
                && HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
                log.info("Dispatching hearing notice task for hearing [{}].",
                        hearingId);
                triggerHearingNoticeEvent(new HearingNoticeMessageVars()
                        .setHearingId(hearingId)
                        .setCaseId(caseReference)
                        .setTriggeredViaScheduler(true));

            } else {
                notifyHmc(hearingId, hearing, partiesNotified.getServiceData());
            }
        } else {
            notifyHmc(hearingId, hearing, new PartiesNotifiedServiceData());
        }
    }

    private static boolean isNotAllowedState(String state, String caseReferene) {
        try {
            CaseState caseState = CaseState.valueOf(state);
            return Arrays.asList(DISALLOWED_CASE_STATES).contains(caseState);
        } catch (IllegalArgumentException e) {
            log.info("Case state is not a valid one {} ", caseReferene);
            return true;
        }
    }

    private void triggerHearingNoticeEvent(HearingNoticeMessageVars messageVars) {
        String hearingNoticeMessage = "NOTIFY_HEARING_PARTIES";
        runtimeService
            .createMessageCorrelation(hearingNoticeMessage)
            .setVariables(messageVars.toMap(mapper))
            .correlateStartMessage();
    }

    private UserAuthContent getSystemUpdateUser() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String userId = userService.getUserInfo(userToken).getUid();
        return new UserAuthContent().setUserToken(userToken).setUserId(userId);
    }

    private void notifyHmc(String hearingId, HearingGetResponse hearing, PartiesNotifiedServiceData serviceData) {
        var partiesNotifiedPayload = new PartiesNotified()
            .setServiceData(serviceData);
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
