package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.UploadTranslatedDocumentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UploadTranslatedDocumentCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private ObjectMapper objectMapper;
    @Mock
    private UploadTranslatedDocumentService uploadTranslatedDocumentService;
    @Mock
    IdamClient idamClient;
    @InjectMocks
    private UploadTranslatedDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new UploadTranslatedDocumentCallbackHandler(objectMapper, idamClient, uploadTranslatedDocumentService);
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid("uid").givenName("").familyName("translator").build());
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallAboutToSubmit() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .build();

            CallbackParams params = callbackParamsOf(
                caseData,
                CaseEvent.UPLOAD_TRANSLATED_DOCUMENT,
                CallbackType.ABOUT_TO_SUBMIT
            );
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            when(uploadTranslatedDocumentService.processTranslatedDocument(eq(caseData), any())).thenReturn(caseDataBuilder);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(response.getErrors()).isNull();
            verify(uploadTranslatedDocumentService).processTranslatedDocument(eq(caseData), any());
        }

    }
}
