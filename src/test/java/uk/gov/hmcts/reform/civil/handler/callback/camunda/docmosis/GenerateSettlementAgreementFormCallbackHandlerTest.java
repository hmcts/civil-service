package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement.SettlementAgreementFormGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLEMENT_AGREEMENT;

@ExtendWith(MockitoExtension.class)
class GenerateSettlementAgreementFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    private GenerateSettlementAgreementFormCallbackHandler handler;
    @Mock
    private SettlementAgreementFormGenerator formGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private FeatureToggleService featureToggleService;

    private ObjectMapper mapper;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final CaseDocument caseDocument;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document1 = new CaseDocument();
        document1.setCreatedBy("John");
        document1.setDocumentName("document name");
        document1.setDocumentSize(0L);
        document1.setDocumentType(SETTLEMENT_AGREEMENT);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        caseDocument = document1;
    }

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new GenerateSettlementAgreementFormCallbackHandler(
            formGenerator,
            systemGeneratedDocumentService,
            mapper,
            featureToggleService
        );

    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder()
                        .soleTrader().build()
                        .setType(Party.Type.INDIVIDUAL))
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
    void shouldNotHideSettlementAgreementDocWhenClaimantHasWelshPreferenceAndWelshToggleDisabled() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        given(formGenerator.generate(
            any(CaseData.class),
            anyString()
        )).willReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference("WELSH");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideSettlementAgreementDocWhenClaimantHasWelshPreference() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(formGenerator.generate(
            any(CaseData.class),
            anyString()
        )).willReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference("WELSH");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideSettlementAgreementDocWhenDefendantHasWelshPreference() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(formGenerator.generate(
            any(CaseData.class),
            anyString()
        )).willReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().build();
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM);
    }

}


