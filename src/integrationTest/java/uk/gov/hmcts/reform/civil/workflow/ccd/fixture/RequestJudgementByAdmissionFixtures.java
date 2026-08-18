package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

public final class RequestJudgementByAdmissionFixtures {

    private RequestJudgementByAdmissionFixtures() {
    }

    public static CaseData jba1v1PayImmediately() {
        return CaseDataBuilder.builder()
            .atStateRespondentFullAdmissionSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("000MC001")
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1Represented(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().individual().build())
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(LocalDate.now().minusDays(1))
                    .build())
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                                   .ccjPaymentPaidSomeAmount(BigDecimal.ZERO)
                                   .build())
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(7000)).build())
            .build();
    }

    public static CaseData jbaNotEligibleDateNotPermitted() {
        return CaseDataBuilder.builder()
            .atStateRespondentFullAdmissionSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .legacyCaseReference("000MC001")
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1Represented(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().individual().build())
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(LocalDate.now().plusDays(10))
                    .build())
            .build();
    }
}
