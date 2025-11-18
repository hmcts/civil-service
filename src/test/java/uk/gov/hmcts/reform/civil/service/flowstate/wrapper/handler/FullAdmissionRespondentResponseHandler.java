package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class FullAdmissionRespondentResponseHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        builder.atStateRespondentFullAdmissionAfterNotificationAcknowledged();
        switch (category) {
            case UNSPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
                            .respondent2ClaimResponseType(RespondentResponseType.FULL_ADMISSION);
                        break;
                    default:
                        builder.respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION);
                }
            }
            case SPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
                        break;
                    default:
                        builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
                }
            }
            default -> {
                // Sonar
            }
        }
    }
}
