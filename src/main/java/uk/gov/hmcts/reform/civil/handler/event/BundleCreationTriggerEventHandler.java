package uk.gov.hmcts.reform.civil.handler.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleData;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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


    /**
     * This method will call bundle API and save required details in case data.
     * If there is any existing bundle then new bundle will be added to existing list of bundles.
     * @param event BundleCreationTriggerEvent.
     */
    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) {
        boolean isBundleCreated = getIsBundleCreatedForHearingDate(event.getCaseId());
        if (isBundleCreated) {
            log.info("Trial Bundle already exists for case {}", event.getCaseId());
            return;
        }
        uk.gov.hmcts.reform.civil.model.bundle.Bundle bundleTest =
            uk.gov.hmcts.reform.civil.model.bundle.Bundle.builder().value(BundleDetails.builder().title(
            "Trial Bundle").id("111")
                                                                                   .stitchStatus("new")
                                                                                   .stitchedDocument(null)
                                                                                   .fileName("Trial Bundle.pdf")
                                                                                   .description("This is trial bundle")
                                                                                   .bundleHearingDate(LocalDate.of(2023, 6, 12))
                                                                                   .stitchedDocument(Document.builder().documentUrl("").documentFileName("Test").build())
                                                                                   .createdOn(LocalDateTime.of(2023, 11, 12, 1, 1, 1))
                                                                                   .build()).build();
        List<uk.gov.hmcts.reform.civil.model.bundle.Bundle> bundlesList = new ArrayList<>();
        bundlesList.add(bundleTest);
        //BundleCreateResponse bundleCreateResponse = bundleCreationService.createBundle(event);
        BundleCreateResponse bundleCreateResponse = BundleCreateResponse.builder()
            .data(BundleData.builder().caseBundles(bundlesList).build()).documentTaskId(100).build();

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

    boolean getIsBundleCreatedForHearingDate(Long caseId) {
        boolean isBundleCreated = false;
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId).getData());
        List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
        isBundleCreated =
            !(caseBundles.stream().filter(bundleIdValue -> bundleIdValue.getValue().getBundleHearingDate().isPresent()).filter(bundleIdValue -> bundleIdValue.getValue()
            .getBundleHearingDate().get().isEqual(caseData.getHearingDate())).collect(Collectors.toList()).isEmpty());
        return isBundleCreated;
    }

    IdValue<Bundle> prepareNewBundle(uk.gov.hmcts.reform.civil.model.bundle.Bundle bundle, CaseData caseData) {
        Bundle result = Bundle.builder()
            .bundleHearingDate(Optional.of(caseData.getHearingDate()))
            .stitchedDocument(Optional.ofNullable(bundle.getValue().getStitchedDocument()))
            .filename(bundle.getValue().getFileName())
            .title(bundle.getValue().getTitle())
            .description(null != bundle.getValue().getDescription()
                             ? Optional.of(bundle.getValue().getDescription()).get() : "")
            .stitchStatus(Optional.ofNullable(bundle.getValue().getStitchStatus()))
            .createdOn(Optional.of(LocalDateTime.now()))
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
