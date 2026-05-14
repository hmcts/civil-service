package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgementBufferScheduledTaskTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JudgementBufferScheduledTask task;

    @Test
    void shouldTriggerUpdateWhenCaseIsEligibleForDefaultJudgement() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().build())
            .build();

        when(coreCaseDataService.startUpdate("1", CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC)).thenReturn(startEventResponse);
        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);
        when(coreCaseDataService.caseDataContentFromStartEventResponse(eq(startEventResponse), any())).thenReturn(caseDataContent);

        task.accept(caseDetails);

        verify(coreCaseDataService).submitUpdate("1", caseDataContent);
    }
}
