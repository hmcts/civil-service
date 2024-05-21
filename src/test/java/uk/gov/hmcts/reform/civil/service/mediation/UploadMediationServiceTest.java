package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM;

@ExtendWith(MockitoExtension.class)
public class UploadMediationServiceTest {

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private DashboardApiClient dashboardApiClient;
    @InjectMocks
    private UploadMediationService uploadMediationService;

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void shouldReturnGetScenarios() {
        //Given
        String[] expectedScenarios =  new String[]{
            SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM.getScenario(),
            SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM.getScenario()
        };
        //When
        String[] scenarios = uploadMediationService.getScenarios();
        //Then
        assertThat(scenarios).isEqualTo(expectedScenarios);
    }

    @Test
    void shouldReturnRecordScenarios() {
        //Given
        String[] expectedScenarios =  new String[]{
            SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM.getScenario(),
            SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM.getScenario()
        };
        params.put("ccdCaseReference", "123");

        when(mapper.mapCaseDataToParams(any())).thenReturn(params);

        var caseData = CaseDataBuilder.builder()
            .caseReference(
                Long.valueOf("123"))
            .build();
        //When
        uploadMediationService.recordScenarios(expectedScenarios, caseData, "token");

        //Then
        verify(dashboardApiClient, times(2)).recordScenario(any(), any(), any(), any());
    }
}

