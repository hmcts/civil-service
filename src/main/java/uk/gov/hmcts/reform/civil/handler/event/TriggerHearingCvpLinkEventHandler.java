package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CVP_JOIN_LINK;
import static uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus.LISTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerHearingCvpLinkEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final HearingsService hearingService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;

    @EventListener
    public void triggerCvpJoinLinkEvent(CvpJoinLinkEvent event) {
        var userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());

        var hearings = hearingService.getHearings(userToken, event.getCaseId(), LISTED.name());

        if (hearings.getCaseHearings() != null && hearings.getCaseHearings().size() > 0) {
            log.info("Triggering 'SEND_CVP_JOIN_LINK' for case: '{}'", event.getCaseId());
            coreCaseDataService.triggerEvent(event.getCaseId(), SEND_CVP_JOIN_LINK);
        }
    }
}
