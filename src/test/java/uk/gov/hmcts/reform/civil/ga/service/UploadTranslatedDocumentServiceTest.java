package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_GA_LIP;

public class UploadTranslatedDocumentServiceTest {

    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private GaForLipService gaForLipService;

    @Mock
    private DocUploadDashboardNotificationService docUploadDashboardNotificationService;

    @InjectMocks
    private UploadTranslatedDocumentService uploadTranslatedDocumentService;

    @Mock
    DeadlinesCalculator deadlinesCalculator;
    LocalDateTime deadline = LocalDateTime.now().plusDays(5);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    String translator = "translator";

    @Test
    void shouldProcessTranslatedDocumentsAndUpdateCaseData() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(new Document()
                      .setDocumentFileName("test.pdf")
                      .setDocumentUrl("http://test")
                      .setDocumentBinaryUrl("http://test/12345")
                      .setUploadTimestamp("01-01-2025"))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .build();
        // When
        GeneralApplicationCaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result.getGeneralOrderDocument()).isNotNull();
        verify(assignCategoryId, times(1)).assignCategoryIdToCollection(
            anyList(),
            any(),
            eq(AssignCategoryId.APPLICATIONS)
        );
    }

    @Test
    void shouldNotProcessWhenNoTranslatedDocumentsPresent() {
        // Given
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(null) // No translated documents
            .build();

        // When
        GeneralApplicationCaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result).isEqualTo(caseData);
        verifyNoInteractions(assignCategoryId);
    }

    @Test
    void shouldAddUploadedApplicantDocumentToAdditionalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.UPLOADED_DOCUMENTS_APPLICANT)
            .file(new Document()
                      .setDocumentFileName("test.pdf")
                      .setDocumentUrl("http://test")
                      .setDocumentBinaryUrl("http://test/12345")
                      .setUploadTimestamp("01-01-2025"))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .build();

        // When
        GeneralApplicationCaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result.getGaAddlDoc()).isNotNull();
        assertThat(result.getGaAddlDocClaimant()).isNotNull();
        assertThat(result.getGaAddlDocStaff()).isNotNull();
        assertThat(result.getGaAddlDoc()).hasSize(1);
        assertThat(result.getGaAddlDocClaimant()).hasSize(1);
        assertThat(result.getGaAddlDocStaff()).hasSize(1);
        verify(assignCategoryId, times(1)).assignCategoryIdToCollection(anyList(), any(), eq(AssignCategoryId.APPLICATIONS));
    }

    @Test
    void updateGaDraftDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_APPLICATION_DRAFT)
            .setDocumentLink(new Document().setDocumentFileName("Draft_application_2025-06-30 11:02:39.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("Draft_application_2025-06-30 11:02:39.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
            any(LocalDateTime.class), any(Integer.class))).thenReturn(deadline);
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getGaDraftDocument()).isNotNull();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void shouldNotUpdateDeadlineForApplicationSummaryWhenOriginalDraftIsMissing() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(null)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC)
            .build();

        // When
        GeneralApplicationCaseData builder = caseData.copy();
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        // Then
        caseData = builder.build();
        assertThat(caseData.getGeneralAppNotificationDeadlineDate()).isNull();
        verify(deadlinesCalculator, never()).calculateApplicantResponseDeadline(any(LocalDateTime.class), any(Integer.class));
    }

    @Test
    void updateGaWrittenRepresentationConcurrentDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .setDocumentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)
            .file(mock(Document.class));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.WRITTEN_REPRESENTATION_CONCURRENT)
            .setDocumentLink(new Document().setDocumentFileName("written_reps_request.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("written_reps_request.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getWrittenRepConcurrentDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaHearingNoticeDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.HEARING_NOTICE)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.HEARING_NOTICE)
            .setDocumentLink(new Document().setDocumentFileName("hearing_notice.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("hearing_notice.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_NOTICE_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getHearingNoticeDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaDirectionOrderDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.DIRECTION_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("direction_order.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("direction_order.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getDirectionOrderDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaWrittenRepresentationSequentialDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
            .setDocumentLink(new Document().setDocumentFileName("written_reps_request.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("written_reps_request.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getWrittenRepSequentialDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaRequestMorInformationDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.REQUEST_FOR_INFORMATION)
            .setDocumentLink(new Document().setDocumentFileName("request_mor_info.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("request_mor_info.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getRequestForInformationDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void copyHearingOrderDocumentFromTempToOriginalCollection() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.HEARING_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.HEARING_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("hearing_order.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("hearing_order.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());
        // Then
        assertThat(caseData.getHearingOrderDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(GaFinalOrderSelection.class)
    void shouldPrepareOriginalAndTranslatedFinalOrderForBulkPrint(GaFinalOrderSelection finalOrderSelection) {
        Document originalFile = new Document().setDocumentFileName("final-order.pdf");
        Document translatedFile = new Document().setDocumentFileName("translated-final-order.pdf");
        CaseDocument originalOrder = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_ORDER)
            .setDocumentLink(originalFile)
            .setDocumentName("final-order.pdf");
        TranslatedDocument translation = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER)
            .file(translatedFile)
            .build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .finalOrderSelection(finalOrderSelection)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.FINAL_ORDER_DOC)
            .preTranslationGaDocuments(new ArrayList<>(List.of(element(originalOrder))))
            .translatedDocuments(new ArrayList<>(List.of(element(translation))))
            .build();
        GeneralApplicationCaseData builder = caseData.copy();

        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);
        GeneralApplicationCaseData updated = uploadTranslatedDocumentService
            .processTranslatedDocument(builder.build(), "CTSC translator").build();

        assertThat(updated.getOriginalDocumentsBulkPrint()).hasSize(1);
        assertThat(updated.getOriginalDocumentsBulkPrint().getFirst().getValue()).isEqualTo(originalOrder);
        assertThat(updated.getTranslatedDocumentsBulkPrint()).hasSize(1);
        assertThat(updated.getTranslatedDocumentsBulkPrint().getFirst().getValue()).isEqualTo(translation);
        assertThat(updated.getPreTranslationGaDocuments()).isEmpty();
        assertThat(updated.getTranslatedDocuments()).isNull();
        assertThat(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData))
            .isEqualTo(UPLOAD_TRANSLATED_DOCUMENT_GA_LIP);
    }

    @ParameterizedTest
    @EnumSource(GaFinalOrderSelection.class)
    void shouldPreparePublishedFinalOrderForBulkPrintWhenNoPreTranslationCollectionExists(
        GaFinalOrderSelection finalOrderSelection) {
        CaseDocument previousOrder = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("previous-order.pdf"));
        CaseDocument originalOrder = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("final-order.pdf"));
        CaseDocument previousTranslation = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_ORDER)
            .setCreatedBy("Previous translator")
            .setDocumentLink(new Document().setDocumentFileName("previous-welsh-order.pdf"));
        TranslatedDocument translation = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER)
            .file(new Document().setDocumentFileName("translated-final-order.pdf"))
            .build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .finalOrderSelection(finalOrderSelection)
            .generalOrderDocument(new ArrayList<>(List.of(element(previousOrder), element(originalOrder), element(previousTranslation))))
            .translatedDocuments(new ArrayList<>(List.of(element(translation))))
            .build();
        GeneralApplicationCaseData builder = caseData.copy();

        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);
        GeneralApplicationCaseData updated = uploadTranslatedDocumentService
            .processTranslatedDocument(builder.build(), "CTSC translator").build();

        assertThat(updated.getOriginalDocumentsBulkPrint()).hasSize(1);
        assertThat(updated.getOriginalDocumentsBulkPrint().getFirst().getValue()).isEqualTo(originalOrder);
        assertThat(updated.getTranslatedDocumentsBulkPrint()).hasSize(1);
        assertThat(updated.getTranslatedDocumentsBulkPrint().getFirst().getValue()).isEqualTo(translation);
        assertThat(updated.getGeneralOrderDocument()).hasSize(4);
        assertThat(updated.getGeneralOrderDocument().get(0).getValue()).isEqualTo(previousOrder);
        assertThat(updated.getGeneralOrderDocument().get(1).getValue()).isEqualTo(originalOrder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "empty", "translationsOnly"})
    void shouldRejectApproveOrEditTranslationWithoutAnEnglishOriginal(String collectionState) {
        GeneralApplicationCaseData builder = publishedFinalOrderCaseData(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER);
        if (collectionState.equals("missing")) {
            builder.generalOrderDocument(null);
        } else if (collectionState.equals("empty")) {
            builder.generalOrderDocument(new ArrayList<>());
        } else {
            builder.generalOrderDocument(new ArrayList<>(List.of(element(new CaseDocument()
                .setDocumentType(DocumentType.GENERAL_ORDER).setCreatedBy("A different translator")))));
        }

        assertThatThrownBy(() -> uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No original final order available for translated approve-or-edit order bulk print");
    }

    @Test
    void shouldNotReuseFinalOrderForPlainGeneralOrderTranslation() {
        GeneralApplicationCaseData builder = publishedFinalOrderCaseData(TranslatedDocumentType.GENERAL_ORDER);

        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        assertThat(builder.getOriginalDocumentsBulkPrint()).isNull();
        assertThat(builder.getGeneralOrderDocument()).hasSize(1);
    }

    @Test
    void shouldNotDuplicatePublishedOriginalForMultipleApproveOrEditTranslations() {
        GeneralApplicationCaseData builder = publishedFinalOrderCaseData(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER);
        final CaseDocument original = builder.getGeneralOrderDocument().getFirst().getValue();
        builder.getTranslatedDocuments().add(element(TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER)
            .file(new Document().setDocumentFileName("second-translation.pdf"))
            .build()));

        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        assertThat(builder.getOriginalDocumentsBulkPrint()).hasSize(1);
        assertThat(builder.getOriginalDocumentsBulkPrint().getFirst().getValue()).isEqualTo(original);
    }

    private GeneralApplicationCaseData publishedFinalOrderCaseData(TranslatedDocumentType type) {
        return new GeneralApplicationCaseData()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalOrderDocument(new ArrayList<>(List.of(element(new CaseDocument()
                .setDocumentType(DocumentType.GENERAL_ORDER)
                .setDocumentLink(new Document().setDocumentFileName("english-order.pdf"))))))
            .translatedDocuments(new ArrayList<>(List.of(element(TranslatedDocument.builder()
                .documentType(type)
                .file(new Document().setDocumentFileName("welsh-order.pdf"))
                .build()))))
            .build();
    }

    @Test
    void updateGaGeneralOrderDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.GENERAL_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("general_order.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("general_order.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.GENERAL_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getGeneralOrderDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaDismissalOrderDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.DISMISSAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentType(DocumentType.DISMISSAL_ORDER)
            .setDocumentLink(new Document().setDocumentFileName("dismissal_order.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("dismissal_order.pdf");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DISMISSAL_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.copy());

        // Then
        assertThat(caseData.getDismissalOrderDocument().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaWrittenRepsApplicantDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentLink(new Document().setDocumentFileName("written_rep_response.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("Written representation")
            .setCreatedBy("Applicant");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPS_RESPONSE_DOC)
            .build();
        //when
        GeneralApplicationCaseData builder = caseData.copy();
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        // Then
        caseData = builder.build();
        assertThat(caseData.getGaAddlDoc().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaWrittenRepsRespondentDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentLink(new Document().setDocumentFileName("written_rep_response.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("Written representation")
            .setCreatedBy("Respondent One");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPS_RESPONSE_DOC)
            .build();
        //when
        GeneralApplicationCaseData builder = caseData.copy();
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        // Then
        caseData = builder.build();
        assertThat(caseData.getGaAddlDoc().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaMoreInfoApplicantDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentLink(new Document().setDocumentFileName("more_info_response.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("Additional information")
            .setCreatedBy("Applicant");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.MORE_INFO_RESPONSE_DOC)
            .build();
        //when
        GeneralApplicationCaseData builder = caseData.copy();
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        // Then
        caseData = builder.build();
        assertThat(caseData.getGaAddlDoc().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaMoreInfoRespondentDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .setDocumentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT)
            .file(mock(Document.class));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        CaseDocument originalDocument = new CaseDocument()
            .setDocumentLink(new Document().setDocumentFileName("more_info_response.pdf")
                              .setCategoryID("applications"))
            .setDocumentName("Additional information")
            .setCreatedBy("Respondent One");

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.MORE_INFO_RESPONSE_DOC)
            .build();
        //when
        GeneralApplicationCaseData builder = caseData.copy();
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(builder);

        // Then
        caseData = builder.build();
        assertThat(caseData.getGaAddlDoc().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void shouldGetCorrectBusinessProcessForApplicationSummaryDraftDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .setDocumentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("F1234")))
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_DOC");
    }

    @Test
    void shouldGetCorrectBusinessProcessForFreeFeeApplicationSummaryDraftDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_FOR_FREE_FEE_APPLICATION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForApplicationSummaryResponseDraftDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT_RESPONDED)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.RESPOND_TO_APPLICATION_SUMMARY_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_RESPONSE_DOC");
    }

    @Test
    void shouldGetCorrectBusinessProcessForRequestWrittenRepsConcurrentDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForRequestWrittenRepsSequentialDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForHearingOrderDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.HEARING_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForRequestMoreInformationDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForFinalOrderDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.FINAL_ORDER_DOC)
            .finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_FINAL_ORDER");
    }

    @Test
    void shouldGetCorrectBusinessProcessForApproveEditOrder() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.GENERAL_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForDismissalOrderDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.DISMISSAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DISMISSAL_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldNotReturnBusinessProcessForApproveEditOrderWhenFinalDecision() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPROVE_OR_EDIT_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.GENERAL_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_GA_LIP");
    }

    @Test
    void shouldGetCorrectBusinessProcessForGeneralOrderDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.GENERAL_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForJudgeDirectionOrder() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForOtherDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.DISMISSAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForHearingNoticeDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.HEARING_NOTICE)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("UPLOAD_TRANSLATED_DOCUMENT_HEARING_SCHEDULED");
    }

    @Test
    void shouldGetCorrectBusinessProcessForWrittenRepsApplicantDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForWrittenRepsRespondentDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION");
    }

    @Test
    void shouldGetCorrectBusinessProcessForMoreInfoApplicantDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("RESPOND_TO_JUDGE_ADDITIONAL_INFO");
    }

    @Test
    void shouldGetCorrectBusinessProcessForMoreInfoRespondentDoc() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .preTranslationGaDocumentType(null)
            .build();
        // When
        String caseEvent = String.valueOf(uploadTranslatedDocumentService.getBusinessProcessEvent(caseData));
        assertThat(caseEvent).isEqualTo("RESPOND_TO_JUDGE_ADDITIONAL_INFO");
    }

    @Test
    void shouldHandleMultipleDocumentTypes() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();

        // Add documents with different types
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.GENERAL_ORDER)
                .file(new Document()
                          .setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));

        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.HEARING_ORDER)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));

        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.HEARING_NOTICE)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));

        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.DISMISSAL_ORDER)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
                .file(new Document().setDocumentFileName("test.pdf")
                          .setDocumentUrl("http://test")
                          .setDocumentBinaryUrl("http://test/12345")
                          .setUploadTimestamp("01-01-2025"))
                .build()));

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .build();

        // When
        GeneralApplicationCaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result.getGeneralOrderDocument()).isNotNull();
        assertThat(result.getHearingOrderDocument()).isNotNull();
        assertThat(result.getHearingNoticeDocument()).isNotNull();
        assertThat(result.getDirectionOrderDocument()).isNotNull();
        assertThat(result.getWrittenRepSequentialDocument()).isNotNull();
        assertThat(result.getWrittenRepConcurrentDocument()).isNotNull();
        assertThat(result.getDismissalOrderDocument()).isNotNull();
        assertThat(result.getGaDraftDocument()).isNotNull();
        assertThat(result.getGeneralOrderDocument().getFirst().getValue().getCreatedBy()).isEqualTo(translator);
        verify(assignCategoryId, times(9)).assignCategoryIdToCollection(anyList(), any(), any());
    }

    @Test
    void shouldSendUserUploadNotificationWrittenRepsApplicant() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();
        GeneralApplicationCaseData updatedCaseData = new GeneralApplicationCaseData().build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService).createDashboardNotification(eq(caseData), eq("Applicant"), eq("auth"), eq(false));
        verify(docUploadDashboardNotificationService).createResponseDashboardNotification(eq(caseData), eq("RESPONDENT"), eq("auth"));
    }

    @Test
    void shouldSendUserUploadNotificationWrittenRepsRespondent() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();
        GeneralApplicationCaseData updatedCaseData = new GeneralApplicationCaseData().build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService).createDashboardNotification(eq(caseData), eq("Respondent One"), eq("auth"), eq(false));
        verify(docUploadDashboardNotificationService).createResponseDashboardNotification(eq(caseData), eq("APPLICANT"), eq("auth"));
    }

    @Test
    void shouldSendUserUploadNotificationMoreInfoApplicant() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();
        GeneralApplicationCaseData updatedCaseData = new GeneralApplicationCaseData().build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService).createDashboardNotification(eq(caseData), eq("Applicant"), eq("auth"), eq(false));
        verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(eq(caseData), eq("RESPONDENT"), eq("auth"));
    }

    @Test
    void shouldSendUserUploadNotificationMoreInfoRespondent() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();
        GeneralApplicationCaseData updatedCaseData = new GeneralApplicationCaseData().build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService).createDashboardNotification(eq(caseData), eq("Respondent One"), eq("auth"), eq(false));
        verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(eq(caseData), eq("APPLICANT"), eq("auth"));
    }

    @Test
    void shouldNotSendUserUploadNotificationForUnsupportedTranslatedDocumentType() {
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, new GeneralApplicationCaseData().build(), "auth");

        verifyNoInteractions(docUploadDashboardNotificationService);
    }

    @Test
    void shouldNotSendUserUploadNotificationWhenCaseIsNotGaForLip() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments).build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, new GeneralApplicationCaseData().build(), "auth");

        verifyNoInteractions(docUploadDashboardNotificationService);
    }

    @Test
    void shouldReturnGaLipEventWhenNoTranslatedDocumentsPresent() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(null)
            .build();

        var caseEvent = uploadTranslatedDocumentService.getBusinessProcessEvent(caseData);

        assertThat(caseEvent).isEqualTo(UPLOAD_TRANSLATED_DOCUMENT_GA_LIP);
    }

    @Test
    void shouldNotSendUserUploadNotificationWhenDocumentIsStillAwaitingTranslation() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_APPLICANT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(new Element<TranslatedDocument>().setValue(translatedDocument));

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(
            element(new CaseDocument()
                        .setDocumentName("Written representation")
                        .setCreatedBy("Applicant"))
        );

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .translatedDocuments(translatedDocuments)
            .build();
        GeneralApplicationCaseData updatedCaseData = new GeneralApplicationCaseData()
            .preTranslationGaDocuments(preTranslationDocuments)
            .build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService, never()).createDashboardNotification(any(), any(), any(), any(Boolean.class));
        verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(any(), any(), any());
    }
}
