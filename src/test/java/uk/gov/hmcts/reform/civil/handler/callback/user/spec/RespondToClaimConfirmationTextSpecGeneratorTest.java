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
            .respondent1Represented(isLipVLR ? YesOrNo.YES :  YesOrNo.NO)
            .applicant1Represented(isLipVLR ? YesOrNo.NO :  YesOrNo.YES).build();
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
            .respondent1Represented(isLipVLR ? YesOrNo.YES :  YesOrNo.NO)
            .applicant1Represented(isLipVLR ? YesOrNo.NO :  YesOrNo.YES)
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
            .respondent1Represented(isLipVLR ? YesOrNo.YES :  YesOrNo.NO)
            .applicant1Represented(isLipVLR ? YesOrNo.NO :  YesOrNo.YES)
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
            .respondent1Represented(isLipVLR ? YesOrNo.YES :  YesOrNo.NO)
            .applicant1Represented(isLipVLR ? YesOrNo.NO :  YesOrNo.YES)
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
            .respondent1Represented(isLipVLR ? YesOrNo.YES :  YesOrNo.NO)
            .applicant1Represented(isLipVLR ? YesOrNo.NO :  YesOrNo.YES)
            .build();
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(howMuchWasPaid);
        caseData.setRespondToAdmittedClaim(respondToClaim);
        return caseData;
    }

    private CaseData getCounterClaim() {
        return CaseDataBuilder.builder()
            .atStateRespondentCounterClaim()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();
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
                Pair.of(getFullAdmitPayBySetDateLipVLr(), FullAdmitSetDateConfirmationText.class)
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
