package uk.gov.hmcts.reform.civil.controllers;

import feign.Client;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.MockMvcFeignClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.dashboard.repositories.*;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@RunWith(SpringRunner.class)
@EnableFeignClients(defaultConfiguration = DashboardBaseIntegrationTest.MockMvcFeignConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DashboardBaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScenarioRepository scenarioRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private TaskItemTemplateRepository taskItemTemplateRepository;
    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;
    @Autowired
    private TaskListRepository taskListRepository;

    public static class MockMvcFeignConfiguration {
        @Bean
        Client feignClient() {
            return new MockMvcFeignClient();
        }
    }

    protected static CallbackParams callbackParams(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    @After
    public void beforeEach() {
        scenarioRepository.deleteAll();
        taskListRepository.deleteAll();
        dashboardNotificationsRepository.deleteAll();
        taskItemTemplateRepository.deleteAll();
        notificationTemplateRepository.deleteAll();
    }
}
