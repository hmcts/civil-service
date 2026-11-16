package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration;

/**
 * One in-memory process engine per test JVM. Building an engine creates the H2 schema and took about a
 * second, paid by every bpmn test class when each built and closed its own.
 */
final class SharedProcessEngine {

    private static ProcessEngine engine;

    private SharedProcessEngine() {
    }

    /**
     * Returns the shared engine with every deployment removed, cascading to process instances, jobs,
     * external tasks, message subscriptions and history, so each class starts from an empty engine.
     */
    static synchronized ProcessEngine reset() {
        if (engine == null) {
            engine = createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
            Runtime.getRuntime().addShutdownHook(new Thread(engine::close));
        }
        RepositoryService repositoryService = engine.getRepositoryService();
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
        return engine;
    }
}
