package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.casedismissed.CaseDismissedScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnGoingBusinessProcessCheckTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private InterceptorChain<Object> chain;

    private OnGoingBusinessProcessCheck<Object> check;

    @BeforeEach
    void setUp() {
        check = new OnGoingBusinessProcessCheck<>(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldCallNext_whenItemIsNotCaseDetails() {
        InterceptorContext<Object> context = new InterceptorContext<>("scheduler", new Object());

        check.accept(context, chain);

        verify(chain).next(context);
        verify(coreCaseDataService, never()).getCase(anyLong());
    }

    @Test
    void shouldCallNext_whenCaseHasNoOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseData.builder().build(); // hasNoOngoingBusinessProcess returns true by default if businessProcess is null

        InterceptorContext<Object> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        check.accept(context, chain);

        verify(chain).next(context);
        assertThat(context.getAttribute(OnGoingBusinessProcessCheck.CASE_DATA_KEY)).isPresent().contains(caseData);
    }

    @Test
    void shouldThrowTaskAbortedException_whenCaseHasOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseData.builder()
            .businessProcess(new uk.gov.hmcts.reform.civil.model.BusinessProcess()
                                 .setStatus(uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED))
            .build();

        InterceptorContext<Object> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> check.accept(context, chain));
        assertThat(exception.getReason()).isEqualTo("Ongoing business process");
        verify(chain, never()).next(context);
    }

    @Test
    void shouldSupportCaseDismissedScheduler() {
        assertThat(check.supports(CaseDismissedScheduler.SCHEDULER_NAME)).isTrue();
        assertThat(check.supports("otherScheduler")).isFalse();
    }
}
