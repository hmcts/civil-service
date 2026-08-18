package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

public final class DefaultJudgementSpecFixtures {

    private DefaultJudgementSpecFixtures() {
    }

    public static CaseData specDj1v1NonDivergentNoBuffer() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .calculatedClaimFeeInPence(BigDecimal.valueOf(7000))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(7000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPayment(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder().label("Mr Defendant").build())
                                      .listItems(List.of(DynamicListElement.builder().label("Mr Defendant").build()))
                                      .build())
            .breathing(new BreathingSpaceInfo()
                           .setLift(new BreathingSpaceLiftInfo()
                                        .setExpectedEnd(LocalDate.now().minusDays(5))))
            .build();
    }

    public static CaseData specDj1v1NonDivergentWithBuffer() {
        return specDj1v1NonDivergentNoBuffer().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .build();
    }

    public static CaseData specDj1v2Divergent() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .calculatedClaimFeeInPence(BigDecimal.valueOf(7000))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(7000)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPayment(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder().label("Mr Defendant").build())
                                      .listItems(List.of(
                                          DynamicListElement.builder().label("Mr Defendant").build(),
                                          DynamicListElement.builder().label("Mrs Defendant Two").build(),
                                          DynamicListElement.builder().label("Both Defendants").build()
                                      ))
                                      .build())
            .breathing(new BreathingSpaceInfo()
                           .setLift(new BreathingSpaceLiftInfo()
                                        .setExpectedEnd(LocalDate.now().minusDays(5))))
            .build();
    }

    public static CaseData specDjDeadlineNotPassed() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(5))
            .addRespondent2(YesOrNo.NO)
            .breathing(new BreathingSpaceInfo()
                           .setLift(new BreathingSpaceLiftInfo()
                                        .setExpectedEnd(LocalDate.now().minusDays(5))))
            .build();
    }

    public static CaseData specDjInBreathingSpace() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .addRespondent2(YesOrNo.NO)
            .breathing(new BreathingSpaceInfo()
                           .setEnter(new BreathingSpaceEnterInfo()))
            .build();
    }
}
