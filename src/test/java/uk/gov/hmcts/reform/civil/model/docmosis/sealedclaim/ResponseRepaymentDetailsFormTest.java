package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseRepaymentDetailsFormTest {

    @Test
    void shouldUseRespondent2AlreadyPaidDetails_when1v2DifferentSolicitorsAndRespondent2RespondsFirstWithPartAdmitAlreadyPaid() {
        RespondToClaim respondent2Paid = new RespondToClaim();
        respondent2Paid.setHowMuchWasPaid(new BigDecimal("400100"));
        respondent2Paid.setWhenWasThisAmountPaid(LocalDate.of(2026, 11, 1));
        respondent2Paid.setHowWasThisAmountPaid(PaymentMethod.OTHER);
        respondent2Paid.setHowWasThisAmountPaidOther("bank transfer");

        CaseData caseData = oneVTwoDifferentSolicitorsRespondent2RespondedFirst();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.YES);
        caseData.setRespondToAdmittedClaim2(respondent2Paid);

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.whyReject()).isEqualTo("ALREADY_PAID");
        assertThat(form.howMuchWasPaid()).isEqualTo("4001.00");
        assertThat(form.paymentDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(form.paymentHow()).isEqualTo("bank transfer");
    }

    @Test
    void shouldUseRespondent2OwingAmount_when1v2DifferentSolicitorsAndRespondent2RespondsFirstWithPartAdmitNotPaid() {
        CaseData caseData = oneVTwoDifferentSolicitorsRespondent2RespondedFirst();
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setRespondToAdmittedClaimOwingAmount2(new BigDecimal("50000"));

        ResponseRepaymentDetailsForm form = ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData);

        assertThat(form.whyReject()).isNull();
        assertThat(form.amountToPay()).isEqualTo("500.00");
    }

    private CaseData oneVTwoDifferentSolicitorsRespondent2RespondedFirst() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Def1");
        respondent1.setIndividualLastName("One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Def2");
        respondent2.setIndividualLastName("Two");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .build();
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.NO);
        caseData.setRespondent1ResponseDate(null);
        caseData.setRespondent2ResponseDate(LocalDateTime.now());
        return caseData;
    }
}
