package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.CounterClaimConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitAlreadyPaidConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidFullConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidLessConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPayImmediatelyConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.RepayPlanConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.SpecResponse1v2DivergentText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.SpecResponse2v1DifferentText;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

/**
 * config for testing implementations of RespondToClaimConfirmationTextSpecGenerator.
 */
class RespondToClaimConfirmationTextSpecGeneratorTest
        implements CaseDataToTextGeneratorTest
        .CaseDataToTextGeneratorIntentionConfig<RespondToClaimConfirmationTextSpecGenerator> {

    @Test
    void shouldGenerateCounterClaimConfirmationText() {
        CaseData caseData = getCounterClaim();

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("You've chosen to counterclaim - this means your defence cannot continue online.")
                        .contains("Download form N9B")
        );
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenOneVOneFullAdmit() {
        CaseData caseData = getFullAdmitPayImmediately();
        caseData.setIsRespondent1(YesOrNo.YES);
        caseData.setIsRespondent2(null);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new SpecResponse1v2DivergentText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new SpecResponse2v1DifferentText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenSameSolicitorSameResponse() {
        CaseData caseData = sameSolicitorSamePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;400")
        );
        assertThat(new SpecResponse1v2DivergentText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowDivergentConfirmation_whenSameSolicitorResponsesDiffer() {
        CaseData caseData = sameSolicitorDivergentCounterClaimAndFullAdmit();

        assertThat(new SpecResponse1v2DivergentText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("The defendants have chosen different responses")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenTwoVOneIncludesBothApplicants() {
        CaseData caseData = twoVOneFullAdmitSetDate();

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );
        String bothApplicants = caseData.getApplicant1().getPartyName()
            + " and " + caseData.getApplicant2().getPartyName();

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains(bothApplicants)
                .doesNotContain("You believe you owe")
        );
        assertThat(new SpecResponse2v1DifferentText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGeneratePartialAdmissionSetDateConfirmationText() {
        CaseData caseData = getPartialAdmitSetDate();
        String applicantName = caseData.getApplicant1().getPartyName();
        String paymentDate = DateFormatHelper.formatLocalDate(
                caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
                DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("You believe you owe &#163;" + caseData.getRespondToAdmittedClaimOwingAmountPounds())
                        .contains(applicantName)
                        .contains("your offer to pay by " + paymentDate)
                        .contains(String.format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference()))
                        .contains("The court will decide how you must pay")
        );
    }

    @Test
    void shouldGenerateTextForDifferentResponsesPerClaimant() {
        CaseData caseData = get2v1DifferentResponseCase().getFirst();

        assertThat(new SpecResponse2v1DifferentText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("The defendant has chosen different responses for each claimant and the claim cannot continue online.")
                        .contains("Download form N9A")
                        .contains("Download form N9B")
                        .contains("County Court Business Centre")
        );
    }

    @Test
    void shouldGenerateCounterClaimConfirmationTextWhenBothResponsesAreTheSame() {
        CaseData caseData = getCounterClaim();
        caseData.setRespondent2(new Party());
        caseData.setRespondentResponseIsSame(YesOrNo.YES);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text).contains("Download form N9B")
        );
    }

    @Test
    void shouldGenerateCounterClaimConfirmationTextWhenSecondRespondentAlsoCounterclaims() {
        CaseData caseData = getCounterClaim();
        caseData.setRespondent2(new Party());
        caseData.setRespondentResponseIsSame(YesOrNo.NO);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text).contains("Download form N9B")
        );
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent2CounterClaimsAndRespondent1FullAdmit() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent1FullAdmitAndRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent1FullAdmitRespondsAfterRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("your offer to pay by " + paymentDate)
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent1FullAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent1FullAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondent2ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .doesNotContain("You believe you owe")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent1FullAdmitRespondsAfterRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent2CounterClaimsFirstBeforeFullAdmit() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyFullAdmitCounterClaim(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2027, 6, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);
        return caseData;
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent2CounterClaimsAndRespondent1PartAdmit() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent1PartAdmitAndRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent1PartAdmitRespondsAfterRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;275")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent1PartAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.YES
        );

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent1PartAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;275")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent1PartAdmitRespondsAfterRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .contains("You believe you owe &#163;275")
            );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent2CounterClaimsFirstBeforePartAdmit() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyPartAdmitCounterClaim(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(275))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2027, 10, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);
        return caseData;
    }

    @Test
    void shouldReturnEmptyForDifferentResponsesPerClaimantWhenSingleResponseProvided() {
        CaseData caseData = get2v1DifferentResponseCase().getFirst();
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.YES);

        assertThat(new SpecResponse2v1DifferentText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForDifferentResponsesPerClaimantWhenResponsesMatch() {
        Party applicant1 = new Party();
        Party applicant2 = new Party();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(applicant1);
        caseData.setApplicant2(applicant2);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new SpecResponse2v1DifferentText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForPartialAdmissionSetDateWhenRequiredFieldIsMissing() {
        CaseData caseData = getPartialAdmitSetDate();
        caseData.setTotalClaimAmount(null);

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGeneratePartialAdmissionSetDateTextForBilingualMultiClaimantMediationCase() {
        CaseData caseData = getPartialAdmitSetDate();
        caseData.setApplicant1(new PartyBuilder().individual("Alice").build());
        caseData.setApplicant2(new PartyBuilder().company().build());
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("Mr. Alice Rambo and Company ltd")
                        .contains("We will let you know when the claimant responds.")
                        .contains("accept your offer")
                        .contains("We'll ask if they want to try mediation.")
                        .contains("If they do not want to try mediation the court will review the case for the full amount of &#163;"
                                + caseData.getTotalClaimAmount())
        );
    }

    @Test
    void shouldGeneratePartialAdmissionSetDateTextForRepresentedApplicantNameEndingWithS() {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.COMPANY);
        applicant1.setCompanyName("Evans");
        applicant1.setPartyName("Evans");
        CaseData caseData = getPartialAdmitSetDate();
        caseData.setApplicant1(applicant1);
        caseData.setTotalClaimAmount(caseData.getRespondToAdmittedClaimOwingAmountPounds());

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("Contact Evans' legal representative if you need details on how to pay.")
                        .doesNotContain("and your explanation of why you do not owe the full amount.")
        );
    }

    private CaseData getFullAdmitAlreadyPaidCase() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .totalClaimAmount(BigDecimal.valueOf(1000)).build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.YES);
        return caseData;
    }

    private CaseData getPartialAdmitSetDate() {
        return getPartialAdmitSetDate(false);
    }

    private CaseData getPartialAdmitSetDate(boolean isLipVLR) {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusMonths(1);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .totalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)))
                .respondent1Represented(isLipVLR ? YesOrNo.YES : YesOrNo.NO)
                .applicant1Represented(isLipVLR ? YesOrNo.NO : YesOrNo.YES).build();
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    private CaseData getPartialAdmitPayImmediately() {
        return getPartialAdmitPayImmediately(false);
    }

    private CaseData getPartialAdmitPayImmediately(boolean isLipVLR) {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .respondent1Represented(isLipVLR ? YesOrNo.YES : YesOrNo.NO)
                .applicant1Represented(isLipVLR ? YesOrNo.NO : YesOrNo.YES)
                .build();
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    private CaseData getFullAdmitPayImmediately() {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .applicant1Represented(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .respondToAdmittedClaimOwingAmountPounds(admitted).build();
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    private CaseData getFullAdmitRepayPlan() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .applicant1Represented(YesOrNo.YES)
                .defenceAdmitPartPaymentTimeRouteRequired(
                        RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN).build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        return caseData;
    }

    private CaseData getFullAdmitRepayPlanLiPvLr() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .applicant1Represented(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                        RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        return caseData;
    }

    private CaseData getPartialAdmitRepayPlan() {
        return getPartialAdmitRepayPlan(false);
    }

    private CaseData getPartialAdmitRepayPlan(boolean isLipVLR) {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(
                        RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .respondent1Represented(isLipVLR ? YesOrNo.YES : YesOrNo.NO)
                .applicant1Represented(isLipVLR ? YesOrNo.NO : YesOrNo.YES)
                .build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        return caseData;
    }

    private CaseData getFullAdmitAlreadyPaid() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.YES);
        return caseData;

    }

    private CaseData getFullAdmitPayBySetDate() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1Represented(YesOrNo.YES)
                .defenceAdmitPartPaymentTimeRouteRequired(
                        RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE).build();
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        return caseData;
    }

    private CaseData getFullAdmitPayBySetDateLipVLr() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1Represented(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(
                        RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE).build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenWillPay);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    private CaseData getPartialAdmitPayFull(boolean isLipVLR) {
        BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
        BigDecimal howMuchWasPaid = new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount));
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .totalClaimAmount(totalClaimAmount)
                .respondent1Represented(isLipVLR ? YesOrNo.YES : YesOrNo.NO)
                .applicant1Represented(isLipVLR ? YesOrNo.NO : YesOrNo.YES)
                .isRespondent1(YesOrNo.YES)
                .build();
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(howMuchWasPaid);
        caseData.setRespondToAdmittedClaim(respondToClaim);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
        return caseData;
    }

    private CaseData getPartialAdmitPayFull() {
        return getPartialAdmitPayFull(false);
    }

    private CaseData getPartialAdmitPayLess() {
        return getPartialAdmitPayLess(false);
    }

    private CaseData getPartialAdmitPayLess(boolean isLipVLR) {
        BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
        BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .totalClaimAmount(totalClaimAmount)
                .isRespondent1(YesOrNo.YES)
                .respondent1Represented(isLipVLR ? YesOrNo.YES : YesOrNo.NO)
                .applicant1Represented(isLipVLR ? YesOrNo.NO : YesOrNo.YES)
                .build();
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(howMuchWasPaid);
        caseData.setRespondToAdmittedClaim(respondToClaim);
        return caseData;
    }

    @Test
    void shouldShowCurrentDefendantInstalments_whenOtherDefendantChoseSetDate() {
        CaseData caseData = multipartyCaseWithDifferentPaymentMethods(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("you've suggested paying by instalments")
        );
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowCurrentDefendantSetDate_whenOtherDefendantChoseInstalments() {
        CaseData caseData = multipartyCaseWithDifferentPaymentMethods(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setIsRespondent1(YesOrNo.NO);
        caseData.setIsRespondent2(YesOrNo.YES);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;500")
        );
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowCurrentDefendantPayImmediately_whenOtherDefendantChoseInstalments() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setIsRespondent1(YesOrNo.NO);
        caseData.setIsRespondent2(YesOrNo.YES);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(400));
        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.now().plusDays(3));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenBothFullAdmitAndRespondent1PaysImmediately() {
        CaseData caseData = multipartyBothFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenBothFullAdmitAndRespondent2PaysByInstalments() {
        CaseData caseData = multipartyBothFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenBothFullAdmitAndRespondent2PaysBySetDateFirst() {
        CaseData caseData = multipartyBothFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondent1ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("your offer to pay by " + paymentDate)
        );
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenBothFullAdmitAndRespondent1PaysByInstalmentsFirst() {
        CaseData caseData = multipartyBothFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondent2ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyBothFullAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2026, 5, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2026, 5, 10));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenBothPartAdmitAndRespondent1PaysImmediately() {
        CaseData caseData = multipartyBothPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenBothPartAdmitAndRespondent2PaysBySetDate() {
        CaseData caseData = multipartyBothPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;250")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenBothPartAdmitAndRespondent2PaysByInstalmentsFirst() {
        CaseData caseData = multipartyBothPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenBothPartAdmitAndRespondent1PaysByInstalmentsFirst() {
        CaseData caseData = multipartyBothPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyBothPartAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(250));
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2026, 6, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2026, 7, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent1FullAdmitAndRespondent2PartAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent2PartAdmitAndRespondent1FullAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;400")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent2PartAdmitRespondsFirstBeforeFullAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent1FullAdmitRespondsFirstBeforePartAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondent2ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyFullAdmitPartAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(400));
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2026, 8, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2026, 9, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent1PartAndRespondent2Full() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent2FullAfterRespondent1Part() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        // Stale generic from first defendant must not divert R2 full-admit confirmation
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .doesNotContain("You believe you owe")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent2FullRespondsFirst() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent1PartRespondsAfterRespondent2Full() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_ADMISSION);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent2FullAdmitRespondsFirstBeforePartAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondent1ResponseDate(null);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent1PartAdmitRespondsFirstBeforeFullAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;350")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent1FullAdmitAndRespondent2FullDefence() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent2FullDefenceAfterRespondent1FullAdmit() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        // Stale payment route / generic from admitting defendant must not produce admit confirmation
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_ADMISSION);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent1FullAdmitRespondsAfterRespondent2FullDefence() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("your offer to pay by " + paymentDate)
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent1FullAdmitRespondsFirst() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent1FullAdmitRespondsFirstBeforeFullDefence() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondent2ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .doesNotContain("You believe you owe")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent1FullAdmitRespondsAfterRespondent2FullDefence() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent2FullDefenceRespondsFirst() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(null);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent2FullAdmitAndRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent1FullDefenceAfterRespondent2FullAdmit() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_ADMISSION);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent2FullAdmitRespondsAfterRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("your offer to pay by " + paymentDate)
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent2FullAdmitRespondsFirst() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent2FullAdmitRespondsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondent1ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .doesNotContain("You believe you owe")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent2FullAdmitRespondsAfterRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent1FullDefenceRespondsFirstBeforeFullAdmit() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyFullDefenceFullAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setDefenceRouteRequired(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2027, 12, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent1CounterClaimsAndRespondent2FullAdmit() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent2FullAdmitAndRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent2FullAdmitRespondsAfterRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text).contains("your offer to pay by " + paymentDate)
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitInstalmentsConfirmation_whenRespondent2FullAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .doesNotContain("Download questionnaire")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitSetDateConfirmation_whenRespondent2FullAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondent1ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .doesNotContain("You believe you owe")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowFullAdmitImmediateConfirmation_whenRespondent2FullAdmitRespondsAfterRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .doesNotContain("You believe you owe")
            );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent1CounterClaimsFirstBeforeFullAdmit() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyCounterClaimFullAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2028, 2, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent2PartAdmitAndRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent1FullDefenceAfterRespondent2PartAdmit() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent2PartAdmitRespondsAfterRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;320")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent2PartAdmitRespondsFirst() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent2PartAdmitRespondsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;320")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent2PartAdmitRespondsAfterRespondent1FullDefence() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .contains("You believe you owe &#163;320")
            );
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent1FullDefenceRespondsFirstBeforePartAdmit() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyFullDefencePartAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(320));
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setDefenceRouteRequired(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2028, 4, 10));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent1CounterClaimsAndRespondent2PartAdmit() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent2PartAdmitAndRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent2PartAdmitRespondsAfterRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;290")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent2PartAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.NO
        );

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent2PartAdmitRespondsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec2().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;290")
        );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent2PartAdmitRespondsAfterRespondent1CounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .contains("You believe you owe &#163;290")
            );
        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGenerateCounterClaimConfirmation_whenRespondent1CounterClaimsFirstBeforePartAdmit() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        assertThat(new CounterClaimConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("You've chosen to counterclaim")
                .contains("Download form N9B")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyCounterClaimPartAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(290));
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2028, 6, 20));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent1PartAdmitAndRespondent2FullDefence() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text -> assertThat(text).contains("you will pay immediately"));
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent2FullDefenceAfterRespondent1PartAdmit() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent1PartAdmitRespondsAfterRespondent2FullDefence() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;300")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitInstalmentsConfirmation_whenRespondent1PartAdmitRespondsFirst() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            YesOrNo.YES
        );

        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("you've suggested paying by instalments")
                .contains("Download questionnaire")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitSetDateConfirmation_whenRespondent1PartAdmitRespondsFirstBeforeFullDefence() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
            YesOrNo.YES
        );
        caseData.setRespondent2ResponseDate(null);

        String paymentDate = DateFormatHelper.formatLocalDate(
            caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
            DATE
        );

        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
            assertThat(text)
                .contains("your offer to pay by " + paymentDate)
                .contains("You believe you owe &#163;300")
        );
        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldShowPartAdmitImmediateConfirmation_whenRespondent1PartAdmitRespondsAfterRespondent2FullDefence() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.YES
        );
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null))
            .hasValueSatisfying(text ->
                assertThat(text)
                    .contains("you will pay immediately")
                    .contains("You believe you owe &#163;300")
            );
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldNotShowAdmitConfirmation_whenRespondent2FullDefenceRespondsFirstBeforePartAdmit() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY,
            YesOrNo.NO
        );
        caseData.setRespondent1ResponseDate(null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(null);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);

        assertThat(new PartialAdmitPayImmediatelyConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new PartialAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new RepayPlanConfirmationText().generateTextFor(caseData, null)).isEmpty();
        assertThat(new FullAdmitSetDateConfirmationText().generateTextFor(caseData, null)).isEmpty();
    }

    private CaseData multipartyPartAdmitFullDefence(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(300))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setDefenceRouteRequired2(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2027, 8, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);
        return caseData;
    }

    private CaseData multipartyFullAdmitFullDefence(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);
        caseData.setDefenceRouteRequired2(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2027, 4, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);
        return caseData;
    }

    private CaseData multipartyPartAdmitFullAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(350))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2026, 11, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2026, 12, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    private CaseData multipartyCaseWithDifferentPaymentMethods(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        YesOrNo isRespondent1
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(respondent1Payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(1000))
            .totalClaimAmount(BigDecimal.valueOf(2000))
            .isRespondent1(isRespondent1)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(500));
        caseData.setIsRespondent2(YesOrNo.YES.equals(isRespondent1) ? YesOrNo.NO : YesOrNo.YES);

        RespondToClaimAdmitPartLRspec admitPart1 = new RespondToClaimAdmitPartLRspec();
        admitPart1.setWhenWillThisAmountBePaid(LocalDate.of(2026, 3, 1));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart1);

        RespondToClaimAdmitPartLRspec admitPart2 = new RespondToClaimAdmitPartLRspec();
        admitPart2.setWhenWillThisAmountBePaid(LocalDate.of(2026, 4, 15));
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart2);
        return caseData;
    }

    private CaseData getCounterClaim() {
        return CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .build();
    }

    private CaseData sameSolicitorSamePartAdmit(
        RespondentResponsePartAdmissionPaymentTimeLRspec payment
    ) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(payment)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(400))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(YesOrNo.YES)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setIsRespondent2(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(payment);
        caseData.setRespondToAdmittedClaimOwingAmountPounds2(BigDecimal.valueOf(400));
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);

        RespondToClaimAdmitPartLRspec admitPart = new RespondToClaimAdmitPartLRspec();
        admitPart.setWhenWillThisAmountBePaid(LocalDate.of(2026, 6, 15));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart);
        caseData.setRespondToClaimAdmitPartLRspec2(admitPart);
        return caseData;
    }

    private CaseData sameSolicitorDivergentCounterClaimAndFullAdmit() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual().build())
            .respondent1(new PartyBuilder().individual().build())
            .respondent2(new PartyBuilder().individual().build())
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .isRespondent1(YesOrNo.YES)
            .build();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondentResponseIsSame(YesOrNo.NO);
        caseData.setIsRespondent2(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY
        );
        return caseData;
    }

    private CaseData twoVOneFullAdmitSetDate() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .applicant1(new PartyBuilder().individual("Alice").build())
            .applicant2(new PartyBuilder().individual("Bob").build())
            .addApplicant2(YesOrNo.YES)
            .respondent1(new PartyBuilder().individual().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent1(YesOrNo.YES)
            .build();
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setIsRespondent2(null);

        RespondToClaimAdmitPartLRspec admitPart = new RespondToClaimAdmitPartLRspec();
        admitPart.setWhenWillThisAmountBePaid(LocalDate.of(2026, 7, 20));
        caseData.setRespondToClaimAdmitPartLRspec(admitPart);
        return caseData;
    }

    @Override
    public Class<RespondToClaimConfirmationTextSpecGenerator> getIntentionInterface() {
        return RespondToClaimConfirmationTextSpecGenerator.class;
    }

    private List<CaseData> get2v1DifferentResponseCase() {
        Party applicant1 = new Party();
        Party applicant2 = new Party();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    CaseData caseData = CaseDataBuilder.builder().build();
                    caseData.setApplicant1(applicant1);
                    caseData.setApplicant2(applicant2);
                    caseData.setClaimant1ClaimResponseTypeForSpec(r1);
                    caseData.setClaimant2ClaimResponseTypeForSpec(r2);
                    cases.add(caseData);
                }
            }
        }
        return cases;
    }

    private List<CaseData> get1v2DivergentResponseCase() {
        Party applicant1 = new Party();
        Party respondent1 = new Party();
        Party respondent2 = new Party();

        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    CaseData caseData = CaseDataBuilder.builder().build();
                    caseData.setApplicant1(applicant1);
                    caseData.setRespondent1(respondent1);
                    caseData.setRespondent2(respondent2);
                    caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
                    caseData.setRespondentResponseIsSame(YesOrNo.NO);
                    caseData.setRespondent1ClaimResponseTypeForSpec(r1);
                    caseData.setRespondent2ClaimResponseTypeForSpec(r2);
                    cases.add(caseData);
                }
            }
        }
        return cases;
    }

    @Override
    public List<Pair<CaseData,
            Class<? extends RespondToClaimConfirmationTextSpecGenerator>>> getCasesToExpectedImplementation() {
        List<Pair<CaseData, Class<? extends RespondToClaimConfirmationTextSpecGenerator>>> list = new ArrayList<>(
                List.of(
                        Pair.of(getFullAdmitAlreadyPaidCase(), FullAdmitAlreadyPaidConfirmationText.class),
                        Pair.of(getPartialAdmitSetDate(), PartialAdmitSetDateConfirmationText.class),
                        Pair.of(getPartialAdmitSetDate(true), PartialAdmitSetDateConfirmationText.class),
                        Pair.of(getPartialAdmitPayImmediately(), PartialAdmitPayImmediatelyConfirmationText.class),
                        Pair.of(getPartialAdmitPayImmediately(true), PartialAdmitPayImmediatelyConfirmationText.class),
                        Pair.of(getFullAdmitRepayPlan(), RepayPlanConfirmationText.class),
                        Pair.of(getPartialAdmitRepayPlan(), RepayPlanConfirmationText.class),
                        Pair.of(getPartialAdmitRepayPlan(true), RepayPlanConfirmationText.class),
                        Pair.of(getFullAdmitAlreadyPaid(), FullAdmitAlreadyPaidConfirmationText.class),
                        Pair.of(getFullAdmitPayBySetDate(), FullAdmitSetDateConfirmationText.class),
                        Pair.of(getPartialAdmitPayFull(), PartialAdmitPaidFullConfirmationText.class),
                        Pair.of(getPartialAdmitPayFull(true), PartialAdmitPaidFullConfirmationText.class),
                        Pair.of(getPartialAdmitPayLess(), PartialAdmitPaidLessConfirmationText.class),
                        Pair.of(getPartialAdmitPayLess(true), PartialAdmitPaidLessConfirmationText.class),
                        Pair.of(getCounterClaim(), CounterClaimConfirmationText.class),
                        Pair.of(getFullAdmitPayImmediately(), PartialAdmitPayImmediatelyConfirmationText.class),
                        Pair.of(getFullAdmitRepayPlanLiPvLr(), RepayPlanConfirmationText.class),
                        Pair.of(getFullAdmitPayBySetDateLipVLr(), FullAdmitSetDateConfirmationText.class),
                        Pair.of(multipartyCaseWithDifferentPaymentMethods(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
                            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
                            YesOrNo.YES
                        ), RepayPlanConfirmationText.class),
                        Pair.of(multipartyCaseWithDifferentPaymentMethods(
                            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
                            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE,
                            YesOrNo.NO
                        ), PartialAdmitSetDateConfirmationText.class)
                ));
        get2v1DifferentResponseCase().forEach(caseData -> list.add(
                Pair.of(caseData, SpecResponse2v1DifferentText.class))
        );
        get1v2DivergentResponseCase().forEach(caseData -> list.add(
                Pair.of(caseData, SpecResponse1v2DivergentText.class)
        ));
        return list;
    }
}
