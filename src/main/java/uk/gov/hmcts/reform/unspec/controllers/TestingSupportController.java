package uk.gov.hmcts.reform.unspec.controllers;

import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.TESTING_SUPPORT_RESET_BUSINESS_PROCESS;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${testing.support.enabled:false}")
public class TestingSupportController {

    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/testing-support/case/{caseId}/business-process/reset")
    public void resetBusinessProcess(@PathVariable("caseId") Long caseId) {
        Map<String, Object> data = new HashMap<>();
        data.put("businessProcess", BusinessProcess.builder().status(FINISHED).build());
        try {
            coreCaseDataService.triggerEvent(caseId, TESTING_SUPPORT_RESET_BUSINESS_PROCESS, data);
        } catch (FeignException e) {
            log.error(String.format("Resetting business process failed: %s", e.contentUTF8()));
            throw e;
        }
    }
}
