package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class DeleteQueryDocumentHandlerTest extends BaseCallbackHandlerTest {

    private static String PROCESS_INSTANCE_ID = "instance-id";
    @InjectMocks
    private DeleteQueryDocumentHandler handler;
    @Mock
    private QueryManagementCamundaService camundaService;
    @Mock
    private SecuredDocumentManagementService documentManagementService;

    @BeforeEach
    void setUp() {
        handler = new DeleteQueryDocumentHandler(
            camundaService,
            documentManagementService
        );
    }

    @Test
    void shouldCallDeleteDocument_withExpectedDocumentId() {
        String documentId = UUID.randomUUID().toString();
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(
            QueryManagementVariables
                .builder()
                .documentToRemoveId(documentId)
                .build());

        CallbackParams params = callbackParamsOf(CaseData.builder()
                                                     .businessProcess(
                                                         BusinessProcess.builder()
                                                             .processInstanceId(PROCESS_INSTANCE_ID)
                                                             .build())
                                                     .build(), ABOUT_TO_SUBMIT);

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(actual).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
        verify(documentManagementService).deleteDocument(
            "BEARER_TOKEN",
            documentId
        );
    }

}

