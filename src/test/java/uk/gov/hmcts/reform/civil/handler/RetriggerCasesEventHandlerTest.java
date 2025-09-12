package uk.gov.hmcts.reform.civil.handler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@ExtendWith(MockitoExtension.class)
class RetriggerCasesEventHandlerTest {

    private static final String EVENT_DESCRIPTION = "Process ID: 1";

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataMap;

    @Spy
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(mapper);

    @InjectMocks
    private RetriggerCasesEventHandler handler;

    @Test
    void testHandleTask_RetriggerClaimantResponse() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(1L),
            eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CLAIMANT_RESPONSE"),
            eq(EVENT_DESCRIPTION)
        );
        verify(coreCaseDataService).triggerEvent(
            eq(2L),
            eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CLAIMANT_RESPONSE"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerClaimantResponseSpecific() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CLAIMANT_RESPONSE_SPEC");
        when(externalTask.getVariable("caseIds")).thenReturn(" 1, 2");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(1L),
            eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CLAIMANT_RESPONSE_SPEC"),
            eq(EVENT_DESCRIPTION)
        );
        verify(coreCaseDataService).triggerEvent(
            eq(2L),
            eq(CaseEvent.RETRIGGER_CLAIMANT_RESPONSE_SPEC),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CLAIMANT_RESPONSE_SPEC"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerCases() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn(" 1, 2 ");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(1L),
            eq(CaseEvent.RETRIGGER_CASES),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CASES"),
            eq(EVENT_DESCRIPTION)
        );
        verify(coreCaseDataService).triggerEvent(
            eq(2L),
            eq(CaseEvent.RETRIGGER_CASES),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CASES"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerGACases() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_GA_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn(" 1, 2 ");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getVariable("document")).thenReturn(null);
        when(externalTask.getVariable("ga")).thenReturn("yes");
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            1L,
            CaseEvent.RETRIGGER_GA_CASES
        );
        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            2L,
            CaseEvent.RETRIGGER_GA_CASES
        );
    }

    @Test
    void testHandleTask_RetriggerCasesWithMissingCaseEvent() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerCasesWithMissingCaseIds() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("CASE_EVENT");
        when(externalTask.getVariable("caseIds")).thenReturn(null);

        assertThrows(AssertionError.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testHandleTask_RetriggerCasesThrowsExceptionAndCarriesOn() {
        ExternalTask externalTask = mock(ExternalTask.class);
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        doThrow(new RuntimeException()).when(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(2L),
            eq(CaseEvent.RETRIGGER_CASES),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CASES"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerCasesSetsCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        String caseData = "{\"myField\":\"myValue\"}";
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(caseData);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        doThrow(new RuntimeException()).when(coreCaseDataService).triggerEvent(1L, CaseEvent.RETRIGGER_CASES);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(
            eq(2L),
            eq(CaseEvent.RETRIGGER_CASES),
            anyMap(),
            eq("Re-trigger of RETRIGGER_CASES"),
            eq(EVENT_DESCRIPTION)
        );
    }

    @Test
    void testHandleTask_RetriggerCasesEmptyCaseData() {
        ExternalTask externalTask = mock(ExternalTask.class);
        String caseData = "[]";
        when(externalTask.getVariable("caseEvent")).thenReturn("RETRIGGER_CASES");
        when(externalTask.getVariable("caseIds")).thenReturn("1,2");
        when(externalTask.getVariable("caseData")).thenReturn(caseData);
        when(externalTask.getProcessInstanceId()).thenReturn("1");

        // expect exception when handler.handleTask is called
        assertThrows(IllegalArgumentException.class, () -> handler.handleTask(externalTask));
    }

    @Test
    void testDocumentUpdateFinalOrders() {
        ExternalTask externalTask = mock(ExternalTask.class);
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of("finalOrderDocumentCollection", List.of(Element.<CaseDocument>builder().id(
            UUID.randomUUID()).value(CaseDocument.builder().documentSize(123L).build()).build()))).build();

        when(externalTask.getVariable("caseEvent")).thenReturn("UPDATE_CASE_DATA");
        when(externalTask.getVariable("caseIds")).thenReturn("1");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getVariable("document")).thenReturn("{\n" +
                                                                  "      \"createdBy\" : \"Civil\",\n" +
                                                                  "      \"documentLink\" : {\n" +
                                                                  "        \"category_id\" : \"caseManagementOrders\",\n" +
                                                                  "        \"document_url\" : \"url\",\n" +
                                                                  "        \"upload_timestamp\" : \"2025-05-30T17:36:30.513000\",\n" +
                                                                  "        \"document_filename\" : \"Order_30052025.pdf\",\n" +
                                                                  "        \"document_binary_url\" : \"binary\"\n" +
                                                                  "      },\n" +
                                                                  "      \"documentName\" : \"Order_30052025.pdf\",\n" +
                                                                  "      \"documentSize\" : 43927,\n" +
                                                                  "      \"documentType\" : \"JUDGE_FINAL_ORDER\",\n" +
                                                                  "      \"createdDatetime\" : \"2025-05-30T17:36:30.513000\"\n" +
                                                                  "    }");
        when(externalTask.getProcessInstanceId()).thenReturn("1");
        when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(1L), eq(UPDATE_CASE_DATA), caseDataMap.capture(), any(), any());

        JavaType listType = mapper.getTypeFactory()
            .constructCollectionType(List.class,
                                     mapper.getTypeFactory().constructParametricType(Element.class, CaseDocument.class));
        List<Element<CaseDocument>> finalOrderDocumentCollection = caseDataMap.getValue().get("finalOrderDocumentCollection") != null
            ? mapper.convertValue(caseDataMap.getValue().get("finalOrderDocumentCollection"), listType)
            : emptyList();

        assertThat(finalOrderDocumentCollection.get(1).getValue().getDocumentName()).isEqualTo("Order_30052025.pdf");
    }

    @Test
    void testDocumentUpdateFinalOrders_CurrentCollectionEmpty() {
        ExternalTask externalTask = mock(ExternalTask.class);
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        when(externalTask.getVariable("caseEvent")).thenReturn("UPDATE_CASE_DATA");
        when(externalTask.getVariable("caseIds")).thenReturn("1");
        when(externalTask.getVariable("caseData")).thenReturn(null);
        when(externalTask.getVariable("document")).thenReturn("{\n" +
                                                                  "      \"createdBy\" : \"Civil\",\n" +
                                                                  "      \"documentLink\" : {\n" +
                                                                  "        \"category_id\" : \"caseManagementOrders\",\n" +
                                                                  "        \"document_url\" : \"url\",\n" +
                                                                  "        \"upload_timestamp\" : \"2025-05-30T17:36:30.513000\",\n" +
                                                                  "        \"document_filename\" : \"Order_30052025.pdf\",\n" +
                                                                  "        \"document_binary_url\" : \"binary\"\n" +
                                                                  "      },\n" +
                                                                  "      \"documentName\" : \"Order_30052025.pdf\",\n" +
                                                                  "      \"documentSize\" : 43927,\n" +
                                                                  "      \"documentType\" : \"JUDGE_FINAL_ORDER\",\n" +
                                                                  "      \"createdDatetime\" : \"2025-05-30T17:36:30.513000\"\n" +
                                                                  "    }");
        when(externalTask.getProcessInstanceId()).thenReturn("1");
        when(coreCaseDataService.getCase(any())).thenReturn(caseDetails);

        handler.handleTask(externalTask);

        verify(coreCaseDataService).triggerEvent(eq(1L), eq(UPDATE_CASE_DATA), caseDataMap.capture(), any(), any());

        JavaType listType = mapper.getTypeFactory()
            .constructCollectionType(List.class,
                                     mapper.getTypeFactory().constructParametricType(Element.class, CaseDocument.class));
        List<Element<CaseDocument>> finalOrderDocumentCollection = caseDataMap.getValue().get("finalOrderDocumentCollection") != null
            ? mapper.convertValue(caseDataMap.getValue().get("finalOrderDocumentCollection"), listType)
            : emptyList();

        assertThat(finalOrderDocumentCollection.get(0).getValue().getDocumentName()).isEqualTo("Order_30052025.pdf");
    }
}
