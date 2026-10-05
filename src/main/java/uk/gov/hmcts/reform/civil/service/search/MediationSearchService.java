package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.List;
import java.util.Optional;

@Slf4j
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
            .map(this::toOptionalCaseData)
            .flatMap(Optional::stream)
            .toList();

        return new ListTaskResult<>(cases);
    }

    private Optional<CaseData> toOptionalCaseData(CaseDetails caseDetails) {
        try {
            return Optional.ofNullable(caseDetailsConverter.toCaseData(caseDetails));
        } catch (Exception e) {
            log.error("Failed to convert CaseDetails to CaseData for case ID: {}", caseDetails.getId(), e);
            return Optional.empty();
        }
    }
}
