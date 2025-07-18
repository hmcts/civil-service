package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrderCaseDataMapper;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrderValidator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class GenerateSdoOrderTest {

    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private SdoGeneratorService sdoGeneratorService;

    @Mock
    private List<GenerateSdoOrderValidator> generateSdoOrderValidators;

    @Mock
    private List<GenerateSdoOrderCaseDataMapper> generateSdoOrderCaseDataMappers;

    private GenerateSdoOrder generateSdoOrder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        generateSdoOrder = new GenerateSdoOrder(assignCategoryId,
                objectMapper, sdoGeneratorService, generateSdoOrderValidators,
                generateSdoOrderCaseDataMappers);
    }

    @Test
    void shouldNotReturnErrorsWhenCaseDataIsEmpty() {
        CaseData caseData = CaseData.builder().build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .version(CallbackVersion.V_1)
                .build();

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) generateSdoOrder.execute(callbackParams);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldNotReturnErrorsWhenSmallClaimsWitnessInputsArePositive() {
        CaseData caseData = CaseData.builder()
                .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                        .input2("1")
                        .input3("1")
                        .build())
                .build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) generateSdoOrder.execute(callbackParams);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldNotReturnErrorsWhenPpiDateIsInFuture() {
        CaseData caseData = CaseData.builder()
                .sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder()
                        .ppiDate(LocalDate.now().plusDays(1))
                        .build())
                .build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) generateSdoOrder.execute(callbackParams);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldGenerateAndSaveSdoOrderDocument() {
        CaseData caseData = CaseData.builder().build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();

        CaseDocument order = CaseDocument.builder().documentLink(
                        Document.builder().documentUrl("url").build())
                .build();

        when(sdoGeneratorService.generate(any(), any())).thenReturn(order);

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) generateSdoOrder.execute(callbackParams);

        assertNotNull(response.getData().get("sdoOrderDocument"));
    }

    @Test
    void shouldNotReturnErrorsWhenFastTrackWitnessInputsArePositive() {
        CaseData caseData = CaseData.builder()
                .fastTrackWitnessOfFact(FastTrackWitnessOfFact.builder()
                        .input2("3")
                        .input3("3")
                        .build())
                .build();

        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) generateSdoOrder.execute(callbackParams);

        assertEquals(0, response.getErrors().size());
    }
}