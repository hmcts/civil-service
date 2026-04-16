package uk.gov.hmcts.reform.civil.service.docmosis.dq.builders;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
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

    private uk.gov.hmcts.reform.civil.model.BusinessProcess createBusinessProcess(String camundaEvent) {
        uk.gov.hmcts.reform.civil.model.BusinessProcess businessProcess = new uk.gov.hmcts.reform.civil.model.BusinessProcess();
        businessProcess.setCamundaEvent(camundaEvent);
        return businessProcess;
    }

    @Test
    void shouldGetDirectionsQuestionnaireFormBuilder() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .build().toBuilder()
            .respondent2(new PartyBuilder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertThat(result.getStatementOfTruthText()).startsWith("The defendant believes");
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

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertThat(result.getStatementOfTruthText()).startsWith("The defendant believes");
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
            .respondent2(new PartyBuilder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        when(state.getName()).thenReturn(FULL_ADMISSION.fullName());

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals("reference", result.getReferenceNumber());
        assertNull(result.getWitnessesIncludingDefendants());

    }

    @Test
    void shouldCountWitnessesIncludingDefendantsForSpecClaimFullAdmission() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(new PartyBuilder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals(2, result.getWitnessesIncludingDefendants());
    }

    @Test
    void shouldSetApplicantsForNonClaimantResponse() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent2DQWithFixedRecoverableCosts()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(new PartyBuilder()
                             .individual()
                             .legalRepHeading()
                             .build())
            .build();

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertEquals(2, result.getWitnessesIncludingDefendants());

    }

    @Test
    void shouldReturnForTrueForLipClaimantBilingual() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData caseData =
            CaseDataBuilder.builder().respondent1Represented(YesOrNo.YES).applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build().toBuilder().ccdState(
                    CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT).build();
        boolean result = dqGeneratorFormBuilder.isRespondentState(caseData);
        assertTrue(result);
    }

    @Test
    void shouldReturnForFalseForLipClaimantBilingual_ForOtherStates() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
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

        Exception exception = assertThrows(
            IllegalStateException.class, () -> {
                dqGeneratorFormBuilder.isRespondentState(caseData);
            }
        );

        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(ERROR_FLOW_STATE_PAST_DEADLINE));
    }

    @Test
    void shouldNotSetStatementOfTruthTextWhenSpecClaim() {
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent1DQ()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        assertNotNull(result);
        assertNull(result.getStatementOfTruthText());
    }

    @Test
    void shouldCorrectlyMapFutureApplicationsForDefendant() {
        // Given: Defendant DQ with future applications set to YES
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications()
            .setIntentionToMakeFutureApplications(YesOrNo.YES)
            .setWhatWillFutureApplicationsBeMadeFor("Test application details for defendant");

        Respondent1DQ respondent1DQ = new Respondent1DQ()
            .setRespondent1DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1DQ(respondent1DQ)
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should be YES and reason should be populated
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertEquals("Test application details for defendant", result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldCorrectlyMapFutureApplicationsForClaimant() {
        // Given: Claimant DQ with future applications set to YES
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications()
            .setIntentionToMakeFutureApplications(YesOrNo.YES)
            .setWhatWillFutureApplicationsBeMadeFor("Test application details for claimant");

        Applicant1DQ applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(applicant1DQ)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should be YES and reason should be populated
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertEquals("Test application details for claimant", result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldDefaultToNoWhenFutureApplicationsNotSetForDefendant() {
        // Given: Defendant DQ without future applications
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Respondent1DQ respondent1DQ = new Respondent1DQ();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1DQ(respondent1DQ)
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should default to NO
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldDefaultToNoWhenFutureApplicationsNotSetForClaimant() {
        // Given: Claimant DQ without future applications
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Applicant1DQ applicant1DQ = new Applicant1DQ();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(applicant1DQ)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should default to NO
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleFutureApplicationsSetToNoForClaimant() {
        // Given: Claimant DQ with future applications explicitly set to NO
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications()
            .setIntentionToMakeFutureApplications(YesOrNo.NO);

        Applicant1DQ applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(applicant1DQ)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should be NO and reason should be null
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleNullApplicant1DQFutureApplicationsFieldWithoutNPE() {
        // Given: Applicant1DQ with null FutureApplications field
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Applicant1DQ applicant1DQ = new Applicant1DQ();
        // Explicitly set futureApplications to null
        applicant1DQ.setApplicant1DQFutureApplications(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(applicant1DQ)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form - should not throw NPE
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Should default to NO without NPE
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleNullRespondent1DQFutureApplicationsFieldWithoutNPE() {
        // Given: Respondent1DQ with null FutureApplications field
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Respondent1DQ respondent1DQ = new Respondent1DQ();
        // Explicitly set futureApplications to null
        respondent1DQ.setRespondent1DQFutureApplications(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1DQ(respondent1DQ)
            .build();

        // When: Generate DQ form - should not throw NPE
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Should default to NO without NPE
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleNullFutureApplicationsIntentionFieldWithoutNPE() {
        // Given: FutureApplications object with null intentionToMakeFutureApplications
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications();
        // Both fields are null
        futureApplications.setIntentionToMakeFutureApplications(null);
        futureApplications.setWhatWillFutureApplicationsBeMadeFor(null);

        Applicant1DQ applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1DQ(applicant1DQ)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form - should not throw NPE
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Should default to NO without NPE
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldCorrectlyMapFutureApplicationsForApplicant2() {
        // Given: Applicant2DQ with future applications set to YES (2v1 scenario - only applicant2 proceeding)
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications()
            .setIntentionToMakeFutureApplications(YesOrNo.YES)
            .setWhatWillFutureApplicationsBeMadeFor("Test application details for applicant2");

        Applicant2DQ applicant2DQ = new Applicant2DQ()
            .setApplicant2DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant2DQ(applicant2DQ)
            .applicant2ResponseDate(java.time.LocalDateTime.now())  // Required for applicant2
            .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)  // Only applicant2 is proceeding
            .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.YES)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should be YES and reason should be populated
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertEquals("Test application details for applicant2", result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldCorrectlyMapFutureApplicationsForRespondent2() {
        // Given: Respondent2DQ with future applications set to YES
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        FutureApplications futureApplications = new FutureApplications()
            .setIntentionToMakeFutureApplications(YesOrNo.YES)
            .setWhatWillFutureApplicationsBeMadeFor("Test application details for respondent2");

        Respondent2DQ respondent2DQ = new Respondent2DQ()
            .setRespondent2DQFutureApplications(futureApplications);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2DQ(respondent2DQ)
            .respondent2ResponseDate(java.time.LocalDateTime.now())  // Required for respondent2
            .respondent1ResponseDate(null)  // Only respondent2 responded
            .build();

        // When: Generate DQ form
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Future applications should be YES and reason should be populated
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getFutureApplications());
        assertEquals(YesOrNo.YES, result.getFurtherInformation().getIntentionToMakeFutureApplications());
        assertEquals("Test application details for respondent2", result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleNullApplicant2DQFutureApplicationsFieldWithoutNPE() {
        // Given: Applicant2DQ with null FutureApplications field
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Applicant2DQ applicant2DQ = new Applicant2DQ();
        applicant2DQ.setApplicant2DQFutureApplications(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant2DQ(applicant2DQ)
            .applicant2ResponseDate(java.time.LocalDateTime.now())  // Required for applicant2
            .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)
            .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.YES)
            .businessProcess(createBusinessProcess("CLAIMANT_RESPONSE_SPEC"))
            .build();

        // When: Generate DQ form - should not throw NPE
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Should default to NO without NPE
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }

    @Test
    void shouldHandleNullRespondent2DQFutureApplicationsFieldWithoutNPE() {
        // Given: Respondent2DQ with null FutureApplications field
        Witnesses mockWitnesses = mock(Witnesses.class);
        when(respondentTemplateForDQGenerator.getWitnesses(any())).thenReturn(mockWitnesses);

        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQFutureApplications(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2DQ(respondent2DQ)
            .respondent2ResponseDate(java.time.LocalDateTime.now())  // Required for respondent2
            .respondent1ResponseDate(null)
            .build();

        // When: Generate DQ form - should not throw NPE
        DirectionsQuestionnaireForm result =
            dqGeneratorFormBuilder.getDirectionsQuestionnaireForm(caseData, DEFENDANT);

        // Then: Should default to NO without NPE
        assertNotNull(result);
        assertNotNull(result.getFurtherInformation());
        assertEquals(YesOrNo.NO, result.getFurtherInformation().getFutureApplications());
        assertNull(result.getFurtherInformation().getReasonForFutureApplications());
    }
}
