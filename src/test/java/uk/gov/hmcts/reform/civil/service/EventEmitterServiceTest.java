package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
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
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class EventEmitterServiceTest {

    @InjectMocks
    private EventEmitterService eventEmitterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RemoteProcessEngineException mockedRemoteProcessEngineException;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    private static final String TEST_EVENT = "TEST_EVENT";
    private static final String TEST_EVENT_QM = "queryManagementRaiseQuery";
    private static final String TEST_EVENT_QM_RESPONSE = "queryManagementRespondQuery";
    private static final long CASE_ID = 1L;
    private static final String QUERY_ID = "1";

    @BeforeEach
    void setup() {
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.tenantId(any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerQueryManagementEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT_QM, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT_QM);
        verify(messageCorrelationBuilder).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).setVariable("queryId", QUERY_ID);
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerResponseToQueryEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT_QM_RESPONSE, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT_QM_RESPONSE);
        verify(messageCorrelationBuilder).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).setVariable("queryId", QUERY_ID);
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage())
            .thenThrow(mockedRemoteProcessEngineException)
            .thenReturn(null);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).withoutTenantId();
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verify(applicationEventPublisher, times(2)).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
    }

    private CaseData createCaseData(String event, long caseId) {
        BusinessProcess businessProcess = BusinessProcess.builder().camundaEvent(event).build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder()
            .businessProcess(businessProcess)
            .ccdCaseReference(caseId);

        if (TEST_EVENT_QM.equals(event) || TEST_EVENT_QM_RESPONSE.equals(event)) {
            caseDataBuilder.qmApplicantSolicitorQueries(CaseQueriesCollection.builder()
                                                            .caseMessages(wrapElements(
                                                                CaseMessage.builder()
                                                                    .id("1")
                                                                    .build()))
                                                            .build());
        }
        return caseDataBuilder.build();
    }
}
