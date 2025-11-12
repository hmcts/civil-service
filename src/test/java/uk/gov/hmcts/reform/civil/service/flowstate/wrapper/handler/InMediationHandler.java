package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class InMediationHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        switch (party) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                builder.addApplicant1MediationInfo().addApplicant1MediationAvailability()
                    .addRespondent1MediationInfo()
                    .addRespondent1MediationAvailability()
                    .addRespondent2MediationInfo()
                    .addRespondent2MediationAvailability();
            default -> builder.addApplicant1MediationInfo().addApplicant1MediationAvailability()
                .addRespondent1MediationInfo()
                .addRespondent1MediationAvailability();
        }

        builder.atStateApplicantProceedAllMediation(party);
    }
}
