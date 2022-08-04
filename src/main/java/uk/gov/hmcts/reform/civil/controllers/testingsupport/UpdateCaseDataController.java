package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${testing.support.enabled:false}")
public class UpdateCaseDataController {

    private final CoreCaseDataService coreCaseDataService;

    @PutMapping("/testing-support/case/{caseId}")
    public void updateCaseData(@PathVariable("caseId") Long caseId, @RequestBody Map<String, Object> caseDataMap) {
        try {
            var startEventResponse = coreCaseDataService.startUpdate(caseId.toString(), UPDATE_CASE_DATA);
            coreCaseDataService.submitUpdate(caseId.toString(), caseDataContent(startEventResponse, caseDataMap));
        } catch (FeignException e) {
            log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    public void updateCaseDataSpecData(Long caseId, CaseData caseData) {
        System.out.println(" inside updateCaseDataSpecData ");
        ObjectMapper mapper = new ObjectMapper();
        //Map<String, Object> caseDataMap = mapper.convertValue(caseData, Map.class);

        //Map<String, Object> caseDataMap = mapper.convertValue(caseData, Map.class);
        try {
            var startEventResponse = coreCaseDataService.startUpdate(caseId.toString(), UPDATE_CASE_DATA);
            System.out.println("startEventResponse " + startEventResponse);

            coreCaseDataService.submitUpdate(caseId.toString(), caseDataContentSpec(startEventResponse, caseData));
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

    private CaseDataContent caseDataContentSpec(StartEventResponse startEventResponse, CaseData caseData) {


        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(caseData)
            .build();
    }

}
