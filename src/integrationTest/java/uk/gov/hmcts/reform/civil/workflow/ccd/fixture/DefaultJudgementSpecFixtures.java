package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
            .claimFee(claimFee())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPayment(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .defendantDetailsSpec(defendantDetails("Mr Defendant"))
            .breathing(liftedBreathingSpace())
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
            .claimFee(claimFee())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPayment(YesOrNo.NO)
            .addRespondent2(YesOrNo.YES)
            .respondent2(new PartyBuilder().individual().build())
            .defendantDetailsSpec(defendantDetails(
                "Mr Defendant",
                "Mrs Defendant Two",
                "Both Defendants"
            ))
            .breathing(liftedBreathingSpace())
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
            .breathing(liftedBreathingSpace())
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
            .breathing(new BreathingSpaceInfo().setEnter(new BreathingSpaceEnterInfo()))
            .build();
    }

    private static Fee claimFee() {
        return new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(7000));
    }

    private static BreathingSpaceInfo liftedBreathingSpace() {
        return new BreathingSpaceInfo()
            .setLift(new BreathingSpaceLiftInfo().setExpectedEnd(LocalDate.now().minusDays(5)));
    }

    private static DynamicList defendantDetails(String selected, String... extraLabels) {
        List<String> labels = extraLabels.length == 0
            ? List.of(selected)
            : Stream.concat(Stream.of(selected), Stream.of(extraLabels)).toList();
        return DynamicList.fromList(labels, Function.identity(), selected, false);
    }
}
