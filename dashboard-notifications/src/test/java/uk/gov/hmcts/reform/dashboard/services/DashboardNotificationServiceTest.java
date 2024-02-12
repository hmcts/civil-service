package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DashboardNotificationServiceTest {

    private DashboardNotificationService dashboardNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        dashboardNotificationService = new DashboardNotificationService(notificationRepository);
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldReturnOkWhenDeletingEntity() {

            dashboardNotificationService.deleteById(id);

            verify(notificationRepository).deleteById(id);
        }
    }
}
