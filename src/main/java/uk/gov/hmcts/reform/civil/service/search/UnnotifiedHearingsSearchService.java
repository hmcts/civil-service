package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice.AutomatedHearingNoticeScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnnotifiedHearingsSearchService {

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final HearingsService hearingsService;

    @Value("${scheduler.automated-hearing-notice.serviceIds}")
    private List<String> serviceIds;

    public TaskResult<String> getUnnotifiedHearings() {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        List<String> hearingIds = new ArrayList<>();
        int totalFound = 0;

        for (String serviceId : serviceIds) {
            UnNotifiedHearingResponse response = hearingsService.getUnNotifiedHearingResponses(
                userToken,
                serviceId,
                LocalDateTime.now(ZoneId.systemDefault()).minusDays(7),
                null
            );
            List<String> serviceHearingIds = response.getHearingIds() == null ? List.of() : response.getHearingIds();
            hearingIds.addAll(serviceHearingIds);
            totalFound += Math.toIntExact(response.getTotalFound());
            log.info(
                "{} scheduler found {} dispatched unnotified hearing(s) for serviceId {} with ids {}",
                AutomatedHearingNoticeScheduler.SCHEDULER_NAME,
                response.getTotalFound(),
                serviceId,
                serviceHearingIds
            );
        }

        return new ListTaskResult<>(hearingIds, totalFound);
    }
}
