package uk.gov.hmcts.reform.civil.service.docmosis.dq.builders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.GetRespondentsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.SetApplicantsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;

@ExtendWith(MockitoExtension.class)
class DQGeneratorFormBuilderTest {

    public static final String ERROR_FLOW_STATE_PAST_DEADLINE =
        "Error evaluating state flow when checking which DQ file name to be used";

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private RepresentativeService representativeService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private GetRespondentsForDQGenerator respondentsForDQGeneratorTask;

    @Mock
    private SetApplicantsForDQGenerator setApplicantsForDQGenerator;

    @Mock
    private RespondentTemplateForDQGenerator respondentTemplateForDQGenerator;

    @Mock
    private StateFlow stateFlow;

    @Mock
    private State state;

    @InjectMocks
    private DQGeneratorFormBuilder dqGeneratorFormBuilder;

    static final String DEFENDANT = "defendant";

    @BeforeEach
    void setUp() {

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(state.getName()).thenReturn(FULL_DEFENCE.fullName());
    }

    @Test
    void shouldGetDirectionsQuestionnaireFormBuilder() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .build().toBuilder()
            .respondent2(PartyBuilder.builder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(caseData, DEFENDANT);

        assertNotNull(result);
        assertThat(result.build().getStatementOfTruthText()).startsWith("The defendant believes");
    }

    @Test
    void shouldGetDirectionsQuestionnaireFormBuilderForPartAdmit() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        when(state.getName()).thenReturn(PART_ADMISSION.fullName());

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmissionSpec()
            .respondent1DQ()
            .build().toBuilder()
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(caseData, DEFENDANT);

        assertNotNull(result);
        assertThat(result.build().getStatementOfTruthText()).startsWith("The defendant believes");
    }

    @Test
    void shouldBuildFormForClaimantResponseWithSpecClaim() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .legacyCaseReference("reference")
            .build().toBuilder()
            .respondent2(PartyBuilder.builder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        when(state.getName()).thenReturn(FULL_ADMISSION.fullName());

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals("reference", result.build().getReferenceNumber());
        assertNull(result.build().getWitnessesIncludingDefendants());

    }

    @Test
    void shouldCountWitnessesIncludingDefendantsForSpecClaimFullAdmission() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(PartyBuilder.builder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals(2, result.build().getWitnessesIncludingDefendants());
    }

    @Test
    void shouldSetApplicantsForNonClaimantResponse() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(PartyBuilder.builder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals(2, result.build().getWitnessesIncludingDefendants());

    }

    @Test
    void shouldReturnForTrueForLipClaimantBilingual() {
        CaseData caseData =
            CaseDataBuilder.builder().respondent1Represented(YesOrNo.YES).applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build().toBuilder().ccdState(
                    CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT).build();
        boolean result = dqGeneratorFormBuilder.isRespondentState(caseData);
        assertTrue(result);
    }

    @Test
    void shouldReturnForFalseForLipClaimantBilingual_ForOtherStates() {
        when(state.getName()).thenReturn(FULL_ADMISSION.fullName());
        CaseData caseData =
            CaseDataBuilder.builder().respondent1Represented(YesOrNo.YES).applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build().toBuilder().ccdState(
                    CaseState.CASE_DISMISSED).build();
        boolean result = dqGeneratorFormBuilder.isRespondentState(caseData);
        assertFalse(result);
    }

    @Test
    void shouldThrowAndExceptionWhenStatePastDeadline() {
        when(state.getName()).thenReturn(PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName());
        CaseData caseData =
            CaseDataBuilder.builder().respondent1Represented(YesOrNo.YES).applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build().toBuilder().ccdState(
                    CaseState.CASE_DISMISSED).build();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            dqGeneratorFormBuilder.isRespondentState(caseData);
        });

        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(ERROR_FLOW_STATE_PAST_DEADLINE));
    }
}
