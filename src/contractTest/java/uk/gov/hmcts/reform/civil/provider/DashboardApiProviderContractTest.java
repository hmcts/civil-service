package uk.gov.hmcts.reform.civil.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.dashboard.controllers.DashboardController;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Provider("dashboard_api")
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch}"
)
@IgnoreNoPactsToVerify
class DashboardApiProviderContractTest {

    private MockMvc mockMvc;

    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;

    private AutoCloseable mocks;

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @BeforeEach
    @SuppressWarnings("java:S2699")
    void beforeEach(PactVerificationContext context) {
        String brokerUrl = System.getenv("PACT_BROKER_FULL_URL");
        if (brokerUrl != null && !brokerUrl.isBlank()) {
            System.setProperty("pactbroker.url", brokerUrl);
        }

        mocks = MockitoAnnotations.openMocks(this);

        DashboardController dashboardController = new DashboardController(
            taskListService,
            dashboardNotificationService,
            dashboardScenariosService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .alwaysDo(result -> result.getResponse().setContentType(APPLICATION_JSON_VALUE))
            .build();

        if (context != null) {
            MockMvcTestTarget target = new MockMvcTestTarget();
            target.setMockMvc(mockMvc);
            context.setTarget(target);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
