package uk.gov.hmcts.reform.civil.handler.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;

@Slf4j
@Service
@AllArgsConstructor
public class BundleCreationTriggerEventHandler {

    private BundleCreationService bundleCreationService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;


    /**
     * This method will call bundle API and save required details in case data.
     * If there is any existing bundle then new bundle will be added to existing list of bundles.
     * @param event BundleCreationTriggerEvent.
     */
    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) {
        BundleCreateResponse bundleCreateResponse = bundleCreationService.createBundle(event);
        String caseId = event.getCaseId().toString();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, CREATE_BUNDLE);
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails().getData());

        List<uk.gov.hmcts.reform.civil.model.bundle.Bundle> bundles = bundleCreateResponse.getData().getCaseBundles();
        Optional<uk.gov.hmcts.reform.civil.model.bundle.Bundle> lastCreatedBundle = bundles.stream()
            .max(Comparator.comparing(bundle -> bundle.getValue().getCreatedOn()));
        if (lastCreatedBundle.isPresent()) {
            log.info("inside if");
            BundleDetails bundle = lastCreatedBundle.get().getValue();
            log.info("Bundle ID: {}", bundle.getId());
            log.info("Bundle Title: {}", bundle.getTitle());
            log.info("Bundle File Name: {}", bundle.getFileName());
            log.info("Bundle Created On: {}", bundle.getCreatedOn());
            log.info("Bundle Stitch Status: {}", bundle.getStitchStatus());
            log.info("Bundle Stitched Document: {}", bundle.getStitchedDocument());
            log.info("Bundle Hearing Date: {}", bundle.getBundleHearingDate());
            log.info("Bundle Description: {}", bundle.getDescription());
        }
        else{
            log.info("inside else");
        }

        YesOrNo hasBundleErrors = lastCreatedBundle
            .map(bundle -> "FAILED".equalsIgnoreCase(bundle.getValue().getStitchStatus()) ? YesOrNo.YES : null)
            .orElse(null);

        log.info("hasBundleErrors: {}", hasBundleErrors);

        if (hasBundleErrors == null) {
            List<IdValue<Bundle>> caseBundles = new ArrayList<>(caseData.getCaseBundles());
            CaseData finalCaseData = caseData;
            caseBundles.addAll(bundleCreateResponse.getData().getCaseBundles()
                                   .stream().map(bundle -> prepareNewBundle(bundle, finalCaseData)).toList());

            CaseDataContent caseContent = prepareCaseContent(caseBundles, startEventResponse);
            coreCaseDataService.submitUpdate(caseId, caseContent);
            coreCaseDataService.triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
        } else {
            caseData = caseData.toBuilder().bundleError(hasBundleErrors).build();
            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .summary("bundle failed")
                           .build())
                .data(caseData)
                .build();
            coreCaseDataService.submitUpdate(caseId, caseDataContent);
        }
    }

    public IdValue<Bundle> prepareNewBundle(uk.gov.hmcts.reform.civil.model.bundle.Bundle bundle, CaseData caseData) {
        Bundle result = Bundle.builder()
            .bundleHearingDate(Optional.of(caseData.getHearingDate()))
            .stitchedDocument(Optional.ofNullable(bundle.getValue().getStitchedDocument()))
            .fileName(bundle.getValue().getFileName())
            .title(bundle.getValue().getTitle())
            .description(null != bundle.getValue().getDescription()
                             ? Optional.of(bundle.getValue().getDescription()).get() : "")
            .stitchStatus(Optional.ofNullable(bundle.getValue().getStitchStatus()))
            .createdOn(Optional.of(LocalDateTime.now(ZoneId.of("Europe/London"))))
            .id(bundle.getValue().getId()).build();
        return new IdValue<>(result.getId(), result);
    }

    CaseDataContent prepareCaseContent(List<IdValue<Bundle>> caseBundles, StartEventResponse startEventResponse) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        List<Element<UploadEvidenceDocumentType>> evidenceUploadedAfterBundle = new ArrayList<>();
        evidenceUploadedAfterBundle.add(ElementUtils.element(UploadEvidenceDocumentType.builder().build()));
        data.put("caseBundles", caseBundles);
        data.put("applicantDocsUploadedAfterBundle", evidenceUploadedAfterBundle);
        data.put("respondentDocsUploadedAfterBundle", evidenceUploadedAfterBundle);
        data.put("bundleError", null);
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary("bundle created")
                       .build())
            .data(data)
            .build();
    }
}
