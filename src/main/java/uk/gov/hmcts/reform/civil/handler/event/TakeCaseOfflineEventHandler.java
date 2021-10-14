package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakeCaseOfflineEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @EventListener
    public void takeCaseOffline(TakeCaseOfflineEvent event) {
        String caseId = event.getCaseId().toString();
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, TAKE_CASE_OFFLINE);
        CaseData caseData = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails().getData());
        CaseDataContent caseContent = getCaseContent(caseData, startEventResponse);
        coreCaseDataService.submitUpdate(caseId, caseContent);
    }

    private CaseDataContent getCaseContent(CaseData caseData, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary("RPA Reason: Claim dismissed after no response from applicant past response deadline.")
                       .build())
            .data(new HashMap<>(startEventResponse.getCaseDetails().getData()))
            .build();
    }
}
