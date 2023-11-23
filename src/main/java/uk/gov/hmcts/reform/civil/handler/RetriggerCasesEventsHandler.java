package uk.gov.hmcts.reform.civil.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RETRIGGER_CASES;

@Slf4j
@RequiredArgsConstructor
@Component
public class RetriggerCasesEventsHandler implements BaseExternalTaskHandler {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        log.info("User authentication successful.");
        var caseIdForNotifyRpaOnCaseHandedOffline = readCaseIds("/caseIdForRetrigger.txt");
        updateCaseByEvent(caseIdForNotifyRpaOnCaseHandedOffline, RETRIGGER_CASES);
    }

    private void updateCaseByEvent(List<String> caseIdList, CaseEvent caseEvent) {
        if (caseIdList != null && !caseIdList.isEmpty()) {
            log.info("Retrigger cases started for event: {}", caseEvent);
            caseIdList.forEach(caseId -> {
                try {
                    log.info("Retrigger CaseId: {} started", caseId);
                    var startEventResponse = coreCaseDataService.startUpdate(caseId, caseEvent);

                    Map<String, Object> caseDataMap = coreCaseDataService.getCase(Long.valueOf(caseId)).getData();

                    coreCaseDataService.submitUpdate(caseId, caseDataContent(startEventResponse, caseDataMap));
                    log.info("Retrigger CaseId: {} finished", caseId);

                } catch (FeignException e) {
                    log.error("ERROR Retrigger CaseId: {}", caseId);
                    log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
                    throw e;
                } catch (Exception e) {
                    log.error("ERROR Retrigger CaseId: {}", caseId);
                    log.error(String.format("Updating case data failed: %s", e.getMessage()));
                }
                log.info("Retrigger cases Finished for event: {}", caseEvent);
            });
        } else {
            log.info("List id empty for: {}", caseEvent);
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

    public List<String> readCaseIds(String file) {

        String data = readString(file);
        return Arrays.stream(data.split("[\r\n]+"))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.toList());
    }

    private String readString(String resourcePath) {
        return new String(readBytes(resourcePath), StandardCharsets.UTF_8);
    }

    private byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = RetriggerCasesEventsHandler.class.getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Unable to read resource: " + resourcePath, e);
        }
    }
}
