package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import jakarta.persistence.EntityManager;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardScenarioTransactionalServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private EntityManager entityManager;

    private DashboardScenarioTransactionalService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DashboardScenarioTransactionalService(dashboardScenariosService, transactionManager, entityManager);
    }

    @Test
    void shouldCreateScenarioSuccessfully() {
        // Arrange
        String bearerToken = "testBearerToken";
        String caseReference = "12345";
        DashboardScenarios scenario = DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT;
        ScenarioRequestParams params = ScenarioRequestParams.builder().build();

        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionManager.getTransaction(any())).thenReturn(null);
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        // Act
        service.createScenario(bearerToken, scenario, caseReference, params);

        // Assert
        verify(dashboardScenariosService, times(1)).recordScenarios(
            eq(bearerToken),
            eq(scenario.getScenario()),
            eq(caseReference),
            eq(params)
        );
    }
}
