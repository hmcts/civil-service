package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class SystemGeneratedDocumentServiceTest {

    private static final String FILE_NAME_1 = "Some file 1";

    private final SystemGeneratedDocumentService systemGeneratedDocumentService = new SystemGeneratedDocumentService();

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments() {
        //Given
        Document document = new Document();
        document.setDocumentFileName(FILE_NAME_1);

        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(DEFENDANT_RESPONSE);
        translatedDocument1.setFile(document);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getSystemGeneratedDocumentsWithAddedDocument(translatedDocument, callbackParams.getCaseData());

        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }

    @Test
    void shouldAddCaseDocumentToSystemGeneratedDocuments() {
        //Given
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName(FILE_NAME_1);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getSystemGeneratedDocumentsWithAddedDocument(
                caseDocument,
                caseData
            );
        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }

    @Test
    void shouldGetHearingDocumentsDocumentToSystemGeneratedDocuments() {
        //Given
        Document document = new Document();
        document.setDocumentFileName(FILE_NAME_1);

        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(HEARING_NOTICE);
        translatedDocument1.setFile(document);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getHearingDocumentsWithAddedDocumentWelsh(translatedDocument, callbackParams.getCaseData());

        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }

    @Test
    void shouldGetAddTranslatedCourtOfficersOrderToCollection() {
        //Given
        Document document = new Document();
        document.setDocumentFileName(FILE_NAME_1);

        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(COURT_OFFICER_ORDER);
        translatedDocument1.setFile(document);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCourtOfficersOrders(new ArrayList<>());
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        List<Element<CaseDocument>> result = systemGeneratedDocumentService
            .getCourtOfficerOrdersWithAddedDocument(translatedDocument, callbackParams.getCaseData());

        //Then
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo(FILE_NAME_1);
    }

}
