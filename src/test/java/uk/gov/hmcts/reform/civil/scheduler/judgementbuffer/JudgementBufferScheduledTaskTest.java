package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JudgementBufferScheduledTaskTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private JudgementBufferScheduledTask task;

    @Test
    void shouldTriggerUpdateWhenCaseIsEligibleForDefaultJudgement() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        task.accept(caseDetails);

        verify(coreCaseDataService).triggerEvent(caseId, CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC);
    }
}
