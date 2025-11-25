package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class StartGeneralApplicationBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @Mock
    private ExternalTask mockTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private FeatureToggleService featureToggleService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @InjectMocks
    private StartGeneralApplicationBusinessProcessCallbackHandler handler;

    private CaseData getTestCaseDataWithNullBusinessProcessGA(CaseData caseData) {
        GeneralApplication application = new GeneralApplication();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        application.setBusinessProcess(businessProcess);
        caseData.setGeneralApplications(wrapElements(application));
        GAHearingDetails hearingDetails = new GAHearingDetails();
        caseData.setGeneralAppHearingDetails(hearingDetails);
        return caseData;
    }

    private CaseData getBusinessProcessReadyTestCaseData(CaseData caseData) {
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setStatus(BusinessProcessStatus.READY);
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        GeneralApplication application = new GeneralApplication();
        application.setBusinessProcess(businessProcess);
        caseData.setGeneralApplications(wrapElements(application));
        GAHearingDetails hearingDetails = new GAHearingDetails();
        caseData.setGeneralAppHearingDetails(hearingDetails);
        return caseData;
    }

    private CaseData getBusinessProcessReadyForMultipleGA(CaseData caseData) {
        BusinessProcess businessProcess1 = new BusinessProcess();
        businessProcess1.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess1.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        businessProcess1.setStatus(BusinessProcessStatus.READY);

        GeneralApplication application = new GeneralApplication();
        application.setBusinessProcess(businessProcess1);

        BusinessProcess businessProcess2 = new BusinessProcess();
        businessProcess2.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess2.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        businessProcess2.setStatus(BusinessProcessStatus.READY);
        GeneralApplication application2 = new GeneralApplication();
        application2.setBusinessProcess(businessProcess2);

        BusinessProcess businessProcess3 = new BusinessProcess();
        businessProcess3.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess3.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        businessProcess3.setStatus(BusinessProcessStatus.READY);
        GeneralApplication application3 = new GeneralApplication();
        application3.setBusinessProcess(businessProcess3);

        caseData.setGeneralApplications(wrapElements(application, application2, application3));
        GAHearingDetails hearingDetails = new GAHearingDetails();
        caseData.setGeneralAppHearingDetails(hearingDetails);
        return caseData;

    }

    private CaseData getBusinessProcessForMultipleGA(CaseData caseData) {
        BusinessProcess businessProcess1 = new BusinessProcess();
        businessProcess1.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess1.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        GeneralApplication application = new GeneralApplication();
        businessProcess1.setStatus(BusinessProcessStatus.READY);
        application.setBusinessProcess(businessProcess1);

        BusinessProcess businessProcess2 = new BusinessProcess();
        businessProcess2.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess2.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        businessProcess2.setStatus(BusinessProcessStatus.STARTED);
        GeneralApplication application2 = new GeneralApplication();
        application2.setBusinessProcess(businessProcess2);

        BusinessProcess businessProcess3 = new BusinessProcess();
        businessProcess3.setProcessInstanceId(PROCESS_INSTANCE_ID);
        businessProcess3.setCamundaEvent("INITIATE_GENERAL_APPLICATION");
        businessProcess3.setStatus(BusinessProcessStatus.FINISHED);
        GeneralApplication application3 = new GeneralApplication();
        application3.setBusinessProcess(businessProcess3);

        caseData.setGeneralApplications(wrapElements(application, application2, application3));
        GAHearingDetails hearingDetails = new GAHearingDetails();
        caseData.setGeneralAppHearingDetails(hearingDetails);
        return caseData;

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
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {});
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        List<?> generalApplications = (List<?>) response.getData().get("generalApplications");
        assertThat(generalApplications).isEmpty();
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

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnErrors_whenGABusinessProcessIsEmpty() {
        CaseData caseData = getTestCaseDataWithNullBusinessProcessGA(CaseDataBuilder.builder().build());
        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertNull(response.getErrors());
    }

    public Boolean checkGAExitsWithBusinessProcessReady(Element<GeneralApplication> generalApplication) {
        return generalApplication.getValue() != null
            && generalApplication.getValue().getBusinessProcess() != null
            && StringUtils.isNotBlank(generalApplication.getValue().getBusinessProcess().getProcessInstanceId());
    }
}
