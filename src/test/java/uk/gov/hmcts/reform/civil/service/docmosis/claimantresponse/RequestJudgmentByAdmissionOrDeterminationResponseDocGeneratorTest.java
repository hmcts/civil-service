package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDetermination;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDeterminationMapper;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_DETERMINATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_CLAIMANT_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_DEFENDANT_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_OR_DETERMINATION;

@ExtendWith(MockitoExtension.class)
class RequestJudgmentByAdmissionOrDeterminationResponseDocGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @Mock
    private JudgmentByAdmissionOrDeterminationMapper judgmentByAdmissionOrDeterminationMapper;

    @Mock
    private SecuredDocumentManagementService documentManagementService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator generator;

    @Test
    void shouldGenerateJudgementByAdmissionDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(CCJ_REQUEST_ADMISSION)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_OR_DETERMINATION)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData, GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        CaseDocument actual = generator.generate(GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, caseData, BEARER_TOKEN);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION));
        assertThat(actual).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateJudgementByDeterminationDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), REFERENCE_NUMBER, "determination");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(CCJ_REQUEST_DETERMINATION)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_OR_DETERMINATION)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_DETERMINATION))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData, GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        CaseDocument actual = generator.generate(GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC, caseData, BEARER_TOKEN);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_DETERMINATION));
        assertThat(actual).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateDefaultJudgementByAdmissionDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(CCJ_REQUEST_ADMISSION)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_OR_DETERMINATION)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData, GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        CaseDocument actual = generator.generate(GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, caseData, BEARER_TOKEN);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION));
        assertThat(actual).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateClaimantJudgementByAdmissionDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_CLAIMANT.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class),
                                                               eq(JUDGMENT_BY_ADMISSION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_CLAIMANT.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentDocs(caseData))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        List<CaseDocument> actual = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                       GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(caseDocument, "judgments");
        assertThat(actual).contains(caseDocument);
    }

    @Test
    void shouldGenerateDefendantJudgementByAdmissionDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_DEFENDANT.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_DEFENDANT)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_DEFENDANT.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentDocs(caseData))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        List<CaseDocument> actual = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                  GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT));
        assertThat(actual).contains(caseDocument);
    }

    @Test
    void shouldGenerateClaimantJudgementByAdmissionWelshDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_CLAIMANT.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT)
            .build();
        JudgmentByAdmissionOrDetermination builder = JudgmentByAdmissionOrDetermination.builder().build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class),
                                                               eq(JUDGMENT_BY_ADMISSION_CLAIMANT_BILINGUAL)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_CLAIMANT_BILINGUAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .claimantBilingualLanguagePreference("BOTH")
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentDocs(caseData))
            .thenReturn(builder);
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentWelshDocs(caseData, builder))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());
        // When
        List<CaseDocument> actual = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                       GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(caseDocument, "judgments");
        assertThat(actual).contains(caseDocument);
    }

    @Test
    void shouldGenerateDefendantJudgementByAdmissionWelshDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_DEFENDANT.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT)
            .build();
        JudgmentByAdmissionOrDetermination builder = JudgmentByAdmissionOrDetermination.builder().build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_DEFENDANT_BILINGUAL)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_DEFENDANT_BILINGUAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .claimantBilingualLanguagePreference("BOTH")
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentDocs(caseData))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());
        when(judgmentByAdmissionOrDeterminationMapper.toNonDivergentWelshDocs(caseData, builder))
            .thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        List<CaseDocument> actual = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                       GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT));
        assertThat(actual).contains(caseDocument);
    }
}
