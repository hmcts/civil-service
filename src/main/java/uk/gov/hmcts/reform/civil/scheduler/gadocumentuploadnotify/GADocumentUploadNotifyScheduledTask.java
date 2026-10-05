package uk.gov.hmcts.reform.civil.scheduler.gadocumentuploadnotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@Component
@RequiredArgsConstructor
@Slf4j
public class GADocumentUploadNotifyScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        Long caseId = caseData.getCcdCaseReference();
        log.info("GADocumentUploadNotifyScheduledTask::accept case {}", caseId);

        coreCaseDataService.triggerGaEvent(caseId, GA_EVIDENCE_UPLOAD_CHECK, Map.of());
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
