package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDoc;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDocMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InterlocutoryJudgementDocGeneratorTest {

    private static final String AUTHORISATION = "authorisation";
    @Mock
    private InterlocutoryJudgementDocMapper mapper;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;

    private InterlocutoryJudgementDocGenerator generator;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @BeforeEach
    public void setup() {
        generator = new InterlocutoryJudgementDocGenerator(mapper, documentManagementService, documentGeneratorService);
    }

    @Test
    void shouldGenerateInterlocutoryJudgementDoc() {

        //Given
        CaseData caseData = CaseData.builder().build();
        InterlocutoryJudgementDoc interlocutoryJudgementDoc = InterlocutoryJudgementDoc.builder().build();
        given(mapper.toInterlocutoryJudgementDoc(any())).willReturn(interlocutoryJudgementDoc);
        DocmosisDocument docmosisDocument = DocmosisDocument.builder().build();

        given(documentGeneratorService.generateDocmosisDocument(
            any(InterlocutoryJudgementDoc.class),
            any()
        )).willReturn(
            docmosisDocument);

        //When
        generator.generateInterlocutoryJudgementDoc(caseData, AUTHORISATION);

        //Then
        verify(documentGeneratorService).generateDocmosisDocument(
            interlocutoryJudgementDoc,
            DocmosisTemplates.INTERLOCUTORY_JUDGEMENT_DOCUMENT
        );
        verify(documentManagementService).uploadDocument(
            eq(AUTHORISATION),
            uploadDocumentArgumentCaptor.capture()
        );

        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.INTERLOCUTORY_JUDGEMENT);
    }
}
