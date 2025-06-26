package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isDefendantNoCOnlineForCaseAfterJBA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;

@ExtendWith(MockitoExtension.class)
class PartAdmissionAgreeRepaymentTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PartAdmissionAgreeRepaymentTransitionBuilder partAdmissionAgreeRepaymentTransitionBuilder =
            new PartAdmissionAgreeRepaymentTransitionBuilder(mockFeatureToggleService);
        result = partAdmissionAgreeRepaymentTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(2);

        assertTransition(result.get(0), "MAIN.PART_ADMIT_AGREE_REPAYMENT", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(1), "MAIN.PART_ADMIT_AGREE_REPAYMENT", "MAIN.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA");
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }

    @Test
    void shouldReturnTrue_whenDefendantLipNocAfterJBA() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .takenOfflineDate(LocalDateTime.now())
            .activeJudgment(JudgmentDetails.builder().type(JudgmentType.JUDGMENT_BY_ADMISSION).build())
            .changeOfRepresentation(ChangeOfRepresentation.builder().build())
            .build();

        assertTrue(isDefendantNoCOnlineForCaseAfterJBA.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseTakeOfflineByStaff() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();

        assertTrue(takenOfflineByStaff.test(caseData));
    }

}
