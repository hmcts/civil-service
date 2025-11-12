package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class ProceedHandler extends Handler {

    private final YesOrNo yesOrNo;

    public ProceedHandler(YesOrNo yesOrNo) {
        super();
        this.yesOrNo = yesOrNo;
    }

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {

        switch (category) {
            case UNSPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(yesOrNo)
                            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(yesOrNo);
                        break;
                    case TWO_V_ONE:
                        builder.applicant1ProceedWithClaimMultiParty2v1(yesOrNo)
                            .applicant2ProceedWithClaimMultiParty2v1(yesOrNo);
                        break;
                    default:
                        builder.applicant1ProceedWithClaim(yesOrNo);
                }
            }
            case SPEC_CLAIM -> {
                switch (party) {
                    case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                        builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(yesOrNo)
                            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(yesOrNo);
                        break;
                    case TWO_V_ONE:
                        builder.applicant1ProceedWithClaimSpec2v1(yesOrNo);
                        break;
                    default:
                        builder.applicant1ProceedWithClaim(yesOrNo);
                }
            }
            default -> {
                // Sonar
            }
        }
    }
}
