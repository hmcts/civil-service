package uk.gov.hmcts.reform.civil.scheduler.fulladmitpayimmediatelynopayfromdef;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask::accept case {}", caseId);

        try {
            setFullAdmitNoPaymentSchedulerProcessed(caseId);
            applicationEventPublisher.publishEvent(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(
                caseId));
        } catch (Exception e) {
            log.error("Updating case with id: '{}' failed", caseId, e);
        }
    }

    protected void setFullAdmitNoPaymentSchedulerProcessed(Long caseId) {
        String eventSummary = "Updating case - Full Admit No Payment Dashboard notification created successfully";
        String eventDescription = "Updating case - Full Admit No Payment Dashboard notification created successfully";

        Map<String, Object> newCaseData = new HashMap<>();
        newCaseData.put("fullAdmitNoPaymentSchedulerProcessed", YesOrNo.YES);

        coreCaseDataService.triggerEvent(
            caseId,
            UPDATE_CASE_DATA,
            newCaseData,
            eventSummary,
            eventDescription
        );
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
