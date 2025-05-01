package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DQLipDefendantFormMapperTest {

    private static final String NAME = "Defendant";
    @Mock
    private CaseDataLiP caseDataLiPMock;
    @Mock
    private DQExtraDetailsLip dqExtraDetailsLipMock;
    @Mock
    private ExpertLiP expertLiPMock;
    @Mock
    private RespondentLiPResponse respondentLiPResponse;
    @Mock
    private CaseData caseData;
    @Mock
    private Party respondent1;
    @InjectMocks
    private DQLipDefendantFormMapper dqLipDefendantFormMapper;
    private DirectionsQuestionnaireForm form;

    @BeforeEach
    void setUp() {
        form = DirectionsQuestionnaireForm.builder().build();
    }

    @Test
    void shouldPopulateLipExtraDQ_whenDQExtraDetailsLipIsNotNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getRespondent1LiPResponse()).willReturn(respondentLiPResponse);
        given(respondentLiPResponse.getRespondent1DQExtraDetails()).willReturn(dqExtraDetailsLipMock);
        given(dqExtraDetailsLipMock.getRespondent1DQLiPExpert()).willReturn(expertLiPMock);
        given(dqExtraDetailsLipMock.getTriedToSettle()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getRequestExtra4weeks()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getGiveEvidenceYourSelf()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getConsiderClaimantDocumentsDetails()).willReturn("test");
        given(dqExtraDetailsLipMock.getDeterminationWithoutHearingRequired()).willReturn(YesOrNo.NO);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipDefendantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNotNull();
    }

    @Test
    void shouldPopulateLipDQ_whenDQIsNotNullMinti() {
        //Given
        given(caseData.getRespondent1DQ()).willReturn(Respondent1DQ.builder()
                                                         .respondent1DQFixedRecoverableCostsIntermediate(
                                                             FixedRecoverableCosts.builder()
                                                                 .isSubjectToFixedRecoverableCostRegime(YesOrNo.NO)
                                                                 .reasons("reasons")
                                                                 .build())
                                                         .specRespondent1DQDisclosureOfElectronicDocuments(
                                                             DisclosureOfElectronicDocuments.builder()
                                                                 .reachedAgreement(YesOrNo.NO)
                                                                 .agreementLikely(YesOrNo.NO)
                                                                 .reasonForNoAgreement("no")
                                                                 .build())
                                                         .specRespondent1DQDisclosureOfNonElectronicDocuments(
                                                             DisclosureOfNonElectronicDocuments.builder()
                                                                 .bespokeDirections("directions")
                                                                 .build())
                                                         .respondent1DQClaimantDocumentsToBeConsidered(
                                                             DocumentsToBeConsidered.builder()
                                                                 .hasDocumentsToBeConsidered(YesOrNo.NO)
                                                                 .details("details")
                                                                 .build())
                                                         .build());
        //When
        FixedRecoverableCostsSection expectedFrc = dqLipDefendantFormMapper.getFixedRecoverableCostsIntermediate(caseData);
        DisclosureOfElectronicDocuments expectedEletronicDisclosure = dqLipDefendantFormMapper.getDisclosureOfElectronicDocuments(caseData);
        DisclosureOfNonElectronicDocuments expectedNonEletronicDisclosure = dqLipDefendantFormMapper.getDisclosureOfNonElectronicDocuments(caseData);
        DocumentsToBeConsideredSection expectedDocsToBeConsidered = dqLipDefendantFormMapper.getDocumentsToBeConsidered(caseData);
        //Then
        assertThat(expectedFrc).isEqualTo(FixedRecoverableCostsSection.builder()
                                              .isSubjectToFixedRecoverableCostRegime(YesOrNo.NO)
                                              .reasons("reasons")
                                              .build());

        assertThat(expectedEletronicDisclosure).isEqualTo(DisclosureOfElectronicDocuments.builder()
                                                              .reachedAgreement(YesOrNo.NO)
                                                              .agreementLikely(YesOrNo.NO)
                                                              .reasonForNoAgreement("no")
                                                              .build());

        assertThat(expectedNonEletronicDisclosure).isEqualTo(DisclosureOfNonElectronicDocuments.builder()
                                                                 .bespokeDirections("directions")
                                                                 .build());

        assertThat(expectedDocsToBeConsidered).isEqualTo(DocumentsToBeConsideredSection.builder()
                                                             .hasDocumentsToBeConsidered(YesOrNo.NO)
                                                             .details("details")
                                                             .sectionHeading("Claimants documents to be considered")
                                                             .question("Are there any documents the claimants have that you want the court to consider?")
                                                             .build());
    }

    @Test
    void shouldReturnClaimantSignature_whenGetStatementOfTruth() {
        //Given
        given(caseData.getRespondent1()).willReturn(respondent1);
        given(respondent1.getPartyName()).willReturn(NAME);
        //When
        String result = dqLipDefendantFormMapper.getStatementOfTruthName(caseData);
        //Then
        assertThat(result).isEqualTo(NAME);
        verify(caseData).getRespondent1();
    }
}
