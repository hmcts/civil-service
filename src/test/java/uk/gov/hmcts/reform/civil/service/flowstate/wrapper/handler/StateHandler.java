package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

@Slf4j
public class StateHandler extends Handler {

    private final FlowState.Main state;

    public StateHandler(FlowState.Main state) {
        super();
        this.state = state;
    }

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        try {
            builder.atState(state);
        } catch (IllegalArgumentException e) {
            log.warn("State {} unsupported by CaseDataBuilder", state);
        }
    }

}
