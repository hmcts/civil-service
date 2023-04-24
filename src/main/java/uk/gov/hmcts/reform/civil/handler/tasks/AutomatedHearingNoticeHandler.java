package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_PARTIES;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutomatedHearingNoticeHandler implements BaseExternalTaskHandler {

    private final RuntimeService runtimeService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        System.out.println("Automated Hearing Notice Scheduler");

        var hearingCaseIdsMap = Map.of(
            Long.parseLong("1682278521572045"), "HER111111111",
            Long.parseLong("1681908201253661"), "HER222222222",
            Long.parseLong("1682266182841536"), "HER333333333"
        );

        hearingCaseIdsMap.keySet().stream().forEach(
            caseId -> {
                try {
                    runtimeService.createMessageCorrelation(NOTIFY_HEARING_PARTIES.name())
                        .setVariable("caseId", caseId)
                        .setVariable("hearingId", hearingCaseIdsMap.get(caseId))
                        .setVariable("triggeredViaScheduler", true)
                        .correlateStartMessage();
                } catch (Exception e) {
                    log.error("Sending hearing notice failed for caseId: %d hearingId %s: %s",
                              caseId, hearingCaseIdsMap.get(caseId), e);
                }
            }
        );

        runtimeService.setVariable(externalTask.getProcessInstanceId(), "totalToProcess", 100);
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
