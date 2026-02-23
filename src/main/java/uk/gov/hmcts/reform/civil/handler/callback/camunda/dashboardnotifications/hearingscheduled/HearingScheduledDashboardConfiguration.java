package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantHmcDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledDefendantDashboardService;

@Configuration
public class HearingScheduledDashboardConfiguration {

    @Bean
    public HearingScheduledDashboardTaskContributor hearingScheduledDashboardTaskContributor(
        @Qualifier("hearingScheduledClaimantDashboardService") HearingScheduledClaimantDashboardService claimantService,
        HearingScheduledDefendantDashboardService defendantService) {
        return new HearingScheduledDashboardTaskContributor(
            DashboardTaskIds.HEARING_SCHEDULED,
            new HearingScheduledClaimantDashboardTask(claimantService),
            new HearingScheduledDefendantDashboardTask(defendantService)
        );
    }

    @Bean
    public HearingScheduledDashboardTaskContributor hearingScheduledHmcDashboardTaskContributor(
        @Qualifier("hearingScheduledClaimantHmcDashboardService") HearingScheduledClaimantHmcDashboardService claimantHmcService,
        HearingScheduledDefendantDashboardService defendantService) {
        return new HearingScheduledDashboardTaskContributor(
            DashboardTaskIds.HEARING_SCHEDULED_HMC,
            new HearingScheduledClaimantDashboardTask(claimantHmcService),
            new HearingScheduledDefendantDashboardTask(defendantService)
        );
    }
}
