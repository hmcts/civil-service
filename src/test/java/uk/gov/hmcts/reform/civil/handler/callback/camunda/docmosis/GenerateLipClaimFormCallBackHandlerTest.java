package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimform.ClaimFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DRAFT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;

@ExtendWith(MockitoExtension.class)
class GenerateLipClaimFormCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ClaimFormGenerator claimFormGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    private ObjectMapper mapper;
    @Mock
    private AssignCategoryId assignCategoryId;
    @InjectMocks
    private GenerateLipClaimFormCallBackHandler handler;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateLipClaimFormCallBackHandler(mapper, claimFormGenerator,
                                                          assignCategoryId, systemGeneratedDocumentService, featureToggleService);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerateDraftClaimForm() {
        //Given
        given(claimFormGenerator.generate(
            any(CaseData.class),
            anyString(),
            eq(CaseEvent.GENERATE_DRAFT_FORM)
        )).willReturn(generateForm(DRAFT_CLAIM_FORM));
        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = buildCallbackParams(caseData, "GENERATE_DRAFT_FORM");

        // When
        handler.handle(callbackParams);

        // Then
        verify(claimFormGenerator).generate(caseData, BEARER_TOKEN, CaseEvent.GENERATE_DRAFT_FORM);

    }

    @Test
    void shouldGenerateClaimantClaimForm() {
        //Given
        given(claimFormGenerator.generate(
            any(CaseData.class),
            anyString(),
            eq(CaseEvent.GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC)
        )).willReturn(generateForm(DocumentType.CLAIMANT_CLAIM_FORM));
        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = buildCallbackParams(caseData, "GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC");

        // When
        handler.handle(callbackParams);

        // Then
        verify(claimFormGenerator).generate(caseData, BEARER_TOKEN, CaseEvent.GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.CLAIMANT1_DETAILS_OF_CLAIM.getValue()));

    }

    @Test
    void shouldGenerateDefendantClaimForm() {
        //Given
        given(claimFormGenerator.generate(
            any(CaseData.class),
            anyString(),
            eq(GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC)
        )).willReturn(generateForm(SEALED_CLAIM));

        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = buildCallbackParams(caseData, "GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC");

        // When
        handler.handle(callbackParams);

        //Then
        verify(claimFormGenerator).generate(caseData, BEARER_TOKEN, CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.CLAIMANT1_DETAILS_OF_CLAIM.getValue()));

    }

    @Test
    void shouldAddDefendantClaimFormInTempCollection_whenWelshFlagIsOn() {
        //Given
        given(claimFormGenerator.generate(
            any(CaseData.class),
            anyString(),
            eq(GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC)
        )).willReturn(generateForm(SEALED_CLAIM));

        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("WELSH").build();
        // When
        CallbackParams params = callbackParamsOf(caseData, GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(claimFormGenerator).generate(caseData, BEARER_TOKEN, CaseEvent.GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.CLAIMANT1_DETAILS_OF_CLAIM.getValue()));

    }

    private CaseDocument generateForm(DocumentType documentType) {
        return CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(documentType)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
    }

    private CallbackParams buildCallbackParams(CaseData caseData, String eventId) {

        return CallbackParams.builder()
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(eventId)
                         .build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, "BEARER_TOKEN"))
            .type(ABOUT_TO_SUBMIT)
            .build();
    }
}
