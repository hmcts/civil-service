package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

public class UploadTranslatedDocumentServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

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
            .file(Document.builder().documentFileName("test.pdf")
                      .documentUrl("http://test")
                      .documentBinaryUrl("http://test/12345")
                      .uploadTimestamp("01-01-2025").build())
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .build();
        // When
        CaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result.getGeneralOrderDocumentGA()).isNotNull();
        verify(assignCategoryId, times(1)).assignCategoryIdToCollection(
            anyList(),
            any(),
            eq(AssignCategoryId.APPLICATIONS)
        );
    }

    @Test
    void shouldNotProcessWhenNoTranslatedDocumentsPresent() {
        // Given
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(null) // No translated documents
            .build();

        // When
        CaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result).isEqualTo(caseData);
        verifyNoInteractions(assignCategoryId);
    }

    @Test
    void updateGaDraftDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.GENERAL_APPLICATION_DRAFT)
            .documentLink(Document.builder().documentFileName("Draft_application_2025-06-30 11:02:39.pdf")
                              .categoryID("applications").build())
            .documentName("Draft_application_2025-06-30 11:02:39.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        when(deadlinesCalculator.calculateApplicantResponseDeadline(
            any(LocalDateTime.class), any(Integer.class))).thenReturn(deadline);
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getGaDraftDocumentGA()).isNotNull();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaWrittenRepresentationConcurrentDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.WRITTEN_REPRESENTATION_CONCURRENT)
            .documentLink(Document.builder().documentFileName("written_reps_request.pdf")
                              .categoryID("applications").build())
            .documentName("written_reps_request.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getWrittenRepConcurrentDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.HEARING_NOTICE)
            .documentLink(Document.builder().documentFileName("hearing_notice.pdf")
                              .categoryID("applications").build())
            .documentName("hearing_notice.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_NOTICE_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getHearingNoticeDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.DIRECTION_ORDER)
            .documentLink(Document.builder().documentFileName("direction_order.pdf")
                              .categoryID("applications").build())
            .documentName("direction_order.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments((preTranslationGaDocuments))
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getDirectionOrderDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
            .documentLink(Document.builder().documentFileName("written_reps_request.pdf")
                              .categoryID("applications").build())
            .documentName("written_reps_request.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getWrittenRepSequentialDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.REQUEST_FOR_INFORMATION)
            .documentLink(Document.builder().documentFileName("request_mor_info.pdf")
                              .categoryID("applications").build())
            .documentName("request_mor_info.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getRequestForInformationDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.HEARING_ORDER)
            .documentLink(Document.builder().documentFileName("hearing_order.pdf")
                              .categoryID("applications").build())
            .documentName("hearing_order.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());
        // Then
        assertThat(caseData.getHearingOrderDocumentGA().isEmpty()).isFalse();
        assertThat(caseData.getPreTranslationGaDocuments().isEmpty()).isTrue();
    }

    @Test
    void updateGaGeneralOrderDocumentsWithTheOriginalDocuments() {
        // Given
        List<Element<TranslatedDocument>> translatedDocuments = new ArrayList<>();
        TranslatedDocument translatedDocument = TranslatedDocument.builder()
            .documentType(TranslatedDocumentType.GENERAL_ORDER)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.GENERAL_ORDER)
            .documentLink(Document.builder().documentFileName("general_order.pdf")
                              .categoryID("applications").build())
            .documentName("general_order.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.GENERAL_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getGeneralOrderDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.DISMISSAL_ORDER)
            .documentLink(Document.builder().documentFileName("dismissal_order.pdf")
                              .categoryID("applications").build())
            .documentName("dismissal_order.pdf")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.DISMISSAL_ORDER_DOC)
            .build();
        //when
        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseData.toBuilder());

        // Then
        assertThat(caseData.getDismissalOrderDocumentGA().isEmpty()).isFalse();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentLink(Document.builder().documentFileName("written_rep_response.pdf")
                              .categoryID("applications").build())
            .documentName("Written representation")
            .createdBy("Applicant")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPS_RESPONSE_DOC)
            .build();
        //when
        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentLink(Document.builder().documentFileName("written_rep_response.pdf")
                              .categoryID("applications").build())
            .documentName("Written representation")
            .createdBy("Respondent One")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.WRITTEN_REPS_RESPONSE_DOC)
            .build();
        //when
        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentLink(Document.builder().documentFileName("more_info_response.pdf")
                              .categoryID("applications").build())
            .documentName("Additional information")
            .createdBy("Applicant")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.MORE_INFO_RESPONSE_DOC)
            .build();
        //when
        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();
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
            .documentType(TranslatedDocumentType.REQUEST_MORE_INFORMATION_RESPONDENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());

        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentLink(Document.builder().documentFileName("more_info_response.pdf")
                              .categoryID("applications").build())
            .documentName("Additional information")
            .createdBy("Respondent One")
            .build();

        List<Element<CaseDocument>> preTranslationGaDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocuments(preTranslationGaDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.MORE_INFO_RESPONSE_DOC)
            .build();
        //when
        CaseData.CaseDataBuilder<?,?> builder = caseData.toBuilder();
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
            .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
            .file(mock(Document.class))
            .build();
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().code("F1234").build()).build())
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().code("FREE").build()).build())
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .preTranslationGaDocumentType(PreTranslationGaDocumentType.FINAL_ORDER_DOC)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.GENERAL_ORDER)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());

        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.HEARING_ORDER)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());

        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.HEARING_NOTICE)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());

        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.DISMISSAL_ORDER)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(
            TranslatedDocument.builder()
                .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)
                .file(Document.builder().documentFileName("test.pdf")
                          .documentUrl("http://test")
                          .documentBinaryUrl("http://test/12345")
                          .uploadTimestamp("01-01-2025").build())
                .build()).build());

        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments)
            .build();

        // When
        CaseData result = uploadTranslatedDocumentService.processTranslatedDocument(caseData, translator).build();

        // Then
        assertThat(result.getGeneralOrderDocumentGA()).isNotNull();
        assertThat(result.getHearingOrderDocumentGA()).isNotNull();
        assertThat(result.getHearingNoticeDocumentGA()).isNotNull();
        assertThat(result.getDirectionOrderDocumentGA()).isNotNull();
        assertThat(result.getWrittenRepSequentialDocumentGA()).isNotNull();
        assertThat(result.getWrittenRepConcurrentDocumentGA()).isNotNull();
        assertThat(result.getDismissalOrderDocumentGA()).isNotNull();
        assertThat(result.getGaDraftDocumentGA()).isNotNull();
        assertThat(result.getGeneralOrderDocumentGA().get(0).getValue().getCreatedBy()).isEqualTo(translator);
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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments).build();
        CaseData updatedCaseData = CaseData.builder().build();

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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments).build();
        CaseData updatedCaseData = CaseData.builder().build();

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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments).build();
        CaseData updatedCaseData = CaseData.builder().build();

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
        translatedDocuments.add(Element.<TranslatedDocument>builder().value(translatedDocument).build());
        CaseData caseData = CaseData.builder()
            .translatedDocumentsGA(translatedDocuments).build();
        CaseData updatedCaseData = CaseData.builder().build();

        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, "auth");

        verify(docUploadDashboardNotificationService).createDashboardNotification(eq(caseData), eq("Respondent One"), eq("auth"), eq(false));
        verify(docUploadDashboardNotificationService, never()).createResponseDashboardNotification(eq(caseData), eq("APPLICANT"), eq("auth"));
    }
}
