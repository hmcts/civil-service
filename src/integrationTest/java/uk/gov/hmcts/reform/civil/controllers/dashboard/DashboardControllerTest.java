package uk.gov.hmcts.reform.civil.controllers.dashboard;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.entities.NotificationEntity;
import uk.gov.hmcts.reform.dashboard.entities.NotificationTemplateEntity;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest extends BaseIntegrationTest {

    @MockBean
    private NotificationRepository notificationRepository;

    private final String endPointUrlGet = "/dashboard/notifications/{uuid}";

    private final String endPointUrlRecord = "/dashboard/notifications/{unique-notification-identifier}";

    private final UUID id = UUID.randomUUID();

    @Nested
    class GenericTests {
        @Test
        @SneakyThrows
        void shouldReturnErrorWhenNotUuidFormat() {

            doGet(BEARER_TOKEN, endPointUrlGet,  "1234")
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetTests {
        @Test
        @SneakyThrows
        void shouldReturnOkWhenGettingExistingEntity() {
            NotificationEntity notification = new NotificationEntity(id,
                                                                     new NotificationTemplateEntity(),
                                                                     "12345",
                                                                     "name",
                                                                     "Defendant",
                                                                     "en",
                                                                     "cy",
                                                                     "params",
                                                                     "createdBy",
                                                                     new Date(),
                                                                     "updatedBy",
                                                                     new Date());

            when(notificationRepository.findById(any())).thenReturn(Optional.of(notification));

            doGet(BEARER_TOKEN, endPointUrlGet, id)
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(notification)));
        }

        @Test
        @SneakyThrows
        void shouldReturnPreconditionFailedErrorWhenIllegalArgumentExceptionIsThrown() {
            doThrow(new IllegalArgumentException()).when(notificationRepository).findById(any());

            //I don't think it should throw this specific error, but I'll leave it to the person working on get notifications to fix.
            doGet(BEARER_TOKEN, endPointUrlGet, id)
                .andExpect(status().isPreconditionFailed());
        }
    }

    @Nested
    class DeleteTests {
        @Test
        @SneakyThrows
        void shouldReturnOkWhenDeletingEntity() {

            doDelete(BEARER_TOKEN, null, endPointUrlRecord, id.toString())
                .andExpect(status().isOk());
        }

        @Test
        @SneakyThrows
        void shouldReturnPreconditionFailedErrorWhenIllegalArgumentError() {
            doThrow(new IllegalArgumentException()).when(notificationRepository).deleteById(id);

            doDelete(BEARER_TOKEN, null, endPointUrlRecord, id)
                .andExpect(status().isPreconditionFailed());
        }
    }
}