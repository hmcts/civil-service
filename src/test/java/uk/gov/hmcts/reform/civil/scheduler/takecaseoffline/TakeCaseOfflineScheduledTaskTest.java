package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @InjectMocks
    private TakeCaseOfflineScheduledTask task;

    @Test
    void shouldPublishTakeCaseOfflineEvent() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new TakeCaseOfflineEvent(caseId));
    }
}
