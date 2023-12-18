package uk.gov.hmcts.reform.civil.service.docmosis.draft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.DraftClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.DraftClaimFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DRAFT_CLAIM_FORM;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DraftClaimFormGenerator.class,
    JacksonAutoConfiguration.class,
})
class DraftClaimFormGeneratorTest {

    private static final String AUTHORISATION = "authorisation";
    @MockBean
    private DraftClaimFormMapper draftClaimFormMapper;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private DraftClaimFormGenerator generator;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @Test
    void shouldGenerateDraftClaimForm() {
        //Given
        CaseData caseData = CaseData.builder().build();
        DraftClaimForm draftClaimForm = DraftClaimForm.builder().build();
        given(draftClaimFormMapper.toDraftClaimForm(any())).willReturn(draftClaimForm);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();
        given(documentGeneratorService.generateDocmosisDocument(any(DraftClaimForm.class), any())).willReturn(
            docmosisDocument);
        //When
        generator.generate(caseData, AUTHORISATION);
        //Then
        verify(documentGeneratorService).generateDocmosisDocument(draftClaimForm, DRAFT_CLAIM_FORM);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.DRAFT_CLAIM_FORM);
    }

}
