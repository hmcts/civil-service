package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediationFileTransferScheduledTask implements ScheduledTask<CaseData, Long> {

    private final CoreCaseDataService coreCaseDataService;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseData caseData) {
        return caseData.getCcdCaseReference();
    }

    @Override
    public void accept(CaseData caseData) {
        setMediationFileSent(caseData);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }

    private void setMediationFileSent(CaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        String eventSummary = "Updating case - Mediation File sent to MMT successfully";
        String eventDescription = "Updating case - Mediation File sent to MMT successfully";

        Map<String, Object> newCaseData = new HashMap<>();
        newCaseData.put("mediationFileSentToMmt", YesOrNo.YES);

        coreCaseDataService.triggerEvent(
            caseId,
            CaseEvent.UPDATE_CASE_DATA,
            newCaseData,
            eventSummary,
            eventDescription
        );
    }
}
