package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class DivergentRespondentResponseHandler extends Handler {

    public DivergentRespondentResponseHandler() {
        super();
    }

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {

        switch (category) {
            case UNSPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP -> builder.atState1v2SameSolicitorDivergentResponse(
                        RespondentResponseType.PART_ADMISSION,
                        RespondentResponseType.FULL_ADMISSION
                    );
                    case ONE_V_TWO_TWO_LEGAL_REP -> builder.atState1v2DivergentResponse(
                        RespondentResponseType.PART_ADMISSION,
                        RespondentResponseType.FULL_ADMISSION
                    );
                    default -> {
                        // Sonar
                    }
                }
            }
            case SPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP -> builder.atState1v2SameSolicitorDivergentResponseSpec(
                        RespondentResponseTypeSpec.PART_ADMISSION,
                        RespondentResponseTypeSpec.FULL_ADMISSION
                    );
                    case ONE_V_TWO_TWO_LEGAL_REP -> builder.atState1v2DifferentSolicitorDivergentResponseSpec(
                        RespondentResponseTypeSpec.PART_ADMISSION,
                        RespondentResponseTypeSpec.FULL_ADMISSION
                    );
                    default -> {
                        // Sonar
                    }
                }
            }

            default -> {
                // Sonar
            }
        }
    }
}
