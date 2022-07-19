package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GenerateSdoCallbackHandlerTest extends BaseCallbackHandlerTest {

    private GenerateSdoCallbackHandler callbackHandler;

    @Mock
    private SdoGeneratorService sdoGeneratorService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void prepare() {
        callbackHandler = new GenerateSdoCallbackHandler(sdoGeneratorService,
                                                         objectMapper);
    }

    @Test
    public void testSubmit() {
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(Collections.emptyList())
            .build();

        CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
        CaseDocument generatedDoc = Mockito.mock(CaseDocument.class);
        Mockito.when(sdoGeneratorService.generate(
            caseData, params.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString()
        )).thenReturn(generatedDoc);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) callbackHandler.handle(params);

        Assertions.assertEquals(1, ((List)response.getData().get("systemGeneratedCaseDocuments")).size());
    }
}
