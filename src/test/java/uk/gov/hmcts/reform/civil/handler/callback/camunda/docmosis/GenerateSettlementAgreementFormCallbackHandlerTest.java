package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement.SettlementAgreementFormGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT;

@ExtendWith(MockitoExtension.class)
class GenerateSettlementAgreementFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private GenerateSettlementAgreementFormCallbackHandler handler;
    @Mock
    private SettlementAgreementFormGenerator formGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private ObjectMapper mapper;

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
    void shouldReturnCorrectActivityId_whenRequestedSettlementAgreementForm() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateSignSettlementAgreement");
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM);
    }

}


