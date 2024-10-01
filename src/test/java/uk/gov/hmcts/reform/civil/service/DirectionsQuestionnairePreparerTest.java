package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.Optional;

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

    @InjectMocks
    private DirectionsQuestionnairePreparer preparer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldPrepareDirectionsQuestionnaire_singleResponse() {
        // Given
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        String userToken = "userToken";

        CaseDocument caseDocument = CaseDocument.builder().documentName("directionsQuestionnaire").build();
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
    void shouldPrepareDirectionsQuestionnaire_respondent2HasSameLegalRep() {
        // Given
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondentResponseIsSame(NO)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent2SameLegalRepresentative(YES)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(Party.builder().type(Party.Type.ORGANISATION).build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .systemGeneratedCaseDocuments(new ArrayList<>())

            .build();

        String userToken = "userToken";

        CaseDocument caseDocument1 =
            CaseDocument.builder().documentName("directionsQuestionnaire1").documentLink(Document.builder().documentUrl("documentUrl1").build())
                .build();
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
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(PART_ADMISSION)
            .respondent2SameLegalRepresentative(NO)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent2(Party.builder().type(Party.Type.ORGANISATION).build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        String userToken = "userToken";

        CaseDocument caseDocument1 =
            CaseDocument.builder().documentName("directionsQuestionnaire1").documentLink(Document.builder().documentUrl("documentUrl1").build())
                .build();
        CaseDocument caseDocument2 =
            CaseDocument.builder().documentName("directionsQuestionnaire2").documentLink(Document.builder().documentUrl("documentUrl1").build())
                .build();

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
