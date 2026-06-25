package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.HearingFeePublisherProvider;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
@Slf4j
@AllArgsConstructor
public class HearingFeeSchedulerTask implements ScheduledTask {

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final HearingFeePublisherProvider hearingFeePublisherProvider;

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("HearingFeeSchedulerTask::accept case {}", caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        hearingFeePublisherProvider.provide(caseData).accept(caseId);
    }
}
