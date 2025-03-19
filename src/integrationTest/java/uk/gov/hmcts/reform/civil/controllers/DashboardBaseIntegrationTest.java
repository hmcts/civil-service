package uk.gov.hmcts.reform.civil.controllers;

import feign.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.MockMvcFeignClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationActionRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.Map;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@RunWith(SpringRunner.class)
@EnableFeignClients(defaultConfiguration = DashboardBaseIntegrationTest.MockMvcFeignConfiguration.class)
public class DashboardBaseIntegrationTest extends BaseIntegrationTest {

    @MockBean
    protected FeatureToggleService featureToggleService;

    @Autowired
    private DashboardNotificationsRepository dashboardNotificationsRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private NotificationActionRepository notificationActionRepository;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    protected CoreCaseUserService coreCaseUserService;

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

    @AfterEach
    public void after() {
        taskListRepository.deleteAll();
        notificationActionRepository.deleteAll();
        dashboardNotificationsRepository.deleteAll();
    }
}
