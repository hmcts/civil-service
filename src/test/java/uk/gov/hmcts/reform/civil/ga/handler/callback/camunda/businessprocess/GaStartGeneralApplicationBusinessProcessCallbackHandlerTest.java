package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GaStartGeneralApplicationBusinessProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private GaStartGeneralApplicationBusinessProcessCallbackHandler startGeneralApplicationBusinessProcessCallbackHandler;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperBuilder.instance();

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, names = {"READY", "DISPATCHED"})
    void shouldSetStatusStarted_whenInitialStateIs(BusinessProcessStatus status) {
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(status)).build();

        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

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
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(new BusinessProcess().setStatus(status)).build();

        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        CallbackParams params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse)
            startGeneralApplicationBusinessProcessCallbackHandler.handle(params);

        assertThat(response.getErrors()).contains("Concurrency Error");
    }
}
