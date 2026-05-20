package uk.gov.hmcts.reform.civil.workflow.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.dashboard.fixture.OrderMadeDefendantFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@SuppressWarnings("java:S5960")
class OrderMadeDefendantWorkflowTest extends DashboardWorkflowIntegrationTest {

    public static final String DEFENDANT = "DEFENDANT";
    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);
        when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
    }

    @Test
    void shouldCreateOrderMadeDefendantNotificationViaAboutToSubmitCallback() throws Exception {
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

        startWorkflow(OrderMadeDefendantFixtures.caseData())
            .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, OrderMadeDefendantFixtures.caseReference(), DEFENDANT)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titleEn").value("An order has been made"))
            .andExpect(jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">The judge has made an order on your claim.</p><p class=\"govuk-body\">"
                    + "<a href=\"{VIEW_FINAL_ORDER}\" rel=\"noopener noreferrer\" target=\"_blank\""
                    + " class=\"govuk-link\">View the order</a></p>"
            ));
    }

    @Test
    void shouldCreateInactiveTrialArrangementsTaskForDefendantWhenFinalOrdersIssuedForFastTrack() throws Exception {
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

        startWorkflow(OrderMadeDefendantFixtures.caseData())
            .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        doGet(
            BEARER_TOKEN,
            GET_NOTIFICATIONS_URL,
            OrderMadeDefendantFixtures.caseReference(),
            DEFENDANT
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titleEn").value("An order has been made"));

        doGet(
            BEARER_TOKEN,
            GET_TASKS_ITEMS_URL,
            OrderMadeDefendantFixtures.caseReference(),
            DEFENDANT
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[1].reference").value(OrderMadeDefendantFixtures.caseReference()))
            .andExpect(jsonPath("$[1].taskNameEn").value("<a>Add the trial arrangements</a>"))
            .andExpect(jsonPath("$[1].currentStatusEn").value("Inactive"));
    }
}
