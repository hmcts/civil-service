package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LIP_RESPONSE;

@ExtendWith(MockitoExtension.class)
class DirectionQuestionnaireLipResponseGeneratorTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseData caseData;
    @InjectMocks
    private DirectionQuestionnaireLipResponseGenerator generator;
    @Mock
    private DQGeneratorFormBuilder dqGeneratorFormBuilder;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;
    @Mock
    private RepresentativeService representativeService;

    private static final String AUTH = "auth";

    @Test
    void shouldReturnLipTemplate_whenLipVLipEnabledAndClaimantLip() {
        //Given
        given(caseData.isApplicantNotRepresented()).willReturn(true);
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isEqualTo(DQ_LIP_RESPONSE);
    }

    @Test
    void shouldReturnLipTempate_whenLipVLipEnabledAndDefendantLip() {
        //Given
        given(caseData.isRespondent1NotRepresented()).willReturn(true);
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isEqualTo(DQ_LIP_RESPONSE);
    }

    @Test
    void shouldNotReturnLipTemplateWhenLrvLr() {
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isNotEqualTo(DQ_LIP_RESPONSE);
    }

    @Test
    void shouldGenerateTemplateDataForIntermediateTrack() {
        //Given
        FixedRecoverableCosts fixedRecoverableCosts = FixedRecoverableCosts.builder()
            .isSubjectToFixedRecoverableCostRegime(YesOrNo.YES)
            .complexityBandingAgreed(YesOrNo.YES)
            .band(ComplexityBand.BAND_1)
            .reasons("reasons")
            .build();
        given(caseData.getApplicant1()).willReturn(Party.builder()
                                                       .partyName("app1")
                                                       .type(Party.Type.COMPANY)
                                                       .build());
        given(caseData.getRespondent1()).willReturn(Party.builder()
                                                       .partyName("res1")
                                                       .type(Party.Type.COMPANY)
                                                       .build());
        given(caseData.getResponseClaimTrack()).willReturn(AllocatedTrack.INTERMEDIATE_CLAIM.name());
        given(caseData.getRespondent1DQ()).willReturn(Respondent1DQ.builder()
                                                         .respondent1DQFixedRecoverableCostsIntermediate(
                                                             fixedRecoverableCosts)
                                                         .specRespondent1DQDisclosureOfElectronicDocuments(
                                                             DisclosureOfElectronicDocuments.builder()
                                                                 .reachedAgreement(YesOrNo.YES)
                                                                 .build()
                                                         )
                                                         .specRespondent1DQDisclosureOfNonElectronicDocuments(
                                                             DisclosureOfNonElectronicDocuments.builder()
                                                                 .bespokeDirections("directions")
                                                                 .build()
                                                         )
                                                         .respondent1DQClaimantDocumentsToBeConsidered(
                                                             DocumentsToBeConsidered.builder()
                                                                 .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                                 .details("details")
                                                                 .build())
                                                         .build());
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder formBuilder = DirectionsQuestionnaireForm.builder();
        when(dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(any(CaseData.class), anyString()))
            .thenReturn(formBuilder);
        //When
        DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, AUTH);

        //Then
        assertThat(templateData.getAllocatedTrack()).isEqualTo("INTERMEDIATE_CLAIM");
        assertThat(templateData.getFixedRecoverableCosts()).isEqualTo(FixedRecoverableCostsSection.from(fixedRecoverableCosts));
        assertThat(templateData.getDisclosureOfElectronicDocuments()).isEqualTo(DisclosureOfElectronicDocuments.builder()
                                                                                    .reachedAgreement(YesOrNo.YES)
                                                                                    .build());
        assertThat(templateData.getDisclosureOfNonElectronicDocuments()).isEqualTo(DisclosureOfNonElectronicDocuments.builder()
                                                                                       .bespokeDirections("directions")
                                                                                       .build());
        assertThat(templateData.getDocumentsToBeConsidered()).isEqualTo(DocumentsToBeConsideredSection.builder()
                                                                            .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                                            .details("details")
                                                                            .sectionHeading("Claimants documents to be considered")
                                                                            .question("Are there any documents the claimants have that you want the court to consider?")
                                                                            .build());
    }

    @Test
    void shouldGenerateTemplateDataForMultiTrack() {
        given(caseData.getApplicant1()).willReturn(Party.builder()
                                                       .partyName("app1")
                                                       .type(Party.Type.COMPANY)
                                                       .build());
        given(caseData.getRespondent1()).willReturn(Party.builder()
                                                        .partyName("res1")
                                                        .type(Party.Type.COMPANY)
                                                        .build());
        given(caseData.getResponseClaimTrack()).willReturn(AllocatedTrack.MULTI_CLAIM.name());
        given(caseData.getRespondent1DQ()).willReturn(Respondent1DQ.builder()
                                                          .specRespondent1DQDisclosureOfElectronicDocuments(
                                                              DisclosureOfElectronicDocuments.builder()
                                                                  .reachedAgreement(YesOrNo.YES)
                                                                  .build()
                                                          )
                                                          .specRespondent1DQDisclosureOfNonElectronicDocuments(
                                                              DisclosureOfNonElectronicDocuments.builder()
                                                                  .bespokeDirections("directions")
                                                                  .build()
                                                          )
                                                          .respondent1DQClaimantDocumentsToBeConsidered(
                                                              DocumentsToBeConsidered.builder()
                                                                  .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                                  .details("details")
                                                                  .build())
                                                          .build());
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder formBuilder = DirectionsQuestionnaireForm.builder();
        when(dqGeneratorFormBuilder.getDirectionsQuestionnaireFormBuilder(any(CaseData.class), anyString()))
            .thenReturn(formBuilder);
        //When
        DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, AUTH);

        //Then
        assertThat(templateData.getAllocatedTrack()).isEqualTo("MULTI_CLAIM");
        assertThat(templateData.getDisclosureOfElectronicDocuments()).isEqualTo(DisclosureOfElectronicDocuments.builder()
                                                                                    .reachedAgreement(YesOrNo.YES)
                                                                                    .build());
        assertThat(templateData.getDisclosureOfNonElectronicDocuments()).isEqualTo(DisclosureOfNonElectronicDocuments.builder()
                                                                                       .bespokeDirections("directions")
                                                                                       .build());
        assertThat(templateData.getDocumentsToBeConsidered()).isEqualTo(DocumentsToBeConsideredSection.builder()
                                                                            .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                                            .details("details")
                                                                            .sectionHeading("Claimants documents to be considered")
                                                                            .question("Are there any documents the claimants have that you want the court to consider?")
                                                                            .build());
    }
}
