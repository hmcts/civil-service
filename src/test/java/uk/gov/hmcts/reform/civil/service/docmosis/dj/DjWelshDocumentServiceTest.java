package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT_WELSH;

@ExtendWith(MockitoExtension.class)
class DjWelshDocumentServiceTest {

    private static final String AUTHORISATION = "auth-token";

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private CivilStitchService civilStitchService;

    private DjWelshDocumentService djWelshDocumentService;

    @BeforeEach
    void setUp() {
        djWelshDocumentService = new DjWelshDocumentService(
            documentGeneratorService,
            documentManagementService,
            civilStitchService
        );
    }

    @Test
    void shouldReturnEnglishDocumentWhenWelshNotRequired() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .build();
        DefaultJudgmentForm defaultJudgmentForm = DefaultJudgmentForm.builder().build();
        CaseDocument englishDocument = CaseDocumentBuilder.builder().documentType(DEFAULT_JUDGMENT_CLAIMANT1).build();

        CaseDocument result = djWelshDocumentService.attachWelshDocumentIfRequired(
            defaultJudgmentForm,
            caseData,
            AUTHORISATION,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name(),
            DEFAULT_JUDGMENT_CLAIMANT1,
            englishDocument
        );

        assertThat(result).isSameAs(englishDocument);
        verifyNoInteractions(documentGeneratorService, documentManagementService, civilStitchService);
    }

    @Test
    void shouldStitchWelshDocumentWhenClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .ccdCaseReference(1234567890L)
            .legacyCaseReference("000LR001")
            .build();
        DefaultJudgmentForm defaultJudgmentForm = DefaultJudgmentForm.builder().build();
        CaseDocument englishDocument = CaseDocumentBuilder.builder()
            .documentName("english.pdf")
            .documentType(DEFAULT_JUDGMENT_CLAIMANT1)
            .build();
        CaseDocument welshDocument = CaseDocumentBuilder.builder()
            .documentName("welsh.pdf")
            .documentType(DEFAULT_JUDGMENT_CLAIMANT1)
            .build();
        CaseDocument stitchedDocument = CaseDocumentBuilder.builder()
            .documentName("stitched.pdf")
            .documentType(DEFAULT_JUDGMENT_CLAIMANT1)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(), eq(N121_SPEC_CLAIMANT_WELSH)))
            .thenReturn(new DocmosisDocument("welsh", new byte[]{1, 2}));
        when(documentManagementService.uploadDocument(eq(AUTHORISATION), any(PDF.class))).thenReturn(welshDocument);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), eq(welshDocument.getDocumentName()), anyLong(),
            eq(DEFAULT_JUDGMENT_CLAIMANT1), eq(AUTHORISATION))).thenReturn(stitchedDocument);

        CaseDocument result = djWelshDocumentService.attachWelshDocumentIfRequired(
            defaultJudgmentForm,
            caseData,
            AUTHORISATION,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name(),
            DEFAULT_JUDGMENT_CLAIMANT1,
            englishDocument
        );

        assertThat(result).isSameAs(stitchedDocument);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentMetaData>> captor = ArgumentCaptor.forClass(List.class);
        verify(civilStitchService).generateStitchedCaseDocument(
            captor.capture(),
            eq(welshDocument.getDocumentName()),
            eq(caseData.getCcdCaseReference()),
            eq(DEFAULT_JUDGMENT_CLAIMANT1),
            eq(AUTHORISATION)
        );

        List<DocumentMetaData> metaData = captor.getValue();
        assertThat(metaData).hasSize(2);
        assertThat(metaData.get(0).getDescription()).isEqualTo("English Document");
        assertThat(metaData.get(1).getDescription()).isEqualTo("Welsh Doc to attach");

        verify(documentGeneratorService).generateDocmosisDocument(defaultJudgmentForm, N121_SPEC_CLAIMANT_WELSH);
        verify(documentManagementService).uploadDocument(eq(AUTHORISATION), any(PDF.class));
    }
}
