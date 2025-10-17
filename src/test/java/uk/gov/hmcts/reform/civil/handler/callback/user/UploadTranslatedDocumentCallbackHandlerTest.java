package uk.gov.hmcts.reform.civil.handler.callback.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UploadTranslatedDocumentService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@ExtendWith(MockitoExtension.class)
public class UploadTranslatedDocumentCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ObjectMapper objectMapper;
    @Mock
    private UploadTranslatedDocumentService uploadTranslatedDocumentService;
    @Mock
    IdamClient idamClient;
    @Mock
    private GaCaseDataEnricher gaCaseDataEnricher;
    @InjectMocks
    private UploadTranslatedDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new UploadTranslatedDocumentCallbackHandler(objectMapper, idamClient, uploadTranslatedDocumentService, gaCaseDataEnricher);
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid("uid").givenName("").familyName("translator").build());
        lenient().when(gaCaseDataEnricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallAboutToSubmit() {
            CaseData caseData = CaseData.builder()
                    .build();

            CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPLOAD_TRANSLATED_DOCUMENT,
                    CallbackType.ABOUT_TO_SUBMIT
            );
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
            doReturn(caseDataBuilder)
                    .when(uploadTranslatedDocumentService)
                    .processTranslatedDocument(eq(caseData), anyString());
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then
            objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getErrors()).isNull();
            verify(uploadTranslatedDocumentService).processTranslatedDocument(eq(caseData), any());
        }

    }
}
