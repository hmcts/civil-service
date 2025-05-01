package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.manualdetermination.ClaimantLipManualDeterminationFormGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LIP_MANUAL_DETERMINATION;

@ExtendWith(MockitoExtension.class)
class GenerateClaimantLipManualDeterminationCallBackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private GenerateClaimantLipManualDeterminationCallBackHandler handler;

    @Mock
    private ClaimantLipManualDeterminationFormGenerator formGenerator;

    @Mock
    private SystemGeneratedDocumentService documentService;

    @Mock
    private ObjectMapper mapper;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(LIP_MANUAL_DETERMINATION)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);

        CaseData caseData = createCaseData(Party.Type.COMPANY);
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_whenPartyTypeIsNotCompanyOrOrganisation() {
        CaseData caseData = createCaseData(Party.Type.SOLE_TRADER);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION.name());

        handler.handle(params);

        verify(formGenerator, never()).generate(any(CaseData.class), anyString());
        verify(documentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequestedMDForm() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("Generate_LIP_Claimant_MD");
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION);
    }

    private CaseData createCaseData(Party.Type type) {
        return CaseDataBuilder.builder()
            .respondent1(PartyBuilder.builder()
                             .soleTrader().build().toBuilder()
                             .type(type)
                             .build())
            .build();
    }
}
