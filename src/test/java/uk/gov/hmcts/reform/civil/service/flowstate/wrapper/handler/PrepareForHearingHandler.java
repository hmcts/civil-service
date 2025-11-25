package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

public class PrepareForHearingHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        // Mirror the CaseDetailsBuilder.atStateDecisionOutcome() minimums
        if (builder.build().getHearingDate() == null) {
            builder.hearingDate(LocalDate.now());
        }
        builder.hearingReferenceNumber("HRN-12345");
    }
}
