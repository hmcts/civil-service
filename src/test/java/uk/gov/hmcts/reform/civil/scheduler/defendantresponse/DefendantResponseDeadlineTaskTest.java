package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.DefendantResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DefendantResponseDeadlineTask defendantResponseDeadlineTask;

    @Test
    void shouldPublishDefendantResponseDeadlineEvent() {
        long caseId = 123L;
        CaseDetails caseDetails = CaseDetailsBuilder.builder().id(caseId).build();
        DefendantResponseDeadlineCheckEvent event = new DefendantResponseDeadlineCheckEvent(caseDetails.getId());

        defendantResponseDeadlineTask.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(eq(event));
    }
}
