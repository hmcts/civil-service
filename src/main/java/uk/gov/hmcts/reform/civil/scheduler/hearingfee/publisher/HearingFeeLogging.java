package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface HearingFeeLogging {

    default String getLogMessage(String prefix, String eventName, CaseData caseData) {
        return String.format(
            "%s%s current case status %s, Case Id %s",
            prefix,
            eventName,
            caseData.getCcdState().name(),
            caseData.getCcdCaseReference().toString()
        );
    }
}
