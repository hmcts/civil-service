package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class BundleCreationTriggerHandler extends BaseExternalTaskHandler {

    private final BundleCreationTriggerService bundleCreationTriggerService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    @Value("${stitch-bundle.batchsize:500}")
    private int batchSize;
    @Value("${stitch-bundle.wait-time-mins:10}")
    private int waitTime;

    @SuppressWarnings("java:S2142")
    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = bundleCreationTriggerService.getCases();
        List<Long> ids = cases.stream().map(CaseDetails::getId).sorted().toList();
        log.info("Job '{}' found {} case(s) with ids {}", externalTask.getTopicName(), cases.size(), ids);

        cases.forEach(caseDetails -> {
            try {
                int count = 0;
                int batchCount = 1;
                boolean isBundleCreated = getIsBundleCreatedForHearingDate(caseDetails.getId());
                if (!isBundleCreated) {
                    count++;
                    applicationEventPublisher.publishEvent(new BundleCreationTriggerEvent(caseDetails.getId()));
                    if (count == batchSize) {
                        log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, batchSize, waitTime);
                        TimeUnit.MINUTES.sleep(waitTime);
                        count = 0;
                        batchCount++;
                    }
                    log.info("Process case reference {}, batch {}, count {}", caseDetails.getId(), batchCount, count);
                } else {
                    log.info("Bundle is already created for {}", caseDetails.getId());
                }
            } catch (InterruptedException e) {
                log.error("Error processing caseRef {} and error is {}", caseDetails.getId(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return ExternalTaskData.builder().build();
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
