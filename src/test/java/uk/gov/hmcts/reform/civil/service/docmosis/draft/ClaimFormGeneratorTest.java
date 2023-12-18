package uk.gov.hmcts.reform.civil.service.docmosis.draft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.ClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.ClaimFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_FORM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DRAFT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERATE_LIP_CLAIMANT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERATE_LIP_DEFENDANT_CLAIM_FORM;

@ExtendWith(MockitoExtension.class)
class ClaimFormGeneratorTest {

    private static final String AUTHORISATION = "authorisation";
    @Mock
    private ClaimFormMapper claimFormMapper;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @InjectMocks
    private ClaimFormGenerator generator;

    @Test
    void shouldGenerateDraftClaimForm() {
        //Given
        CaseData caseData = CaseData.builder().build();
        ClaimForm claimForm = ClaimForm.builder().build();
        given(claimFormMapper.toClaimForm(any())).willReturn(claimForm);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(documentGeneratorService.generateDocmosisDocument(any(ClaimForm.class), any())).willReturn(
            docmosisDocument);

        //When
        generator.generate(caseData, AUTHORISATION, GENERATE_DRAFT_FORM);

        //Then
        verify(documentGeneratorService).generateDocmosisDocument(claimForm, DRAFT_CLAIM_FORM);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.DRAFT_CLAIM_FORM);
    }

    @Test
    void shouldGenerateClaimantClaimForm() {
        //Given
        CaseData caseData = CaseData.builder().build();
        ClaimForm claimForm = ClaimForm.builder().build();
        given(claimFormMapper.toClaimForm(any())).willReturn(claimForm);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(documentGeneratorService.generateDocmosisDocument(any(ClaimForm.class), any())).willReturn(
            docmosisDocument);

        //When
        generator.generate(caseData, AUTHORISATION, CaseEvent.GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC);

        //Then
        verify(documentGeneratorService).generateDocmosisDocument(claimForm, GENERATE_LIP_CLAIMANT_CLAIM_FORM);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.CLAIMANT_CLAIM_FORM);
    }


    @Test
    void shouldGenerateDefendantClaimForm() {
        //Given
        CaseData caseData = CaseData.builder().build();
        ClaimForm claimForm = ClaimForm.builder().build();
        given(claimFormMapper.toClaimForm(any())).willReturn(claimForm);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(documentGeneratorService.generateDocmosisDocument(any(ClaimForm.class), any())).willReturn(
            docmosisDocument);

        //When
        generator.generate(caseData, AUTHORISATION, GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC);

        //Then
        verify(documentGeneratorService).generateDocmosisDocument(claimForm, GENERATE_LIP_DEFENDANT_CLAIM_FORM);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.SEALED_CLAIM);
    }

}
