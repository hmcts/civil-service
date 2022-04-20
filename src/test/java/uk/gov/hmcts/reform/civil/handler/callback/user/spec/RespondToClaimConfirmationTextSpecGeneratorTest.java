package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * config for testing implementations of RespondToClaimConfirmationTextSpecGenerator.
 */
public class RespondToClaimConfirmationTextSpecGeneratorTest
    implements CaseDataToTextGeneratorTest
    .CaseDataToTextGeneratorIntentionConfig<RespondToClaimConfirmationTextSpecGenerator> {

    private CaseData getFullAdmitAlreadyPaidCase() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YesOrNo.YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
    }

    private CaseData getPartialAdmitSetDate() {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusMonths(1);
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(admitted)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .totalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)))
            .build();
    }

    private CaseData getPartialAdmitPayImmediately() {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToAdmittedClaimOwingAmountPounds(admitted)
            .build();
    }

    private CaseData getFullAdmitRepayPlan() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
    }

    private CaseData getPartialAdmitRepayPlan() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceFullAdmittedRequired(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
    }

    private CaseData getFullAdmitAlreadyPaid() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YesOrNo.YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
    }

    private CaseData getFullAdmitPayBySetDate() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .build();
    }

    private CaseData getPartialAdmitPayFull() {
        BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
        BigDecimal howMuchWasPaid = new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount));
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.YES)
            .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
            .totalClaimAmount(totalClaimAmount)
            .build();
    }

    private CaseData getPartialAdmitPayLess() {
        BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
        BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.YES)
            .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
            .totalClaimAmount(totalClaimAmount)
            .build();
    }

    private CaseData getCounterClaim() {
        return CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
    }

    @Override
    public Class<RespondToClaimConfirmationTextSpecGenerator> getIntentionInterface() {
        return RespondToClaimConfirmationTextSpecGenerator.class;
    }

    private List<CaseData> get2v1DifferentResponseCase() {
        Party applicant1 = Party.builder().build();
        Party applicant2 = Party.builder().build();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    cases.add(CaseData.builder()
                                  .applicant1(applicant1)
                                  .applicant2(applicant2)
                                  .claimant1ClaimResponseTypeForSpec(r1)
                                  .claimant2ClaimResponseTypeForSpec(r2)
                                  .build());
                }
            }
        }
        return cases;
    }

    private List<CaseData> get1v2DivergentResponseCase() {
        Party applicant1 = Party.builder().build();
        Party respondent1 = Party.builder().build();
        Party respondent2 = Party.builder().build();

        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    cases.add(CaseData.builder()
                                  .applicant1(applicant1)
                                  .respondent1(respondent1)
                                  .respondent2(respondent2)
                                  .respondent2SameLegalRepresentative(YesOrNo.YES)
                                  .respondentResponseIsSame(YesOrNo.NO)
                                  .respondent1ClaimResponseTypeForSpec(r1)
                                  .respondent2ClaimResponseTypeForSpec(r2)
                                  .build());
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
                Pair.of(getPartialAdmitPayImmediately(), PartialAdmitPayImmediatelyConfirmationText.class),
                Pair.of(getFullAdmitRepayPlan(), RepayPlanConfirmationText.class),
                Pair.of(getPartialAdmitRepayPlan(), RepayPlanConfirmationText.class),
                Pair.of(getFullAdmitAlreadyPaid(), FullAdmitAlreadyPaidConfirmationText.class),
                Pair.of(getFullAdmitPayBySetDate(), FullAdmitSetDateConfirmationText.class),
                Pair.of(getPartialAdmitPayFull(), PartialAdmitPaidFullConfirmationText.class),
                Pair.of(getPartialAdmitPayLess(), PartialAdmitPaidLessConfirmationText.class),
                Pair.of(getCounterClaim(), CounterClaimConfirmationText.class)
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
