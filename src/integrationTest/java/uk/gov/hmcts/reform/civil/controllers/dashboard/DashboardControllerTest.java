package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.controller.DashboardController;
import uk.gov.hmcts.reform.dashboard.service.DashboardNotificationService;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest extends BaseIntegrationTest {

    @Autowired
    private DashboardController dashboardController;

    @MockBean
    private DashboardNotificationService dashboardNotificationService;

    private final String endPointUrl = "/dashboard-notifications/{unique-notification-identifier}";

    @Test
    @SneakyThrows
    void shouldReturnOkWhenDeletingEntity() {
        UUID id = UUID.fromString("1122");

        doDelete(BEARER_TOKEN, null, endPointUrl, "1122")
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnErrorWhenServiceReturnsError() {
        UUID id = UUID.fromString("1122");

        doThrow(new Exception()).when(dashboardNotificationService).delete(id);

        doDelete(BEARER_TOKEN, null, endPointUrl, "1122")
            .andExpect(status().isInternalServerError());
    }

}
