package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.NoCacheUserService;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.tasklisteners.BundleCreationTriggerHandlerExternalTaskListener.LOCK_DURATION;

@Slf4j
@Component
public class BundleCreationTriggerHandler extends BaseExternalTaskHandler {

    private final BundleCreationTriggerService bundleCreationTriggerService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final SystemUpdateUserConfiguration userConfig;
    private final FeatureToggleService featureToggleService;
    @Value("${stitch-bundle.wait-time-in-milliseconds}")
    private Integer waitTime;
    private final NoCacheUserService noCacheUserService;

    public BundleCreationTriggerHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        BundleCreationTriggerService bundleCreationTriggerService,
        ApplicationEventPublisher applicationEventPublisher,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SystemUpdateUserConfiguration userConfig,
        NoCacheUserService noCacheUserService,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.bundleCreationTriggerService = bundleCreationTriggerService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.caseDetailsConverter = caseDetailsConverter;
        this.coreCaseDataService = coreCaseDataService;
        this.userConfig = userConfig;
        this.noCacheUserService = noCacheUserService;
        this.featureToggleService = featureToggleService;
    }

    @SuppressWarnings("java:S2142")
    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (!featureToggleService.isSpringSchedulerEnabled()) {
            Set<CaseDetails> cases = bundleCreationTriggerService.getCases();
            List<Long> ids = cases.stream().map(CaseDetails::getId).sorted().toList();
            log.info("Job '{}' found {} case(s) with ids {}", externalTask.getTopicName(), cases.size(), ids);
            log.info("Initial  waitTime in milliseconds is {}", waitTime);
            String accessToken = noCacheUserService.getAccessToken(userConfig.getUserName(),
                                                                   userConfig.getPassword());
            cases.forEach(caseDetails -> {
                try {
                    boolean isBundleCreated = getIsBundleCreatedForHearingDate(caseDetails.getId());
                    if (!isBundleCreated) {
                        applicationEventPublisher.publishEvent(
                            new BundleCreationTriggerEvent(caseDetails.getId(), accessToken));
                        throttle(cases.size(), waitTime, LOCK_DURATION);
                    } else {
                        log.info("Bundle is already created for {}", caseDetails.getId());
                    }
                } catch (Exception e) {
                    log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
                }
            });
        }
        return new ExternalTaskData();
    }

    boolean getIsBundleCreatedForHearingDate(Long caseId) {
        boolean isBundleCreated = false;
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId).getData());
        List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
        if (caseBundles != null) {
            isBundleCreated =
                !(caseBundles.stream().filter(bundleIdValue -> bundleIdValue.getValue().getBundleHearingDate().isPresent()).filter(bundleIdValue -> bundleIdValue.getValue()
                    .getBundleHearingDate().get().isEqual(caseData.getHearingDate())).toList().isEmpty());
        }
        return isBundleCreated;
    }

}
