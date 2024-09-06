package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * history of a case, for DashboardClaimStatusFactory test
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDataHistory {

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class CaseDataMoment {
        private final CaseData caseData;
        private final DashboardClaimStatus claimantStatus;
        private final DashboardClaimStatus defendantStatus;
    }

    private final List<CaseDataMoment> history = new ArrayList<>();

    public static CaseDataHistory smallClaim() {
        CaseDataHistory instance = new CaseDataHistory();
        instance.history.add(new CaseDataMoment(
            CaseData.builder()
                .ccdState(CaseState.CASE_PROGRESSION)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .build(),
            DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED,
            DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED
        ));
        return instance;
    }

    public static CaseDataHistory fastClaim() {
        CaseDataHistory instance = new CaseDataHistory();
        instance.history.add(new CaseDataMoment(
            CaseData.builder()
                .ccdState(CaseState.CASE_PROGRESSION)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build(),
            DashboardClaimStatus.SDO_ORDER_CREATED,
            DashboardClaimStatus.SDO_ORDER_CREATED
        ));
        return instance;
    }

    public CaseDataHistory copy() {
        CaseDataHistory copy = new CaseDataHistory();
        copy.history.addAll(this.history);
        return copy;
    }

    public CaseDataHistory scheduleHearingDays(int days) {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO schedule hearing
            .build();
        DashboardClaimStatus expectedStatus;
        if (days > 6 * 7) {
            expectedStatus = DashboardClaimStatus.TRIAL_OR_HEARING_SCHEDULED;
        } else if (days <= 3 * 7) {
            // TODO create bundle
            expectedStatus = DashboardClaimStatus.BUNDLE_CREATED;
        } else {
            expectedStatus = DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED;
        }
        history.add(new CaseDataMoment(
            caseData,
            expectedStatus,
            expectedStatus
        ));
        return this;
    }

    public CaseDataHistory doNotPayHearingFee() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO hearing fee was not paid
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.HEARING_FEE_UNPAID,
            DashboardClaimStatus.HEARING_FEE_UNPAID
        ));
        return this;
    }

    public CaseDataHistory requestHwF() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO request help with fees
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIM_SUBMIT_HWF,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory invalidHwFReferenceNumber() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory submitClaimantHearingArrangements() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory submitDefendantHearingArrangements() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            history.get(history.size() - 1).claimantStatus,
            DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED
        ));
        return this;
    }

    public CaseDataHistory awaitingJudgment() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.AWAITING_JUDGMENT,
            DashboardClaimStatus.AWAITING_JUDGMENT
        ));
        return this;
    }

    public CaseDataHistory updatedHwFReferenceNumber() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory moreInformationRequiredHwF() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.HWF_MORE_INFORMATION_NEEDED,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory hwfRejected() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory hwfPartial() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory hwfFull() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            // TODO
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    public CaseDataHistory payHearingFee() {
        CaseData caseData = history.get(history.size() - 1).caseData.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.FEE_PAYMENT_OUTCOME)
                                   .build())
            .build();
        history.add(new CaseDataMoment(
            caseData,
            DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
            history.get(history.size() - 1).defendantStatus
        ));
        return this;
    }

    Stream<Arguments> getArguments(
        FeatureToggleService toggleService
    ) {
        return history.stream()
            .flatMap(moment -> Stream.of(
                Arguments.arguments(
                    new CcdDashboardDefendantClaimMatcher(
                        moment.caseData,
                        toggleService
                    ),
                    moment.defendantStatus
                ),
                Arguments.arguments(
                    new CcdDashboardClaimantClaimMatcher(
                        moment.caseData,
                        toggleService
                    ),
                    moment.claimantStatus
                )
            ));
    }
}
