package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashboardClaimStatusFactoryTest {

    private final DashboardClaimStatusFactory claimStatusFactory = new DashboardClaimStatusFactory();

    static Stream<Arguments> caseToExpectedStatus() {
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        Mockito.when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

        return getCaseToExpectedStatusCp(toggleService);
    }

    static Stream<Arguments> getCaseToExpectedStatusCp(FeatureToggleService toggleService) {
        List<Arguments> list = new ArrayList<>();

        CaseData caseDataJudge = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CaseData caseDataLegalAdvisor = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(999))
            .build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.SDO_ORDER_CREATED
        );
        addClaimImplementations(list, toggleService, caseDataLegalAdvisor,
                                DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED
        );

        // TODO schedule a hearing more than 6 weeks from now
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.TRIAL_OR_HEARING_SCHEDULED,
                                DashboardClaimStatus.TRIAL_OR_HEARING_SCHEDULED);

        caseDataJudge = addPaymentOptions(caseDataJudge,
                                          list, toggleService,
                                          DashboardClaimStatus.SDO_ORDER_CREATED);

        // TODO hearing is 6 weeks from now
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED);

        // TODO submit claimant arrangements
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED);

        // TODO submit defendant arrangements
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
                                DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED);

        // TODO hearing is 3 weeks from now, bundle is created
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.BUNDLE_CREATED,
                                DashboardClaimStatus.BUNDLE_CREATED);

        // TODO awaiting judgement
        caseDataJudge = caseDataJudge.toBuilder().build();
        addClaimImplementations(list, toggleService, caseDataJudge,
                                DashboardClaimStatus.AWAITING_JUDGMENT,
                                DashboardClaimStatus.AWAITING_JUDGMENT);

        // TODO interweave ORDER_MADE status because of All_FINAL_ORDERS_ISSUED, GENERATE_DIRECTIONS_ORDER, COURT_OFFICER_ORDER, STANDARD_DIRECTION_ORDER_DJ

        return list.stream();
    }

    private static CaseData addPaymentOptions(CaseData caseData,
                                              List<Arguments> argumentsList,
                                              FeatureToggleService toggleService,
                                              DashboardClaimStatus defendantStatus) {
        // doesn't pay
        addClaimImplementations(argumentsList, toggleService,
                                caseData.toBuilder().build(),
                                DashboardClaimStatus.HEARING_FEE_UNPAID,
                                DashboardClaimStatus.HEARING_FEE_UNPAID
        );

        CaseData paid = caseData.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.FEE_PAYMENT_OUTCOME)
                                   .build())
            .build();
        addClaimImplementations(argumentsList, toggleService, paid,
                                DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
                                defendantStatus
        );

        CaseData requested = caseData.toBuilder()
            .build();
        addClaimImplementations(argumentsList, toggleService, requested,
                                DashboardClaimStatus.CLAIM_SUBMIT_HWF,
                                defendantStatus
        );

        requested = requested.toBuilder().build();
        addClaimImplementations(argumentsList, toggleService, requested,
                                DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER,
                                defendantStatus
        );

        requested = requested.toBuilder().build();
        addClaimImplementations(argumentsList, toggleService, requested,
                                DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER,
                                defendantStatus
        );

        requested = requested.toBuilder().build();
        addClaimImplementations(argumentsList, toggleService, requested,
                                DashboardClaimStatus.HWF_MORE_INFORMATION_NEEDED,
                                defendantStatus
        );

        addClaimImplementations(argumentsList, toggleService,
                                requested.toBuilder().build(),
                                DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION,
                                defendantStatus
        );

        addClaimImplementations(argumentsList, toggleService,
                                requested.toBuilder().build(),
                                DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION,
                                defendantStatus
        );

        addClaimImplementations(argumentsList, toggleService,
                                requested.toBuilder().build(),
                                DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
                                defendantStatus
        );

        return paid;
    }

    private static void addClaimImplementations(List<Arguments> argumentList,
                                                FeatureToggleService toggleService,
                                                CaseData caseData,
                                                DashboardClaimStatus expectedStatus) {
        addClaimImplementations(argumentList, toggleService, caseData,
                                expectedStatus, expectedStatus
        );
    }

    private static void addClaimImplementations(List<Arguments> argumentList,
                                                FeatureToggleService toggleService,
                                                CaseData caseData,
                                                DashboardClaimStatus expectedStatusClaimant,
                                                DashboardClaimStatus expectedStatusDefendant) {
        argumentList.add(Arguments.arguments(
            new CcdDashboardDefendantClaimMatcher(caseData, toggleService),
            expectedStatusDefendant
        ));
        argumentList.add(Arguments.arguments(
            new CcdDashboardClaimantClaimMatcher(caseData, toggleService),
            expectedStatusClaimant
        ));
    }

    @ParameterizedTest
    @MethodSource("caseToExpectedStatus")
    void shouldReturnCorrectStatus_whenInvoked(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }
}
