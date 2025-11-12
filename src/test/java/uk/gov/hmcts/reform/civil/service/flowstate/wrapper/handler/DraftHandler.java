package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class DraftHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        builder.atStateClaimDraft();
    }
}
