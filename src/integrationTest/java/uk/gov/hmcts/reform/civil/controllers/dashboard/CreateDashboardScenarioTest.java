package uk.gov.hmcts.reform.civil.controllers.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/scripts/create_dashboard_notifications_and_task_list_templates.sql")
public class CreateDashboardScenarioTest extends BaseIntegrationTest {

    public static final String NOTICE_HEARING_FEE_PAYMENT_REQUIRED = "notice.hearing.fee.payment.required";
    private static final String DASHBOARD_CREATE_SCENARIO_URL = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario() throws Exception {
        doPost(BEARER_TOKEN,
              ScenarioRequestParams.builder()
                          .params(Map.of("hearingFeePayByTime", "4 pm",
                                         "hearingFeePayByDate", OffsetDateTime.now()
                          ))
                          .build(),
               DASHBOARD_CREATE_SCENARIO_URL, NOTICE_HEARING_FEE_PAYMENT_REQUIRED, UUID.randomUUID()
        )
            .andExpect(status().isOk());
    }
}
