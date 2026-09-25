package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

class ResponseRepaymentDetailsFormTest {

    @Test
    void shouldShowPartAdmitSetDate_whenOneVOneWithNoRespondent2() {
        CaseData caseData = oneVOnePartAdmit(BY_SET_DATE);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(form.amountToPay()).isEqualTo("500.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - 1v1");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowFullAdmitImmediate_whenOneVOneIgnoresUnsetRespondent2PaymentFields() {
        CaseData caseData = oneVOneFullAdmit(IMMEDIATELY);
        // Stale R2 fields must not divert a 1v1 sealed form
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 12, 31))
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowSameSolicitorSharedPartAdmitSetDate_whenBothDefendantsHaveSameResponse() {
        CaseData caseData = sameSolicitorSamePartAdmitResponse(BY_SET_DATE);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(form.amountToPay()).isEqualTo("400.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - same solicitor");
    }

    @Test
    void shouldUseRespondent1RepaymentPlan_whenRespondent1RespondsWithDifferentInstalmentPlan() {
        CaseData caseData = multipartyCaseWithDifferentInstalmentPlans(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(form.repaymentPlan().getFirstRepaymentDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(form.repaymentPlan().getPaymentFrequencyDisplay())
            .isEqualTo(PaymentFrequencyLRspec.ONCE_ONE_WEEK.getLabel());
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1");
    }

    @Test
    void shouldUseRespondent2RepaymentPlan_whenRespondent2RespondsWithDifferentInstalmentPlan() {
        CaseData caseData = multipartyCaseWithDifferentInstalmentPlans(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(form.repaymentPlan().getFirstRepaymentDate()).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(form.repaymentPlan().getPaymentFrequencyDisplay())
            .isEqualTo(PaymentFrequencyLRspec.ONCE_ONE_MONTH.getLabel());
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2");
    }

    @Test
    void shouldUseRespondent1PayByDate_whenRespondent1RespondsWithDifferentSetDate() {
        CaseData caseData = multipartyCaseWithDifferentSetDates(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1");
    }

    @Test
    void shouldUseRespondent2PayByDate_whenRespondent2RespondsWithDifferentSetDate() {
        CaseData caseData = multipartyCaseWithDifferentSetDates(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2");
    }

    @Test
    void shouldUseRespondent1Answers_whenRespondentsHaveDifferentPaymentTypesAndRespondent1IsLatest() {
        CaseData caseData = multipartyCaseWithMixedPaymentTypes(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(form.payBy()).isNotEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1");
        assertThat(form.mediation()).isTrue();
    }

    @Test
    void shouldUseRespondent2Answers_whenRespondentsHaveDifferentPaymentTypesAndRespondent2IsLatest() {
        CaseData caseData = multipartyCaseWithMixedPaymentTypes(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2");
        assertThat(form.mediation()).isFalse();
    }

    @Test
    void shouldUseRespondent1ImmediatePayBy_whenBothFullAdmitImmediateAndRespondent1IsLatest() {
        CaseData caseData = multipartyCaseWithImmediatePayments(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyNotPayImmediately()).isNull();
    }

    @Test
    void shouldUseRespondent2ImmediatePayBy_whenBothFullAdmitImmediateAndRespondent2IsLatest() {
        CaseData caseData = multipartyCaseWithImmediatePayments(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyNotPayImmediately()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenRespondent1FullAdmitsFirstBeforeRespondent2() {
        CaseData caseData = multipartyCaseWithImmediatePayments(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenRespondent2FullAdmitsFirstBeforeRespondent1() {
        CaseData caseData = multipartyCaseWithImmediatePayments(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitSetDate_whenRespondent1FullAdmitsFirstBeforeRespondent2Instalments() {
        CaseData caseData = multipartyCaseWithMixedPaymentTypes(LocalDateTime.now(), null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 first");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 3, 1))
        );
        caseData.setRespondent1RepaymentPlan(null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 first");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitInstalments_whenRespondent2FullAdmitsFirstBeforeRespondent1Immediate() {
        CaseData caseData = multipartyCaseWithDifferentInstalmentPlans(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2");
    }

    @Test
    void shouldShowRespondent1PartAdmitPaymentDetails_whenBothPartAdmitWithDifferentPaymentTypes() {
        CaseData caseData = multipartyPartAdmitMixedPayments(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("100.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenBothPartAdmitWithDifferentPaymentTypes() {
        CaseData caseData = multipartyPartAdmitMixedPayments(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(form.amountToPay()).isEqualTo("250.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 part");
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenBothPartAdmitInstalments() {
        CaseData caseData = multipartyPartAdmitInstalments(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 part");
        assertThat(form.amountToPay()).isEqualTo("250.00");
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenRespondent1PartAdmitsFirstBeforeRespondent2() {
        CaseData caseData = multipartyPartAdmitMixedPayments(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("100.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenRespondent2PartAdmitsFirstBeforeRespondent1() {
        CaseData caseData = multipartyPartAdmitMixedPayments(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(form.amountToPay()).isEqualTo("250.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 part");
    }

    @Test
    void shouldShowRespondent1PartAdmitInstalments_whenRespondent1PartAdmitsFirstBeforeRespondent2Immediate() {
        CaseData caseData = multipartyPartAdmitInstalments(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 part");
        assertThat(form.amountToPay()).isEqualTo("100.00");
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenRespondent2PartAdmitsFirstBeforeRespondent1() {
        CaseData caseData = multipartyPartAdmitInstalments(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 part");
        assertThat(form.amountToPay()).isEqualTo("250.00");
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenMixedFullAndPartAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullAdmitPartAdmitMixedPayments(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenMixedFullAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullAdmitPartAdmitMixedPayments(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(form.amountToPay()).isEqualTo("400.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 mixed");
    }

    @Test
    void shouldShowRespondent1FullAdmitInstalments_whenMixedFullInstalmentsAndPartImmediate() {
        CaseData caseData = multipartyFullAdmitInstalmentsPartAdmitImmediate(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 mixed");
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenMixedFullInstalmentsAndPartImmediate() {
        CaseData caseData = multipartyFullAdmitInstalmentsPartAdmitImmediate(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(form.amountToPay()).isEqualTo("400.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenRespondent1FullAdmitsFirstBeforeRespondent2PartAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmitMixedPayments(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenRespondent2PartAdmitsFirstBeforeRespondent1FullAdmit() {
        CaseData caseData = multipartyFullAdmitPartAdmitMixedPayments(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(form.amountToPay()).isEqualTo("400.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 mixed");
    }

    @Test
    void shouldShowRespondent1FullAdmitInstalments_whenRespondent1FullAdmitsFirstBeforeRespondent2PartImmediate() {
        CaseData caseData = multipartyFullAdmitInstalmentsPartAdmitImmediate(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 mixed");
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenRespondent2PartAdmitsFirstBeforeRespondent1FullInstalments() {
        CaseData caseData = multipartyFullAdmitInstalmentsPartAdmitImmediate(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(form.amountToPay()).isEqualTo("400.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenMixedPartAndFullAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(form.amountToPay()).isEqualTo("350.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenMixedPartAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 full");
    }

    @Test
    void shouldShowRespondent1PartAdmitInstalments_whenMixedPartInstalmentsAndFullImmediate() {
        CaseData caseData = multipartyPartAdmitInstalmentsFullAdmitImmediate(
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 part");
        assertThat(form.amountToPay()).isEqualTo("350.00");
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenMixedPartInstalmentsAndFullImmediate() {
        CaseData caseData = multipartyPartAdmitInstalmentsFullAdmitImmediate(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenRespondent2RespondsFirstBeforeRespondent1PartAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(null, LocalDateTime.now());
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 2, 1))
        );
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2(null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitSetDate_whenRespondent1RespondsFirstBeforeRespondent2FullAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(LocalDateTime.now(), null);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 first");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 3, 10))
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 3, 10));
        assertThat(form.amountToPay()).isEqualTo("350.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 first");
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenRespondent1PartAdmitsFirstBeforeRespondent2FullAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(form.amountToPay()).isEqualTo("350.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenRespondent2FullAdmitsFirstBeforeRespondent1PartAdmit() {
        CaseData caseData = multipartyPartAdmitFullAdmitMixedPayments(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2026, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 full");
    }

    @Test
    void shouldShowRespondent1PartAdmitInstalments_whenRespondent1PartAdmitsFirstBeforeRespondent2FullImmediate() {
        CaseData caseData = multipartyPartAdmitInstalmentsFullAdmitImmediate(LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 part");
        assertThat(form.amountToPay()).isEqualTo("350.00");
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenRespondent2FullAdmitsFirstBeforeRespondent1PartInstalments() {
        CaseData caseData = multipartyPartAdmitInstalmentsFullAdmitImmediate(null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenMixedFullAdmitAndFullDefenceAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyReject()).isNull();
        assertThat(form.freeTextWhyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullDefenceDispute_whenMixedFullAdmitAndFullDefenceAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R2 disputes the claim");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullDefence_whenRespondent2RejectsAllFirst() {
        CaseData caseData = multipartyFullAdmitFullDefence(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R2 disputes the claim");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitInstalments_whenRespondent1AdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 FR");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenRespondent1AdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyFullAdmitFullDefence(IMMEDIATELY, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitSetDate_whenRespondent1AdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyFullAdmitFullDefence(BY_SET_DATE, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 FR");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitSetDate_whenMixedFullAdmitAndFullDefenceAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullAdmitFullDefence(
            BY_SET_DATE,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 FR");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullDefence_whenMixedRejectAllAndFullAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R1 disputes the claim");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenMixedRejectAllAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 RF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenRespondent2FullAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefenceFullAdmit(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullDefence_whenRespondent1RejectsAllFirstBeforeRespondent2FullAdmit() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R1 disputes the claim");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitInstalments_whenMixedRejectAllAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 RF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenMixedRejectAllAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            IMMEDIATELY,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenRespondent2FullAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefenceFullAdmit(BY_SET_DATE, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 12, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 RF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitInstalments_whenRespondent2FullAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefenceFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            null,
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 RF");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyFullDefenceFullAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceRouteRequired(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("R1 disputes the claim");
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 12, 15))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2 RF");
            caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(90))))
                                                     .setFirstRepaymentDate(LocalDate.of(2028, 1, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH));
        } else if (BY_SET_DATE.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 RF");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1CounterClaim_whenMixedCounterClaimAndFullAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenMixedCounterClaimAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 2, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 CF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenRespondent2FullAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 2, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1CounterClaim_whenRespondent1CounterClaimsFirstBeforeRespondent2FullAdmit() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitInstalments_whenMixedCounterClaimAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(85));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 CF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitImmediate_whenMixedCounterClaimAndFullAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            IMMEDIATELY,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 2, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitSetDate_whenRespondent2FullAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(BY_SET_DATE, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 2, 15));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 CF");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullAdmitInstalments_whenRespondent2FullAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimFullAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            null,
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(85));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 CF");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyCounterClaimFullAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2028, 2, 15))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2 CF");
            caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(85))))
                                                     .setFirstRepaymentDate(LocalDate.of(2028, 3, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH));
        } else if (BY_SET_DATE.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 CF");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1FullDefence_whenMixedRejectAllAndPartAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R1 disputes before part admit");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenMixedRejectAllAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 4, 10));
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 RP");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenRespondent2PartAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefencePartAdmit(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 4, 10));
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullDefence_whenRespondent1RejectsAllFirstBeforeRespondent2PartAdmit() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R1 disputes before part admit");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenMixedRejectAllAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 RP");
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenMixedRejectAllAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            IMMEDIATELY,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 4, 10));
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenRespondent2PartAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefencePartAdmit(BY_SET_DATE, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 4, 10));
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 RP");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenRespondent2PartAdmitsFirstBeforeRejectAll() {
        CaseData caseData = multipartyFullDefencePartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            null,
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 RP");
        assertThat(form.amountToPay()).isEqualTo("320.00");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyFullDefencePartAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim("R1 disputes before part admit");
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(320)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2028, 4, 10))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2 RP");
            caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(40))))
                                                     .setFirstRepaymentDate(LocalDate.of(2028, 5, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 RP");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1CounterClaim_whenMixedCounterClaimAndPartAdmitAndRespondent1IsLatest() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenMixedCounterClaimAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 6, 20));
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 CP");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenRespondent2PartAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 6, 20));
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1CounterClaim_whenRespondent1CounterClaimsFirstBeforeRespondent2PartAdmit() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenMixedCounterClaimAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(35));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 CP");
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitImmediate_whenMixedCounterClaimAndPartAdmitAndRespondent2IsLatest() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            IMMEDIATELY,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 6, 20));
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitSetDate_whenRespondent2PartAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(BY_SET_DATE, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2028, 6, 20));
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R2 CP");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2PartAdmitInstalments_whenRespondent2PartAdmitsFirstBeforeCounterClaim() {
        CaseData caseData = multipartyCounterClaimPartAdmit(
            SUGGESTION_OF_REPAYMENT_PLAN,
            null,
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(35));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R2 CP");
        assertThat(form.amountToPay()).isEqualTo("290.00");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyCounterClaimPartAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent2Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(290)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(respondent2Payment);
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2028, 6, 20))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2 CP");
            caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(35))))
                                                     .setFirstRepaymentDate(LocalDate.of(2028, 7, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent2Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 CP");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenMixedPartAdmitAndFullDefenceAndRespondent1IsLatest() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("300.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2FullDefenceDispute_whenMixedPartAdmitAndFullDefenceAndRespondent2IsLatest() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R2 disputes after part admit");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2FullDefence_whenRespondent2RejectsAllFirstBeforePartAdmit() {
        CaseData caseData = multipartyPartAdmitFullDefence(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(form.whyReject()).isEqualTo("DISPUTE");
        assertThat(form.freeTextWhyReject()).isEqualTo("R2 disputes after part admit");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitInstalments_whenRespondent1PartAdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(60));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 PR");
        assertThat(form.amountToPay()).isEqualTo("300.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenRespondent1PartAdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyPartAdmitFullDefence(IMMEDIATELY, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("300.00");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitSetDate_whenRespondent1PartAdmitsFirstBeforeRespondent2Rejects() {
        CaseData caseData = multipartyPartAdmitFullDefence(BY_SET_DATE, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("300.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 PR");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitSetDate_whenMixedPartAdmitAndFullDefenceAndRespondent1IsLatest() {
        CaseData caseData = multipartyPartAdmitFullDefence(
            BY_SET_DATE,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 8, 1));
        assertThat(form.amountToPay()).isEqualTo("300.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 PR");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyPartAdmitFullDefence(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(300)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(respondent1Payment);
        caseData.setDefenceRouteRequired2(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim2("R2 disputes after part admit");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 8, 1))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 PR");
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(60))))
                                                     .setFirstRepaymentDate(LocalDate.of(2027, 9, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 PR");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenMixedPartAdmitAndCounterClaimAndRespondent1IsLatest() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(form.amountToPay()).isEqualTo("275.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2CounterClaim_whenMixedPartAdmitAndCounterClaimAndRespondent2IsLatest() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2CounterClaim_whenRespondent2CounterClaimsFirstBeforePartAdmit() {
        CaseData caseData = multipartyPartAdmitCounterClaim(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitInstalments_whenRespondent1PartAdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 PC");
        assertThat(form.amountToPay()).isEqualTo("275.00");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitImmediate_whenRespondent1PartAdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(IMMEDIATELY, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(form.amountToPay()).isEqualTo("275.00");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitSetDate_whenRespondent1PartAdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyPartAdmitCounterClaim(BY_SET_DATE, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(form.amountToPay()).isEqualTo("275.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 PC");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1PartAdmitSetDate_whenMixedPartAdmitAndCounterClaimAndRespondent1IsLatest() {
        CaseData caseData = multipartyPartAdmitCounterClaim(
            BY_SET_DATE,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(form.amountToPay()).isEqualTo("275.00");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 PC");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyPartAdmitCounterClaim(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(275)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(respondent1Payment);
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 10, 1))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 PC");
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(55))))
                                                     .setFirstRepaymentDate(LocalDate.of(2027, 11, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 PC");
        }
        return caseData;
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenMixedFullAdmitAndCounterClaimAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            IMMEDIATELY,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent2CounterClaim_whenMixedFullAdmitAndCounterClaimAndRespondent2IsLatest() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            BY_SET_DATE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.amountToPay()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent2CounterClaim_whenRespondent2CounterClaimsFirst() {
        CaseData caseData = multipartyFullAdmitCounterClaim(IMMEDIATELY, null, LocalDateTime.now());

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.COUNTER_CLAIM);
        assertThat(form.whyReject()).isEqualTo("COUNTER_CLAIM");
        assertThat(form.payBy()).isNull();
        assertThat(form.repaymentPlan()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitInstalments_whenRespondent1AdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            SUGGESTION_OF_REPAYMENT_PLAN,
            LocalDateTime.now(),
            null
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(SUGGESTION_OF_REPAYMENT_PLAN);
        assertThat(form.repaymentPlan().getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(form.whyNotPayImmediately()).isEqualTo("Cannot pay all at once - R1 FC");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitImmediate_whenRespondent1AdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(IMMEDIATELY, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(IMMEDIATELY);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.repaymentPlan()).isNull();
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitSetDate_whenRespondent1AdmitsFirstBeforeRespondent2CounterClaim() {
        CaseData caseData = multipartyFullAdmitCounterClaim(BY_SET_DATE, LocalDateTime.now(), null);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 FC");
        assertThat(form.whyReject()).isNull();
    }

    @Test
    void shouldShowRespondent1FullAdmitSetDate_whenMixedFullAdmitAndCounterClaimAndRespondent1IsLatest() {
        CaseData caseData = multipartyFullAdmitCounterClaim(
            BY_SET_DATE,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now()
        );

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.responseType()).isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
        assertThat(form.howToPay()).isEqualTo(BY_SET_DATE);
        assertThat(form.payBy()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(form.amountToPay()).isEqualTo("1000");
        assertThat(form.whyNotPayImmediately()).isEqualTo("Need more time - R1 FC");
        assertThat(form.whyReject()).isNull();
    }

    private CaseData multipartyFullAdmitCounterClaim(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(respondent1Payment);
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 6, 1))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 FC");
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(80))))
                                                     .setFirstRepaymentDate(LocalDate.of(2027, 7, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 FC");
        }
        return caseData;
    }

    private CaseData multipartyFullAdmitFullDefence(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec respondent1Payment,
        LocalDateTime respondent1ResponseDate,
        LocalDateTime respondent2ResponseDate
    ) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(respondent1Payment);
        caseData.setDefenceRouteRequired2(
            uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
        );
        caseData.setDetailsOfWhyDoesYouDisputeTheClaim2("R2 disputes the claim");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 4, 1))
        );
        if (SUGGESTION_OF_REPAYMENT_PLAN.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 FR");
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                     .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100))))
                                                     .setFirstRepaymentDate(LocalDate.of(2027, 5, 1))
                                                     .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        } else if (BY_SET_DATE.equals(respondent1Payment)) {
            caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1 FR");
        }
        return caseData;
    }

    private CaseData multipartyPartAdmitFullAdmitMixedPayments(LocalDateTime respondent1ResponseDate,
                                                               LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(350)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 full");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 11, 1))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 12, 15))
        );
        return caseData;
    }

    private CaseData multipartyPartAdmitInstalmentsFullAdmitImmediate(LocalDateTime respondent1ResponseDate,
                                                                      LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(350)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 part");
        caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(75))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 3, 10))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2027, 1, 5))
        );
        return caseData;
    }

    private CaseData multipartyFullAdmitPartAdmitMixedPayments(LocalDateTime respondent1ResponseDate,
                                                               LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(400)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 mixed");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 8, 1))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 9, 15))
        );
        return caseData;
    }

    private CaseData multipartyFullAdmitInstalmentsPartAdmitImmediate(LocalDateTime respondent1ResponseDate,
                                                                      LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(400)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 mixed");
        caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 1, 10))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 10, 5))
        );
        return caseData;
    }

    private CaseData multipartyPartAdmitMixedPayments(LocalDateTime respondent1ResponseDate,
                                                      LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyPartAdmitBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100)))
        );
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(250)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2 part");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 6, 1))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 7, 15))
        );
        return caseData;
    }

    private CaseData multipartyPartAdmitInstalments(LocalDateTime respondent1ResponseDate,
                                                    LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyPartAdmitBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100)))
        );
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(250)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1 part");
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2 part");
        caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(20))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 1, 10))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(50))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 2, 15))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH));
        return caseData;
    }

    private CaseData multipartyPartAdmitBaseCase(LocalDateTime respondent1ResponseDate,
                                                 LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        return caseData;
    }

    private CaseData multipartyCaseWithDifferentInstalmentPlans(LocalDateTime respondent1ResponseDate,
                                                                LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1");
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Cannot pay all at once - R2");
        caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 1, 10))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        caseData.setRespondent2RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(250))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 2, 15))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH));
        return caseData;
    }

    private CaseData multipartyCaseWithDifferentSetDates(LocalDateTime respondent1ResponseDate,
                                                         LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - R1");
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 3, 1))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 4, 15))
        );
        return caseData;
    }

    private CaseData multipartyCaseWithMixedPaymentTypes(LocalDateTime respondent1ResponseDate,
                                                         LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(BY_SET_DATE);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Cannot pay all at once - R1");
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - R2");
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setResponseClaimMediationSpec2Required(YesOrNo.NO);
        caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                                                 .setPaymentAmount(new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(100))))
                                                 .setFirstRepaymentDate(LocalDate.of(2026, 1, 10))
                                                 .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 4, 15))
        );
        return caseData;
    }

    private CaseData multipartyCaseWithImmediatePayments(LocalDateTime respondent1ResponseDate,
                                                         LocalDateTime respondent2ResponseDate) {
        CaseData caseData = multipartyBaseCase(respondent1ResponseDate, respondent2ResponseDate);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 5, 1))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 5, 10))
        );
        return caseData;
    }

    private CaseData multipartyBaseCase(LocalDateTime respondent1ResponseDate,
                                        LocalDateTime respondent2ResponseDate) {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.COMPANY);
        applicant1.setCompanyName("Applicant Ltd");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Resp1 Ltd");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.COMPANY);
        respondent2.setCompanyName("Resp2 Ltd");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDate(respondent1ResponseDate)
            .respondent2ResponseDate(respondent2ResponseDate)
            .build();

        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        return caseData;
    }

    private CaseData oneVOnePartAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec payment
    ) {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.COMPANY);
        applicant1.setCompanyName("Applicant Ltd");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Resp1 Ltd");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDate(LocalDateTime.now())
            .build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(500)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(payment);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - 1v1");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 5, 1))
        );
        return caseData;
    }

    private CaseData oneVOneFullAdmit(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec payment
    ) {
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.COMPANY);
        applicant1.setCompanyName("Applicant Ltd");
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Resp1 Ltd");

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(applicant1)
            .respondent1(respondent1)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDate(LocalDateTime.now())
            .build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(payment);
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 4, 1))
        );
        return caseData;
    }

    private CaseData sameSolicitorSamePartAdmitResponse(
        uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec payment
    ) {
        LocalDateTime responseDate = LocalDateTime.now();
        CaseData caseData = multipartyBaseCase(responseDate, responseDate);
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(400)))
        );
        caseData.setRespondToAdmittedClaimOwingAmount2(
            new BigDecimal(MonetaryConversions.poundsToPennies(BigDecimal.valueOf(400)))
        );
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(payment);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(payment);
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec("Need more time - same solicitor");
        caseData.setResponseToClaimAdmitPartWhyNotPayLRspec2("Need more time - same solicitor");
        caseData.setRespondToClaimAdmitPartLRspec(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 6, 15))
        );
        caseData.setRespondToClaimAdmitPartLRspec2(
            new RespondToClaimAdmitPartLRspec(LocalDate.of(2026, 6, 15))
        );
        return caseData;
    }
}
