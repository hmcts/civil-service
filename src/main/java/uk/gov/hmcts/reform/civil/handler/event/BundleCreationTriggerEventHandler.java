package uk.gov.hmcts.reform.civil.handler.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @throws Exception If there is any issue calling bundle API then exception will be thrown.
     */
    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) throws Exception {
        BundleCreateResponse bundleCreateResponse  = bundleCreationService.createBundle(event);
        if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {

            String caseId = event.getCaseId().toString();
            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, CREATE_BUNDLE);
            CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails().getData());
            List<IdValue<Bundle>> newBundles = new ArrayList<>();

            bundleCreateResponse.getData().getCaseBundles().forEach(bundle -> {
                Bundle bundle1 =
                    Bundle.builder().bundleHearingDate(Optional.of(caseData.getHearingDate()))
                        .stitchedDocument(Optional.ofNullable(bundle.getValue().getStitchedDocument()))
                        .filename(bundle.getValue().getFileName())
                        .title(bundle.getValue().getTitle())
                        .description(Optional.ofNullable(bundle.getValue().getDescription()).toString())
                        .stitchStatus(Optional.ofNullable(bundle.getValue().getStitchStatus()))
                        .createdOn(Optional.of(LocalDateTime.now()))
                        .id(bundle.getValue().getId()).build();
                newBundles.add(new IdValue<>(bundle.getValue().getId(),
                                             bundle1));

            });
            List<IdValue<Bundle>> caseBundles = caseData.getCaseBundles();
            caseBundles.addAll(newBundles);
            CaseDataContent caseContent = getCaseContent(caseBundles, startEventResponse);
            coreCaseDataService.submitUpdate(caseId, caseContent);
        }
    }

    private CaseDataContent getCaseContent(List<IdValue<Bundle>> caseBundles, StartEventResponse startEventResponse) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("caseBundles", caseBundles);
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
