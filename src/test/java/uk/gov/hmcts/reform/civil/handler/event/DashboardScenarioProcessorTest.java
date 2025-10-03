package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardScenarioProcessorTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private IDashboardScenarioService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardScenarioProcessor processor;

    @Captor
    private ArgumentCaptor<ScenarioRequestParams> paramsCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDashboardScenario_shouldCallDashboardService() {
        String caseId = "123";
        String scenario = "SOME_SCENARIO";

        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(123L)
            .build();

        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn("mock-token");
        when(userConfig.getUserName()).thenReturn("user");
        when(userConfig.getPassword()).thenReturn("pass");
        when(mapper.mapCaseDataToParams(caseData))
            .thenReturn(new HashMap<String, Object>() {{
                put("key", "value");
            }});

        processor.createDashboardScenario(caseId, scenario);

        verify(dashboardScenariosService).createScenario(
            eq("mock-token"),
            eq(processor.fromScenario(scenario)),
            eq("123"),
            paramsCaptor.capture()
        );

        ScenarioRequestParams capturedParams = paramsCaptor.getValue();
        assertNotNull(capturedParams);
        assertEquals("value", capturedParams.getParams().get("key"));
    }

    @Test
    void fromScenario_shouldReturnCorrectEnum() {
        DashboardScenarios ds = processor.fromScenario("Scenario.AAA6.ClaimIssue.ClaimSubmit.Required");
        assertEquals(DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED, ds);
    }

    @Test
    void fromScenario_shouldThrowExceptionForInvalidScenario() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            processor.fromScenario("INVALID_SCENARIO")
        );

        assertTrue(exception.getMessage().contains("No enum constant with scenario: INVALID_SCENARIO"));
    }
}
