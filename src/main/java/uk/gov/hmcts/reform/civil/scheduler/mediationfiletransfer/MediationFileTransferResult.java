package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import java.util.List;

public record MediationFileTransferResult(
    List<String> caseIds,
    List<String> succeededCaseIds,
    List<MediationFileTransferResult.FailedCase> failedCases,
    boolean abortedEarly,
    String abortReason
) {

    public int totalCases() {
        return caseIds.size();
    }

    public record FailedCase(String caseId, Exception exception) {
    }
}
