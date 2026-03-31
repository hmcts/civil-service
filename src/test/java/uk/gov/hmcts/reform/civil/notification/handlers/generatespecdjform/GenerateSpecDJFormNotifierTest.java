package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private GenerateSpecDJFormAllPartiesEmailGenerator partiesEmailGenerator;

    private GenerateSpecDJFormNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new GenerateSpecDJFormNotifier(notificationService, caseTaskTrackingService, partiesEmailGenerator);
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("GenerateSpecDJFormNotifier");
    }
}

