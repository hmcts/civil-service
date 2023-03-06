package uk.gov.hmcts.reform.civil.controllers.support;

import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CASE_HANDED_OFFLINE;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
public class ResendingRoboticsDataController {

    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/support/rpacontinuousfeed/{caseId}")
    public void updateCaseDataForRPAContinuousfeed(@PathVariable("caseId") Long caseId) {
        try {
            var startEventResponse = coreCaseDataService.startUpdate(caseId.toString(), NOTIFY_RPA_ON_CONTINUOUS_FEED);

            Map<String, Object> caseDataMap  = coreCaseDataService.getCase(caseId).getData();

            coreCaseDataService.submitUpdate(caseId.toString(), caseDataContent(startEventResponse, caseDataMap));

        } catch (FeignException e) {
            log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    @PostMapping("/support/rpafeedonoffline/{caseId}")
    public void updateCaseDataForRPAFeedOnOffline(@PathVariable("caseId") Long caseId) {
        try {
            var startEventResponse = coreCaseDataService.startUpdate(caseId.toString(), NOTIFY_RPA_ON_CASE_HANDED_OFFLINE);

            Map<String, Object> caseDataMap  = coreCaseDataService.getCase(caseId).getData();

            coreCaseDataService.submitUpdate(caseId.toString(), caseDataContent(startEventResponse, caseDataMap));

        } catch (FeignException e) {
            log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Map<String, Object> caseDataMap) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.putAll(caseDataMap);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }
}
