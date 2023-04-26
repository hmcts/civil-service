package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
public class RespondentMediationServiceTest {

    @InjectMocks
    RespondentMediationService respondentMediationService;

    @Test
    void whenNotSmallClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void whenResponseTypeIsIncorrect() {
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void shouldSetMediationRequired_whenItsFD_ClaimantAgreeToProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .setClaimantMediationFlag(YES)
            .applicant1ProceedWithClaim(YES)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);
    }

    @Test
    void shouldSetMediationRequired_whenItsFD_ClaimantDisagreeToProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .setClaimantMediationFlag(YES)
            .applicant1ProceedWithClaim(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void shouldSetMediationRequired_whenItsFD_DefendantNotOptedMediation() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .setClaimantMediationFlag(NO)
            .applicant1ProceedWithClaim(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void shouldSetMediationRequired_whenItsFD_NotAgreeToProcceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .setClaimantMediationFlag(NO)
            .applicant1ProceedWithClaim(YES)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void shouldSetMediationRequired_whenItsPA_DefendantHasNotPaidToClaimant() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .setClaimantMediationFlag(YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);

    }

    @Test
    void shouldSetMediationRequired_whenItsPA_DefendantHasPaid_ButClaimantNotAgreeToProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .setClaimantMediationFlag(YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);

    }

    @Test
    void shouldSetMediationRequired_whenItsPA_DefendantHasNotPaid_ClaimantNotAcceptedDefendantPaymentPlan() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .setClaimantMediationFlag(YES)
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);

    }

    @Test
    void shouldSetMediationRequired_whenItsPA_DefendantHasNotPaid_ClaimantAcceptedDefendantPaymentPlan() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .setClaimantMediationFlag(YES)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();

    }

    @Test
    void shouldSetMediationRequired_whenItsPA_DefendantNotAgreedForMediation() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .setClaimantMediationFlag(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();

    }

    @Test
    void shouldSetMediationRequired_whenItsFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1ProceedWithClaim(YES)
            .defendantSingleResponseToBothClaimants(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ADMIT_PAID_ONE_V_ONE);
    }

    @Test
    void shouldSetMediationRequired_whenItsFullAdmissionWithoutMultiParty() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1ProceedWithClaim(YES)
            .defendantSingleResponseToBothClaimants(YES)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

    @Test
    void shouldSetMediationRequired_whenIts2v1Claim() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .applicant1ProceedWithClaimSpec2v1(YES)
            .multiPartyClaimTwoApplicants()
            .defendantSingleResponseToBothClaimants(YES)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_TWO_V_ONE);
    }

    @Test
    void shouldSetMediationRequired_whenIts1V2Claim() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .applicant1ProceedWithClaim(YES)
            .defendantSingleResponseToBothClaimants(NO)
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isEqualTo(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_TWO);
    }

    @Test
    void shouldSetMediationRequired_whenIts1V2ClaimNegativePath() {
        CaseData caseData = CaseDataBuilder.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .respondent2(PartyBuilder.builder().individual().build())
            .applicant1ProceedWithClaim(NO)
            .defendantSingleResponseToBothClaimants(NO)
            .build();

        DefendantResponseShowTag showConditionFlag = respondentMediationService.setMediationRequired(caseData);
        assertThat(showConditionFlag).isNull();
    }

}
