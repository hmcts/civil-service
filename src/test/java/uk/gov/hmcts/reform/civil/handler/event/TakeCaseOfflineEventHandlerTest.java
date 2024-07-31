package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_STATE;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private TakeCaseOfflineEventHandler takeCaseOfflineEventHandler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        takeCaseOfflineEventHandler = new TakeCaseOfflineEventHandler(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldTakeCaseOfflineOnTakeCaseOfflineEvent() {
        Long caseId = 1633357679902210L;

        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .build();
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, "MAIN.DRAFT");
        variables.putValue(FLOW_FLAGS, Map.of());

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        when(coreCaseDataService.startUpdate(caseId.toString(), TAKE_CASE_OFFLINE))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

        when(coreCaseDataService.submitUpdate(eq(caseId.toString()), any(CaseDataContent.class))).thenReturn(caseData);

        TakeCaseOfflineEvent event = new TakeCaseOfflineEvent(caseId);
        takeCaseOfflineEventHandler.takeCaseOffline(event);

        verify(coreCaseDataService).startUpdate(caseId.toString(), TAKE_CASE_OFFLINE);
        verify(coreCaseDataService).submitUpdate(eq(caseId.toString()), any(CaseDataContent.class));
    }

}
