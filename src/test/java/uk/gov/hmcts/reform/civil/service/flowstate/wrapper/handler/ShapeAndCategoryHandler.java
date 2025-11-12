package uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class ShapeAndCategoryHandler extends Handler {

    @Override
    protected void process(CaseDataBuilder builder, MultiPartyScenario party, CaseCategory category) {
        applyShapeAndCategory(builder, party, category);
    }

    private static void applyShapeAndCategory(CaseDataBuilder builder, MultiPartyScenario shape, CaseCategory category) {
        builder.caseAccessCategory(category);

        if (category == SPEC_CLAIM) {
            // Ensure Spec flows have minimal non-null LiP structure to avoid NPEs in predicates
            builder.caseDataLip(CaseDataLiP.builder().applicant1SettleClaim(NO).build());
        }

        switch (shape) {
            case ONE_V_ONE -> builder
                .addApplicant2(NO)
                .addRespondent2(NO)
                .applicant2(null)
                .respondent2(null)
                .respondent2Copy(null)
                .respondent2SameLegalRepresentative(null)
                .respondent2Represented(null);
            case ONE_V_TWO_ONE_LEGAL_REP -> {
                if (category == SPEC_CLAIM) {
                    builder.multiPartyClaimTwoDefendantSameSolicitorsSpec();
                } else {
                    builder.multiPartyClaimOneDefendantSolicitor();
                }
                builder
                    .addApplicant2(NO)
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(YES)
                    .respondent2Represented(YES);
            }
            case ONE_V_TWO_TWO_LEGAL_REP -> {
                if (category == SPEC_CLAIM) {
                    builder.multiPartyClaimTwoDefendantSolicitorsSpec();
                } else {
                    builder.multiPartyClaimTwoDefendantSolicitors();
                }
                builder
                    .addApplicant2(NO)
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2Represented(YES);
            }
            case TWO_V_ONE -> builder
                .multiPartyClaimTwoApplicants()
                .addApplicant2(YES)
                .addRespondent2(NO)
                .respondent2(null)
                .respondent2Copy(null)
                .respondent2SameLegalRepresentative(null)
                .respondent2Represented(null);
            default -> {
                // ONE_V_ONE
            }
        }
    }

}
