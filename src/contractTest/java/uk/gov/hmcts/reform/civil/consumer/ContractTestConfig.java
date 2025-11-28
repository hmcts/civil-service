package uk.gov.hmcts.reform.civil.consumer;

import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.civil.handler.event.IDashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.dashboard.repositories.ScenarioRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

@TestConfiguration
public class ContractTestConfig {

    @Bean
    ScenarioRepository scenarioRepository() {
        return Mockito.mock(ScenarioRepository.class);
    }

    @Bean
    DashboardNotificationsRepository dashboardNotificationsRepository() {
        return Mockito.mock(DashboardNotificationsRepository.class);
    }

    @Bean
    TaskListRepository taskListRepository() {
        return Mockito.mock(TaskListRepository.class);
    }

    @Bean
    NotificationActionRepository notificationActionRepository() {
        return Mockito.mock(NotificationActionRepository.class);
    }

    @Bean
    TaskItemTemplateRepository taskItemTemplateRepository() {
        return Mockito.mock(TaskItemTemplateRepository.class);
    }

    @Bean
    RoboticsNotificationService roboticsNotificationService() {
        return Mockito.mock(RoboticsNotificationService.class);
    }

    @Bean(name = "dashboardScenarioTransactionalService")
    IDashboardScenarioService dashboardScenarioTransactionalService() {
        return Mockito.mock(IDashboardScenarioService.class);
    }

    @Bean
    static BeanFactoryPostProcessor removeDashboardScenarioTransactionalService() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            if (beanFactory instanceof BeanDefinitionRegistry registry
                && registry.containsBeanDefinition("dashboardScenarioTransactionalService")) {
                registry.removeBeanDefinition("dashboardScenarioTransactionalService");
            }
        };
    }
}
