package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingInformation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

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
        log.info("bundle response : " + new ObjectMapper().writeValueAsString(bundleCreateResponse));
        if (null != bundleCreateResponse && null != bundleCreateResponse.getData() && null != bundleCreateResponse.getData().getCaseBundles()) {

            String caseId = event.getCaseId().toString();
            StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, CREATE_BUNDLE);
            CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails().getData());
            caseData.toBuilder().bundleInformation(BundlingInformation.builder().caseBundles(bundleCreateResponse.getData().getCaseBundles())
                                                       .historicalBundles(caseData.getBundleInformation().getHistoricalBundles())
                                                       .bundleConfiguration(bundleCreateResponse.data.getBundleConfiguration())
                                                       .bundleCreationDateAndTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                                                                      .format(ZonedDateTime.now(ZoneId.of("Europe/London"))).toString())
                                                       .bundleHearingDateAndTime(null != bundleCreateResponse.getData().getHearingDate()
                                                                                     && null != bundleCreateResponse.getData().getHearingDate()
                                                                                     ? bundleCreateResponse.getData().getHearingDate() : "")
                                                       .build());
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
            .data(new HashMap<>(startEventResponse.getCaseDetails().getData()))
            .build();
    }
}
