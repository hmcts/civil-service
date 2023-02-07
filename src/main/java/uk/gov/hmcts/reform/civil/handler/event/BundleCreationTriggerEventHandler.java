package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;

@Slf4j
@Service
@AllArgsConstructor
public class BundleCreationTriggerEventHandler {

    @Autowired
    private BundleCreationService bundleCreationService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @EventListener
    public void sendBundleCreationTrigger(BundleCreationTriggerEvent event) throws Exception {
        BundleCreateResponse bundleCreateResponse  = bundleCreationService.createBundle(event);
        log.info("bundle response : " + new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(bundleCreateResponse));
        if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {

            String caseId = event.getCaseId().toString();
            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, CREATE_BUNDLE);
            CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails().getData());
            //moveExistingCaseBundlesToHistoricalBundles(caseData);
            bundleCreateResponse.getData().getCaseBundles().forEach(bundle -> {
                bundle.getValue().setCreatedOn(LocalDateTime.now());
                bundle.getValue().setBundleHearingDate(caseData.getHearingDate());
            });
            caseData.setCaseBundlesInfo(bundleCreateResponse.getData().getCaseBundles());
            CaseDataContent caseContent = getCaseContent(caseData, startEventResponse);
            coreCaseDataService.submitUpdate(event.getCaseId().toString(), caseContent);
            log.info("*** Bundle created successfully for the case id: {}", caseData.getCcdCaseReference());
        }
    }

    private CaseDataContent getCaseContent(CaseData caseData, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary("bundle created")
                       .build())
            .data(caseData)
            .build();
    }

    private void moveExistingCaseBundlesToHistoricalBundles(CaseData caseData) {
        List<Bundle> historicalBundles = new ArrayList<>();
        List<Bundle> existingBundleInformation = caseData.getCaseBundlesInfo();
        List<Bundle> historicBundleInformation = caseData.getHistoricalBundles();
        if (nonNull(existingBundleInformation)) {
            if (nonNull(existingBundleInformation)) {
                historicalBundles.addAll(existingBundleInformation);
            }
            if (nonNull(historicBundleInformation)) {
                historicalBundles.addAll(historicBundleInformation);
            }
            caseData.setHistoricalBundles(historicalBundles);
            //existingBundleInformation.setCaseBundles(null);
        }
    }
}
