package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class BundleCreationTriggerHandler implements BaseExternalTaskHandler {

    private final BundleCreationTriggerService bundleCreationTriggerService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = bundleCreationTriggerService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                boolean isBundleCreated = getIsBundleCreatedForHearingDate(caseDetails.getId());
                if (!isBundleCreated) {
                    applicationEventPublisher.publishEvent(new BundleCreationTriggerEvent(caseDetails.getId()));
                } else {
                    log.info("Bundle is already created for {}", caseDetails.getId());
                }
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }

    boolean getIsBundleCreatedForHearingDate(Long caseId) {
        boolean isBundleCreated = false;
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId).getData());
        List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
        if (caseBundles != null) {
            isBundleCreated =
                !(caseBundles.stream().filter(bundleIdValue -> bundleIdValue.getValue().getBundleHearingDate().isPresent()).filter(bundleIdValue -> bundleIdValue.getValue()
                    .getBundleHearingDate().get().isEqual(caseData.getHearingDate())).collect(Collectors.toList()).isEmpty());
        }
        return isBundleCreated;
    }

}
