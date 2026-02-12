package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    GaStartGeneralApplicationBusinessProcessCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GaStartGeneralApplicationBusinessProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private GaStartGeneralApplicationBusinessProcessCallbackHandler startGeneralApplicationBusinessProcessCallbackHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldSetStatusStarted_whenInitialStateIs(BusinessProcessStatus status) {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(status)).build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)
            startGeneralApplicationBusinessProcessCallbackHandler.handle(params);

        GeneralApplicationCaseData data = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        BusinessProcess businessProcess = data.getBusinessProcess();
        assertThat(businessProcess.getStatus()).isEqualTo(BusinessProcessStatus.STARTED);
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"STARTED", "FINISHED"})
    void shouldReturnErrors_whenInitialStatusIs(BusinessProcessStatus status) {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(status)).build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)
            startGeneralApplicationBusinessProcessCallbackHandler.handle(params);

        assertThat(response.getErrors()).contains("Concurrency Error");
    }
}
