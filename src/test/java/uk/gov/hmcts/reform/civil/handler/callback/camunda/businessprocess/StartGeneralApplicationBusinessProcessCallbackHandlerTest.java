package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    StartGeneralApplicationBusinessProcessCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class StartGeneralApplicationBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private StartGeneralApplicationBusinessProcessCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private CaseData getTestCaseDataWithNullBusinessProcessGA(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();
        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    private CaseData getBusinessProcessReadyTestCaseData(CaseData caseData) {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        GeneralApplication application = builder
            .businessProcess(BusinessProcess.builder()
                                 .status(BusinessProcessStatus.READY)
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .build()).build();
        return caseData.toBuilder()
            .generalApplications(wrapElements(application))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();
    }

    private CaseData getBusinessProcessReadyForMultipleGA(CaseData caseData) {
        GeneralApplication application = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.READY).build()).build();

        GeneralApplication application2 = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.READY).build()).build();

        GeneralApplication application3 = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.READY).build()).build();

        return caseData.toBuilder()
            .generalApplications(wrapElements(application, application2, application3))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();

    }

    private CaseData getBusinessProcessForMultipleGA(CaseData caseData) {
        GeneralApplication application = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.READY).build()).build();

        GeneralApplication application2 = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.STARTED).build()).build();

        GeneralApplication application3 = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId(PROCESS_INSTANCE_ID)
                                 .camundaEvent("INITIATE_GENERAL_APPLICATION")
                                 .status(BusinessProcessStatus.FINISHED).build()).build();

        return caseData.toBuilder()
            .generalApplications(wrapElements(application, application2, application3))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .build())
            .build();

    }

    @Test
    void shouldReturnStartedStatusForSingleGABusinessProcess() {
        CaseData caseData = getBusinessProcessReadyTestCaseData(CaseDataBuilder.builder().build());
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

        List<Element<GeneralApplication>> generalApplications = data.getGeneralApplications();

        for (Element<GeneralApplication> generalApplication : generalApplications) {
            if (checkGAExitsWithBusinessProcessReady(generalApplication)) {
                assertThat(generalApplication.getValue().getBusinessProcess().getStatus()).isEqualTo(STARTED);
            }
        }
    }

    @Test
    void shouldThrowErrorWhenNoGAExits() {
        CaseData caseData = CaseDataBuilder.builder().build();
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(((List)(response.getData().get("generalApplications"))).size()).isEqualTo(0);
    }

    @Test
    void shouldReturnStartedStatusForMultipleGA() {
        CaseData caseData = getBusinessProcessReadyForMultipleGA(CaseDataBuilder.builder().build());
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

        List<Element<GeneralApplication>> generalApplications = data.getGeneralApplications();

        for (Element<GeneralApplication> generalApplication : generalApplications) {
            if (checkGAExitsWithBusinessProcessReady(generalApplication)) {
                assertThat(generalApplication.getValue().getBusinessProcess().getStatus()).isEqualTo(STARTED);
            }
        }
    }

    @Test
    void shouldNotThrowErrorForMultipleValidGA() {
        CaseData caseData = getBusinessProcessForMultipleGA(CaseDataBuilder.builder().build());
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldNotReturnErrors_whenGABusinessProcessIsEmpty() {
        CaseData caseData = getTestCaseDataWithNullBusinessProcessGA(CaseDataBuilder.builder().build());
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    public Boolean checkGAExitsWithBusinessProcessReady(Element<GeneralApplication> generalApplication) {
        return generalApplication.getValue() != null
            && generalApplication.getValue().getBusinessProcess() != null
            && StringUtils.isNotBlank(generalApplication.getValue().getBusinessProcess().getProcessInstanceId());
    }
}
