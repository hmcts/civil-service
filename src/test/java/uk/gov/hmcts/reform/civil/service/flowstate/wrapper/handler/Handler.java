package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

@Slf4j
public abstract class Handler {

    private Handler next;

    public Handler setNext(Handler next) {
        this.next = next;
        return next;
    }

    public void handle(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        process(builder, party, category);

        if (next != null) {
            next.handle(builder, party, category);
        }
    }

    protected abstract void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category);

}
