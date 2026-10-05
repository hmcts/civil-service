package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.camunda.community.rest.client.api.ExternalTaskApiClient;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.IncidentApiClient;
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDto;
import org.camunda.community.rest.client.model.StartProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableQueryParameterDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CamundaRestEngineClientTest {

    @Mock
    private ProcessInstanceApiClient processInstanceApiClient;
    @Mock
    private ExternalTaskApiClient externalTaskApiClient;
    @Mock
    private IncidentApiClient incidentApiClient;
    @Mock
    private ProcessDefinitionApiClient processDefinitionApiClient;
    @Mock
    private HistoryApiClient historyApiClient;

    private CamundaRestEngineClient client;

    @BeforeEach
    void setUp() {
        client = new CamundaRestEngineClient(
            processInstanceApiClient,
            externalTaskApiClient,
            incidentApiClient,
            processDefinitionApiClient,
            historyApiClient
        );
    }

    @Test
    void shouldSerializeVariablesWhenStartingProcess() {
        client.startProcessByKey("process-key", Map.of("caseId", 123L, "enabled", true));

        ArgumentCaptor<StartProcessInstanceDto> request = ArgumentCaptor.forClass(StartProcessInstanceDto.class);
        verify(processDefinitionApiClient)
            .startProcessInstanceByKeyAndTenantId(eq("process-key"), eq("civil"), request.capture());

        assertThat(request.getValue().getVariables())
            .extractingByKeys("caseId", "enabled")
            .extracting("value")
            .containsExactly(123L, true);
    }

    @Test
    void shouldQueryHistoricInstancesWithCompatibleVariableFilters() {
        client.getProcessInstances("instance-id", "process-key", "caseId_eq_123,status_neq_CLOSED");

        ArgumentCaptor<HistoricProcessInstanceQueryDto> query =
            ArgumentCaptor.forClass(HistoricProcessInstanceQueryDto.class);
        verify(historyApiClient).queryHistoricProcessInstances(isNull(), isNull(), query.capture());

        assertThat(query.getValue().getProcessInstanceId()).isEqualTo("instance-id");
        assertThat(query.getValue().getProcessDefinitionKey()).isEqualTo("process-key");
        assertThat(query.getValue().getVariables())
            .extracting(
                VariableQueryParameterDto::getName,
                VariableQueryParameterDto::getOperator,
                VariableQueryParameterDto::getValue
            )
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(
                    "caseId", VariableQueryParameterDto.OperatorEnum.EQ, "123"
                ),
                org.assertj.core.groups.Tuple.tuple(
                    "status", VariableQueryParameterDto.OperatorEnum.NEQ, "CLOSED"
                )
            );
    }

    @Test
    void shouldOmitVariableFiltersWhenNotProvided() {
        client.getProcessInstances(null, "process-key", null);

        ArgumentCaptor<HistoricProcessInstanceQueryDto> query =
            ArgumentCaptor.forClass(HistoricProcessInstanceQueryDto.class);
        verify(historyApiClient).queryHistoricProcessInstances(isNull(), isNull(), query.capture());

        assertThat(query.getValue().getVariables()).isNull();
    }
}
