package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class EventEmitterServiceTest {

    @InjectMocks
    private EventEmitterService eventEmitterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private FeignException mockedFeignException;

    @Mock
    private CamundaRuntimeClient camundaRuntimeClient;

    private static final String TEST_EVENT = "TEST_EVENT";
    private static final String TEST_EVENT_QM = "queryManagementRaiseQuery";
    private static final String TEST_EVENT_QM_RESPONSE = "queryManagementRespondQuery";
    private static final long CASE_ID = 1L;
    private static final String QUERY_ID = "1";

    @BeforeEach
    void setup() {
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT, "civil", Map.of("caseId", CASE_ID));
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerQueryManagementEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT_QM, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        Map<String, Object> expected = new HashMap<>();
        expected.put("caseId", CASE_ID);
        expected.put("queryId", QUERY_ID);
        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT_QM, "civil", expected);
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerResponseToQueryEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT_QM_RESPONSE, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        Map<String, Object> expected = new HashMap<>();
        expected.put("caseId", CASE_ID);
        expected.put("queryId", QUERY_ID);
        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT_QM_RESPONSE, "civil", expected);
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT, "civil", Map.of("caseId", CASE_ID));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant(TEST_EVENT, Map.of("caseId", CASE_ID));
        verify(applicationEventPublisher, times(2)).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);
        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT, "civil", Map.of("caseId", CASE_ID));
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withoutTenantId() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT, "civil", Map.of("caseId", CASE_ID));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant(TEST_EVENT, Map.of("caseId", CASE_ID));
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(camundaRuntimeClient).correlateStartMessage(TEST_EVENT, "civil", Map.of("caseId", CASE_ID));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant(TEST_EVENT, Map.of("caseId", CASE_ID));
    }

    private CaseData createCaseData(String event, long caseId) {
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setCamundaEvent(event);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdCaseReference(caseId);

        if (TEST_EVENT_QM.equals(event) || TEST_EVENT_QM_RESPONSE.equals(event)) {
            CaseMessage caseMessage = new CaseMessage();
            caseMessage.setId("1");
            CaseQueriesCollection caseQueriesCollection = new CaseQueriesCollection();
            caseQueriesCollection.setCaseMessages(wrapElements(caseMessage));
            caseData.setQueries(caseQueriesCollection);
        }
        return caseData;
    }
}
