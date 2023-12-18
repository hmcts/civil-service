package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement.SettlementAgreementFormGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateSettlementAgreementFormCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
public class GenerateSettlementAgreementFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateSettlementAgreementFormCallbackHandler handler;
    @MockBean
    private SettlementAgreementFormGenerator formGenerator;
    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final CaseDocument caseDocument = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(SETTLEMENT_AGREEMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                    .documentUrl("fake-url")
                    .documentFileName("file-name")
                    .documentBinaryUrl("binary-url")
                    .build())
            .build();

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder()
                        .soleTrader().build().toBuilder()
                        .type(Party.Type.INDIVIDUAL)
                        .build())
                .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_whenPartyTypeIsCompanyOROrganisation() {
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(caseDocument);

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder()
                        .soleTrader().build().toBuilder()
                        .type(Party.Type.ORGANISATION)
                        .build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM.name());

        handler.handle(params);
        verify(formGenerator, never()).generate(any(CaseData.class), anyString());
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequestedSettlementAgreementForm() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("Generate_Settlement_Agreement");
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM);
    }

}


