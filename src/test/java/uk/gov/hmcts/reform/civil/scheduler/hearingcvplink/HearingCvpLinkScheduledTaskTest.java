package uk.gov.hmcts.reform.civil.scheduler.hearingcvplink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingCvpLinkScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @InjectMocks
    private HearingCvpLinkScheduledTask task;

    @Test
    void shouldPublishCvpJoinLinkEvent() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
    }

    @Test
    void shouldNotThrottleWhenBatchSizeIsOne() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails, 1);

        verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
    }

    @Test
    void shouldRestoreInterruptedFlagWhenThrottleIsInterrupted() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();
        eventProperties.setDispatchDelay(2000);
        eventProperties.setLockDuration(600000);

        try {
            Thread.currentThread().interrupt();

            task.accept(caseDetails, 26);

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
        verify(applicationEventPublisher).publishEvent(new CvpJoinLinkEvent(caseId));
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setDispatchDelay(0);
        properties.setLockDuration(600000);
        return properties;
    }
}
