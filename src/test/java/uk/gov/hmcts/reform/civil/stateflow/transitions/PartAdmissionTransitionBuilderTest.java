package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.agreePartAdmitSettle;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.partAdmitPayImmediately;

@ExtendWith(MockitoExtension.class)
public class PartAdmissionTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PartAdmissionTransitionBuilder partAdmissionTransitionBuilder = new PartAdmissionTransitionBuilder(
            mockFeatureToggleService);
        result = partAdmissionTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(10);

        assertTransition(result.get(0), "MAIN.PART_ADMISSION", "MAIN.IN_MEDIATION");
        assertTransition(result.get(1), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION");
        assertTransition(result.get(2), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_PROCEED");
        assertTransition(result.get(3), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_NOT_PROCEED");
        assertTransition(result.get(4), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        assertTransition(result.get(5), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_AGREE_SETTLE");
        assertTransition(result.get(6), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_AGREE_REPAYMENT");
        assertTransition(result.get(7), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_REJECT_REPAYMENT");
        assertTransition(result.get(8), "MAIN.PART_ADMISSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(9), "MAIN.PART_ADMISSION", "MAIN.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnTrue_whenPartAdmitClaimIsSettled() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertTrue(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsPartAdmitClaimSpecIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantIntentionSettlePartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantConfirmAmountPaidPartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseAccessCategoryIsNotSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant1AcceptAdmitAmountPaidSpecIsNotYes() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNull() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(null)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNotOneVOnePartAdmitPayImmediately() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
