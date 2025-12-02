package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class DirectionsQuestionnairePreparerTest {

    @Mock
    private DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator;

    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DirectionsQuestionnairePreparer preparer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_singleResponse() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setDuplicateSystemGeneratedCaseDocs(new ArrayList<>());
        String userToken = "userToken";

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName("directionsQuestionnaire");
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), eq(userToken)))
            .thenReturn(caseDocument);

        // When
        CaseData result = preparer.prepareDirectionsQuestionnaire(caseData, userToken);

        // Then
        verify(directionsQuestionnaireGenerator).generate(any(CaseData.class), eq(userToken));
        verify(assignCategoryId).copyCaseDocumentWithCategoryId(any(CaseDocument.class), eq(""));
        assertEquals(1, result.getSystemGeneratedCaseDocuments().size());
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_singleResponse_ClaimantDqPreTranslation() {
        // Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setApplicant1Represented(YES);
        caseData.setRespondent1Represented(NO);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(new ArrayList<>());
        String userToken = "userToken";

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName("directionsQuestionnaire");
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), eq(userToken)))
            .thenReturn(caseDocument);

        // When
        CaseData result = preparer.prepareDirectionsQuestionnaire(caseData, userToken);

        // Then
        verify(directionsQuestionnaireGenerator).generate(any(CaseData.class), eq(userToken));
        assertEquals(1, result.getPreTranslationDocuments().size());
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_singleResponseForWelshLip() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        caseData.setClaimantBilingualLanguagePreference("BOTH");
        caseData.setSpecRespondent1Represented(YES);
        caseData.setApplicant1Represented(NO);
        caseData.setDuplicateSystemGeneratedCaseDocs(new ArrayList<>());
        String userToken = "userToken";

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName("directionsQuestionnaire");
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), eq(userToken)))
            .thenReturn(caseDocument);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        // When
        CaseData result = preparer.prepareDirectionsQuestionnaire(caseData, userToken);

        // Then
        verify(directionsQuestionnaireGenerator).generate(any(CaseData.class), eq(userToken));
        verify(assignCategoryId).copyCaseDocumentWithCategoryId(any(CaseDocument.class), eq(""));
        assertThat(result.getRespondent1OriginalDqDoc()).isNotNull();
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_respondent2HasSameLegalRep() {
        // Given
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.ORGANISATION);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setRespondentResponseIsSame(NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setRespondent2SameLegalRepresentative(YES);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent2DQ(new Respondent2DQ());
        caseData.setRespondent2(respondent2);
        caseData.setRespondent1(respondent1);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());

        String userToken = "userToken";

        Document document1 = new Document();
        document1.setDocumentUrl("documentUrl1");
        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentName("directionsQuestionnaire1");
        caseDocument1.setDocumentLink(document1);
        when(directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(any(CaseData.class), eq(userToken), eq("ONE")))
            .thenReturn(caseDocument1);

        // When
        CaseData result = preparer.prepareDirectionsQuestionnaire(caseData, userToken);

        // Then
        verify(directionsQuestionnaireGenerator).generateDQFor1v2SingleSolDiffResponse(any(CaseData.class), eq(userToken), eq("ONE"));
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(CaseDocument.class), eq("DQRespondent"));
        assertEquals(1, result.getSystemGeneratedCaseDocuments().size());
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_differentLegalRep() {
        // Given
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.ORGANISATION);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseTypeForSpec(PART_ADMISSION);
        caseData.setRespondent2SameLegalRepresentative(NO);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent2DQ(new Respondent2DQ());
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setRespondent2(respondent2);
        caseData.setRespondent1(respondent1);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        String userToken = "userToken";

        Document document1 = new Document();
        document1.setDocumentUrl("documentUrl1");
        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentName("directionsQuestionnaire1");
        caseDocument1.setDocumentLink(document1);
        Document document2 = new Document();
        document2.setDocumentUrl("documentUrl1");
        CaseDocument caseDocument2 = new CaseDocument();
        caseDocument2.setDocumentName("directionsQuestionnaire2");
        caseDocument2.setDocumentLink(document2);

        when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(any(CaseData.class), eq(userToken), eq("ONE")))
            .thenReturn(Optional.of(caseDocument1));
        when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(any(CaseData.class), eq(userToken), eq("TWO")))
            .thenReturn(Optional.of(caseDocument2));

        // When
        CaseData result = preparer.prepareDirectionsQuestionnaire(caseData, userToken);

        // Then
        verify(directionsQuestionnaireGenerator).generateDQFor1v2DiffSol(any(CaseData.class), eq(userToken), eq("ONE"));
        verify(directionsQuestionnaireGenerator).generateDQFor1v2DiffSol(any(CaseData.class), eq(userToken), eq("TWO"));
        assertEquals(2, result.getSystemGeneratedCaseDocuments().size());
    }
}
