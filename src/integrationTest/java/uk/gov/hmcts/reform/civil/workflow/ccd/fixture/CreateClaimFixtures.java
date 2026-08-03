package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_ONE_ORGANISATION;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_TWO_ORGANISATION;

public final class CreateClaimFixtures {

    private static final String CREATE_CLAIM_START = "create-claim-start";

    private CreateClaimFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CREATE_CLAIM_START);
    }

    public static CaseData representedOneVOneClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_ONE_ORGANISATION,
                RESPONDENTSOLICITORONE,
                "respondent-one-policy"
            ))
            .build());
    }

    public static CaseData representedTwoVOneClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .multiPartyClaimTwoApplicants()
            .applicant2(new PartyBuilder().individual("Jason").build())
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_ONE_ORGANISATION,
                RESPONDENTSOLICITORONE,
                "respondent-one-policy"
            ))
            .build());
    }

    public static CaseData representedTwoSolicitorClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_ONE_ORGANISATION,
                RESPONDENTSOLICITORONE,
                "respondent-one-policy"
            ))
            .respondent2OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_TWO_ORGANISATION,
                RESPONDENTSOLICITORTWO,
                "respondent-two-policy"
            ))
            .build());
    }

    public static CaseData representedSameSolicitorClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2SameLegalRepresentative(YES)
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_ONE_ORGANISATION,
                RESPONDENTSOLICITORONE,
                "respondent-one-policy"
            ))
            .respondent2OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_TWO_ORGANISATION,
                RESPONDENTSOLICITORTWO,
                "respondent-two-policy"
            ))
            .build());
    }

    public static CaseData oneVOneLipClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .addRespondent2(NO)
            .respondent1Represented(NO)
            .respondent1OrgRegistered(NO)
            .respondent1OrganisationPolicy(null)
            .build());
    }

    public static CaseData mixedRepresentationClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .atStateClaimDraft()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1OrganisationPolicy(ClaimLifecycleFixtures.registeredPolicy(
                RESPONDENT_ONE_ORGANISATION,
                RESPONDENTSOLICITORONE,
                "respondent-one-policy"
            ))
            .respondent2Represented(NO)
            .respondent2OrgRegistered(NO)
            .respondent2OrganisationPolicy(null)
            .build());
    }

    public static CaseData twoLipClaimDraft() {
        return completeDraft(CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantLips()
            .respondent2(new PartyBuilder().individual().build())
            .build());
    }

    private static CaseData completeDraft(CaseData caseData) {
        return caseData.toBuilder()
            .ccdCaseReference(CASE_REFERENCE)
            .caseAccessCategory(null)
            .claimStarted(YES)
            .uiStatementOfTruth(ClaimLifecycleFixtures.statementOfTruth())
            .build();
    }
}
