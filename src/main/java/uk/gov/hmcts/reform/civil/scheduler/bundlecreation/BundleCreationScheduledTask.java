package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.NoCacheUserService;

import java.util.List;

@Component
@Slf4j
public class BundleCreationScheduledTask implements ScheduledTask {

    private static final long LOCK_DURATION = 600000L;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final SystemUpdateUserConfiguration userConfig;
    private final NoCacheUserService noCacheUserService;
    private final SchedulerThrottleService schedulerThrottleService;
    private final long waitTime;

    public BundleCreationScheduledTask(
        ApplicationEventPublisher applicationEventPublisher,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SystemUpdateUserConfiguration userConfig,
        NoCacheUserService noCacheUserService,
        SchedulerThrottleService schedulerThrottleService,
        @Value("${stitch-bundle.wait-time-in-milliseconds}") long waitTime
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.caseDetailsConverter = caseDetailsConverter;
        this.coreCaseDataService = coreCaseDataService;
        this.userConfig = userConfig;
        this.noCacheUserService = noCacheUserService;
        this.schedulerThrottleService = schedulerThrottleService;
        this.waitTime = waitTime;
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        accept(caseDetails, 0);
    }

    public void accept(CaseDetails caseDetails, int totalCases) {
        Long caseId = caseDetails.getId();
        log.info("BundleCreationScheduledTask::accept case {}", caseId);

        if (isBundleCreatedForHearingDate(caseId)) {
            log.info("Bundle is already created for {}", caseId);
            return;
        }

        String accessToken = noCacheUserService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        applicationEventPublisher.publishEvent(new BundleCreationTriggerEvent(caseId, accessToken));
        schedulerThrottleService.throttle(totalCases, waitTime, LOCK_DURATION);
    }

    boolean isBundleCreatedForHearingDate(Long caseId) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId).getData());
        List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
        if (caseBundles == null || caseData.getHearingDate() == null) {
            return false;
        }

        return caseBundles.stream()
            .filter(bundle -> bundle != null && bundle.getValue() != null)
            .map(IdValue::getValue)
            .anyMatch(bundle -> bundle.getBundleHearingDate()
                .filter(caseData.getHearingDate()::isEqual)
                .isPresent());
    }

}
