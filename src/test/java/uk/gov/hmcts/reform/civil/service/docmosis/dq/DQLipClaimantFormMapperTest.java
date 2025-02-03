package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DQLipClaimantFormMapperTest {

    @Mock
    private CaseDataLiP caseDataLiPMock;
    @Mock
    private DQExtraDetailsLip dqExtraDetailsLipMock;
    @Mock
    private ExpertLiP expertLiPMock;
    @Mock
    private ClaimantLiPResponse claimantLiPResponse;
    @Mock
    private CaseData caseData;
    @Mock
    private Party applicant1;
    @InjectMocks
    private DQLipClaimantFormMapper dqLipClaimantFormMapper;
    private DirectionsQuestionnaireForm form;
    private static final String NAME = "Claimant";

    @BeforeEach
    void setUp() {
        form = DirectionsQuestionnaireForm.builder().build();
    }

    @Test
    void shouldNotPopulateDtoWithLipData_whenCaseDataLipIsNull() {
        //Given
        Optional<CaseDataLiP> emptyOptional = Optional.empty();
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, emptyOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldNotPopulateLipExtraDQ_whenDQExtraDetailsLipIsNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getApplicant1LiPResponse()).willReturn(claimantLiPResponse);
        given(claimantLiPResponse.getApplicant1DQExtraDetails()).willReturn(null);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldPopulateLipExtraDQ_whenDQExtraDetailsLipIsNotNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getApplicant1LiPResponse()).willReturn(claimantLiPResponse);
        given(claimantLiPResponse.getApplicant1DQExtraDetails()).willReturn(dqExtraDetailsLipMock);
        given(dqExtraDetailsLipMock.getApplicant1DQLiPExpert()).willReturn(expertLiPMock);
        given(dqExtraDetailsLipMock.getTriedToSettle()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getRequestExtra4weeks()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getGiveEvidenceYourSelf()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getConsiderClaimantDocumentsDetails()).willReturn("test");
        given(dqExtraDetailsLipMock.getDeterminationWithoutHearingRequired()).willReturn(YesOrNo.NO);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNotNull();
    }

    @Test
    void shouldPopulateLipDQ_whenDQIsNotNullMinti() {
        //Given
        given(caseData.getApplicant1DQ()).willReturn(Applicant1DQ.builder()
                                                         .applicant1DQFixedRecoverableCostsIntermediate(FixedRecoverableCosts.builder()
                                                                                                            .isSubjectToFixedRecoverableCostRegime(YesOrNo.YES)
                                                                                                            .complexityBandingAgreed(YesOrNo.YES)
                                                                                                            .band(ComplexityBand.BAND_1)
                                                                                                            .reasons("reasons")
                                                                                                            .build())
                                                         .specApplicant1DQDisclosureOfElectronicDocuments(DisclosureOfElectronicDocuments.builder()
                                                                                                               .reachedAgreement(YesOrNo.YES)
                                                                                                               .build())
                                                         .specApplicant1DQDisclosureOfNonElectronicDocuments(DisclosureOfNonElectronicDocuments.builder()
                                                                                                                 .bespokeDirections("directions")
                                                                                                                 .build())
                                                         .applicant1DQDefendantDocumentsToBeConsidered(DocumentsToBeConsidered.builder()
                                                                                                           .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                                                                           .details("details")
                                                                                                           .build())
                                                         .build());
        //When
        FixedRecoverableCostsSection expectedFrc = dqLipClaimantFormMapper.getFixedRecoverableCostsIntermediate(caseData);
        DisclosureOfElectronicDocuments expectedEletronicDisclosure = dqLipClaimantFormMapper.getDisclosureOfElectronicDocuments(caseData);
        DisclosureOfNonElectronicDocuments expectedNonEletronicDisclosure = dqLipClaimantFormMapper.getDisclosureOfNonElectronicDocuments(caseData);
        DocumentsToBeConsideredSection expectedDocsToBeConsidered = dqLipClaimantFormMapper.getDocumentsToBeConsidered(caseData);
        //Then
        assertThat(expectedFrc).isEqualTo(FixedRecoverableCostsSection.builder()
                                              .isSubjectToFixedRecoverableCostRegime(YesOrNo.YES)
                                              .complexityBandingAgreed(YesOrNo.YES)
                                              .reasons("reasons")
                                              .band(ComplexityBand.BAND_1)
                                              .bandText("Band 1")
                                              .build());

        assertThat(expectedEletronicDisclosure).isEqualTo(DisclosureOfElectronicDocuments.builder()
                                                                          .reachedAgreement(YesOrNo.YES).build());

        assertThat(expectedNonEletronicDisclosure).isEqualTo(DisclosureOfNonElectronicDocuments.builder()
                                                                 .bespokeDirections("directions")
                                                                 .build());

        assertThat(expectedDocsToBeConsidered).isEqualTo(DocumentsToBeConsideredSection.builder()
                                                             .hasDocumentsToBeConsidered(YesOrNo.YES)
                                                             .details("details")
                                                             .sectionHeading("Defendants documents to be considered")
                                                             .question("Are there any documents the defendants have that you want the court to consider?")
                                                             .build());
    }

    @Test
    void shouldReturnClaimantSignature_whenGetStatementOfTruth() {
        //Given
        given(caseData.getApplicant1()).willReturn(applicant1);
        given(applicant1.getPartyName()).willReturn(NAME);
        //When
        String result = dqLipClaimantFormMapper.getStatementOfTruthName(caseData);
        //Then
        assertThat(result).isEqualTo(NAME);
        verify(caseData).getApplicant1();
    }
}
