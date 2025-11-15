package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

public class CounterClaimHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {

        if (category == SPEC_CLAIM) {
            builder.atStateRespondentCounterClaimSpec();
        } else {
            builder.atStateRespondentCounterClaim();
        }

        switch (category) {
            case UNSPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.respondent1ClaimResponseType(RespondentResponseType.COUNTER_CLAIM)
                            .respondent2ClaimResponseType(RespondentResponseType.COUNTER_CLAIM);
                        break;
                    default:
                        builder.respondent1ClaimResponseType(RespondentResponseType.COUNTER_CLAIM);
                }
            }
            case SPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
                        break;
                    default:
                        builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
                }
            }
            default -> {
                // Sonar
            }
        }
    }
}
