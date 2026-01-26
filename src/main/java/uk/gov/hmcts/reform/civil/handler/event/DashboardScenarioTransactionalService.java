package uk.gov.hmcts.reform.civil.handler.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import jakarta.persistence.EntityManager;

@Service
@Slf4j
public class DashboardScenarioTransactionalService implements IDashboardScenarioService {

    private final DashboardScenariosService dashboardScenariosService;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;

    @Autowired
    public DashboardScenarioTransactionalService(DashboardScenariosService dashboardScenariosService,
                                                 PlatformTransactionManager transactionManager, EntityManager entityManager) {
        this.dashboardScenariosService = dashboardScenariosService;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    @Override
    public void createScenario(String bearerToken, DashboardScenarios scenario, String caseReference, ScenarioRequestParams params) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            log.info("Starting transactional scenario creation");

            entityManager.joinTransaction();

            dashboardScenariosService.recordScenarios(
                bearerToken,
                scenario.getScenario(),
                caseReference,
                params
            );
        });
    }

}

