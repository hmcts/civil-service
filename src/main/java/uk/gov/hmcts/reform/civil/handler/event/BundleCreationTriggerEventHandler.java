package uk.gov.hmcts.reform.civil.handler.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;

@Slf4j
@Service
@AllArgsConstructor
public class BundleCreationTriggerEventHandler {

    @Autowired
    private BundleCreationService bundleCreationService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final AssignCategoryId assignCategoryId;

    
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

        List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
        caseBundles.addAll(bundleCreateResponse.getData().getCaseBundles()
                               .stream().map(bundle -> prepareNewBundle(bundle, caseData)
            ).collect(Collectors.toList()));
        CaseDataContent caseContent = prepareCaseContent(caseBundles, startEventResponse);
        coreCaseDataService.submitUpdate(caseId, caseContent);
        coreCaseDataService.triggerEvent(event.getCaseId(), BUNDLE_CREATION_NOTIFICATION);
    }

    IdValue<Bundle> prepareNewBundle(uk.gov.hmcts.reform.civil.model.bundle.Bundle bundle, CaseData caseData) {
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
        result.getStitchedDocument().ifPresent(x -> assignCategoryId.assignCategoryIdToDocument(
                x, DocCategory.BUNDLES.getValue()));
        return new IdValue<>(result.getId(), result);
    }

    CaseDataContent prepareCaseContent(List<IdValue<Bundle>> caseBundles, StartEventResponse startEventResponse) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        List<Element<UploadEvidenceDocumentType>> evidenceUploadedAfterBundle = new ArrayList<>();
        evidenceUploadedAfterBundle.add(ElementUtils.element(UploadEvidenceDocumentType.builder().build()));
        data.put("caseBundles", caseBundles);
        data.put("applicantDocsUploadedAfterBundle", evidenceUploadedAfterBundle);
        data.put("respondentDocsUploadedAfterBundle", evidenceUploadedAfterBundle);
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
