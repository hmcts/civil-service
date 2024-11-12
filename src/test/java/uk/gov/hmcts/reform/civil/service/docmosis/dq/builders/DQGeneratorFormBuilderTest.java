package uk.gov.hmcts.reform.civil.service.docmosis.dq.builders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;

@ExtendWith(MockitoExtension.class)
class DQGeneratorFormBuilderTest {

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

        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(state.getName()).thenReturn(FULL_DEFENCE.fullName());
    }

    @Test
    void shouldGetDirectionsQuestionnaireFormBuilder() {

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

    }

    @Test
    void shouldBuildFormForClaimantResponseWithSpecClaim() {

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
}
