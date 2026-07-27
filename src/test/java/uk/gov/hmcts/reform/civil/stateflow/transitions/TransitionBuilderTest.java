package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;

@ExtendWith(MockitoExtension.class)
class TransitionBuilderTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private TestTransitionBuilder transitionBuilder;

    @BeforeEach
    void setUp() {
        transitionBuilder = new TestTransitionBuilder(featureToggleService);
    }

    @Test
    void shouldConfigureCurrentTransition() {
        List<Transition> transitions = new ArrayList<>();
        Predicate<CaseData> condition = caseData -> true;
        Consumer<Map<String, Boolean>> flags = flagMap -> flagMap.put("FLAG", true);

        transitionBuilder.moveTo(CLAIM_SUBMITTED, transitions)
            .onlyWhen(condition, transitions)
            .set(flags, transitions);

        assertThat(transitions).hasSize(1);
        assertThat(transitions.get(0))
            .extracting(Transition::getSourceState, Transition::getTargetState, Transition::getCondition,
                        Transition::getFlags)
            .containsExactly("MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED", condition, flags);
    }

    @Test
    void shouldThrowWhenOnlyWhenIsCalledBeforeMoveTo() {
        List<Transition> transitions = new ArrayList<>();

        assertThatThrownBy(() -> transitionBuilder.onlyWhen(caseData -> true, transitions))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No transition has been defined. Call moveTo before onlyWhen or set.");
    }

    @Test
    void shouldThrowWhenStaticSetIsCalledBeforeMoveTo() {
        List<Transition> transitions = new ArrayList<>();

        assertThatThrownBy(() -> transitionBuilder.set(flags -> flags.put("FLAG", true), transitions))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No transition has been defined. Call moveTo before onlyWhen or set.");
    }

    @Test
    void shouldThrowWhenDynamicSetIsCalledBeforeMoveTo() {
        List<Transition> transitions = new ArrayList<>();

        assertThatThrownBy(() -> transitionBuilder.set((caseData, flags) -> flags.put("FLAG", true), transitions))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No transition has been defined. Call moveTo before onlyWhen or set.");
    }

    @Test
    void shouldDelegateDefendantNocPredicateToFeatureToggleService() {
        CaseData caseData = CaseData.builder().build();
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        assertThat(transitionBuilder.isDefendantNoCOnlineForCase(caseData)).isTrue();
        verify(featureToggleService).isDefendantNoCOnlineForCase(caseData);
    }

    @Test
    void shouldSetRespondentResponseLanguageFlag() {
        CaseData caseData = CaseData.builder()
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1LiPResponse(new RespondentLiPResponse()
                                                             .setRespondent1ResponseLanguage(Language.WELSH.name())))
            .build();
        Map<String, Boolean> flags = new HashMap<>();

        transitionBuilder.applyRespondentResponseLanguageFlag(caseData, flags);

        assertThat(flags).containsEntry(FlowFlag.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.name(), true);
    }

    @Test
    void shouldSetJudicialReferralFlagsUsingMintiFeatureToggleOnce() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1ProceedWithClaim(YES)
            .build();
        Map<String, Boolean> flags = new HashMap<>();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        transitionBuilder.applyJudicialReferralFlags(caseData, flags);

        assertThat(flags).containsEntry(FlowFlag.MINTI_ENABLED.name(), true);
        assertThat(flags).containsEntry(FlowFlag.SDO_ENABLED.name(), true);
        verify(featureToggleService).isMultiOrIntermediateTrackEnabled(caseData);
    }

    @Test
    void shouldSetLipJudgmentAdmissionFlag() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .specRespondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
            .ccjPaymentDetails(new CCJPaymentDetails()
                                   .setCcjPaymentPaidSomeOption(YES)
                                   .setCcjPaymentPaidSomeAmount(BigDecimal.valueOf(600))
                                   .setCcjJudgmentLipInterest(BigDecimal.valueOf(300))
                                   .setCcjJudgmentAmountClaimFee(BigDecimal.ZERO))
            .build();
        Map<String, Boolean> flags = new HashMap<>();

        transitionBuilder.applyLipJudgmentAdmissionFlag(caseData, flags);

        assertThat(flags).containsEntry(FlowFlag.LIP_JUDGMENT_ADMISSION.name(), true);
    }

    private static class TestTransitionBuilder extends TransitionBuilder {

        private TestTransitionBuilder(FeatureToggleService featureToggleService) {
            super(FlowState.Main.DRAFT, featureToggleService);
        }

        @Override
        void setUpTransitions(List<Transition> transitions) {
        }

        private boolean isDefendantNoCOnlineForCase(CaseData caseData) {
            return defendantNoCOnlineForCase().test(caseData);
        }

        private void applyRespondentResponseLanguageFlag(CaseData caseData, Map<String, Boolean> flags) {
            setRespondentResponseLanguageFlag(caseData, flags);
        }

        private void applyJudicialReferralFlags(CaseData caseData, Map<String, Boolean> flags) {
            setJudicialReferralFlags(caseData, flags);
        }

        private void applyLipJudgmentAdmissionFlag(CaseData caseData, Map<String, Boolean> flags) {
            setLipJudgmentAdmissionFlag(caseData, flags);
        }
    }
}
