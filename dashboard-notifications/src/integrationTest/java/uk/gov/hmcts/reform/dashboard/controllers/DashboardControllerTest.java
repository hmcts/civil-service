package uk.gov.hmcts.reform.dashboard.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest extends BaseIntegrationTest {

    @MockBean
    private DashboardNotificationService dashboardNotificationService;

    private final String endPointUrl = "/dashboard/notifications/{uuid}";



    @Test
    @SneakyThrows
    void shouldReturnOkWhenDeletingEntity() {

        doGet(BEARER_TOKEN, null, endPointUrl, "1122")
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnErrorWhenServiceReturnsError() {
        UUID id = UUID.fromString("1122");

        doThrow(new Exception()).when(dashboardNotificationService).getNotification(id);

        doGet(BEARER_TOKEN, null, endPointUrl, "1122")
            .andExpect(status().isInternalServerError());
    }

}
