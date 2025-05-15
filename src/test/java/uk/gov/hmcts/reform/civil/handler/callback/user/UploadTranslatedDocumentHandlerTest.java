package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentDefaultStrategy;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategyFactory;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentV1Strategy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentHandlerTest extends BaseCallbackHandlerTest {

    private UploadTranslatedDocumentHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private UploadTranslatedDocumentV1Strategy uploadTranslatedDocumentV1Strategy;

    private static final String FILE_NAME_1 = "Some file 1";

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        SystemGeneratedDocumentService systemGeneratedDocumentService = new SystemGeneratedDocumentService();
        UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy = new UploadTranslatedDocumentDefaultStrategy(
            systemGeneratedDocumentService,
            objectMapper,
            assignCategoryId,
            featureToggleService
        );
        UploadTranslatedDocumentStrategyFactory uploadTranslatedDocumentStrategyFactory = new UploadTranslatedDocumentStrategyFactory(
            uploadTranslatedDocumentDefaultStrategy,
            uploadTranslatedDocumentV1Strategy
        );
        handler = new UploadTranslatedDocumentHandler(uploadTranslatedDocumentStrategyFactory);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUploadTranslatedDocumentSuccessfully() {
            //Given
            TranslatedDocument translatedDocument1 = TranslatedDocument
                .builder()
                .documentType(DEFENDANT_RESPONSE)
                .file(Document.builder().documentFileName(FILE_NAME_1).build())
                .build();
            List<Element<TranslatedDocument>> translatedDocument = List.of(
                element(translatedDocument1)
            );

            CaseData caseData = CaseDataBuilder
                .builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .caseDataLiP(CaseDataLiP
                                 .builder()
                                 .translatedDocuments(translatedDocument)
                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).doesNotHaveToString("translatedDocument");
        }
    }

}
