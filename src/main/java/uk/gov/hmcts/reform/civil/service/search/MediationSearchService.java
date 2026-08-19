package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediationSearchService {

    private final MediationCasesSearchService mediationCasesSearchService;
    private final CaseDetailsConverter caseDetailsConverter;

    public TaskResult<CaseData> getInMediationCsv() {
        return getInMediationCases(false);
    }

    public TaskResult<CaseData> getInMediationJson() {
        return getInMediationCases(true);
    }

    private TaskResult<CaseData> getInMediationCases(boolean carmEnabled) {
        List<CaseData> cases = mediationCasesSearchService.getInMediationCases(carmEnabled).stream()
            .map(this::toCaseData)
            .toList();

        return new ListTaskResult<>(cases, cases.size());
    }

    private CaseData toCaseData(CaseDetails caseDetails) {
        return caseDetailsConverter.toCaseData(caseDetails);
    }
}
