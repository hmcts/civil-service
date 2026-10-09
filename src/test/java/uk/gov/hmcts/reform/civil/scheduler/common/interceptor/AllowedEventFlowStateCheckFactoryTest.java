package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@ExtendWith(MockitoExtension.class)
class AllowedEventFlowStateCheckFactoryTest {

    @Mock
    private AllowedEventService allowedEventService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    private AllowedEventFlowStateCheckFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AllowedEventFlowStateCheckFactory(
            allowedEventService,
            stateFlowEngine,
            caseDetailsConverter,
            coreCaseDataService
        );
    }

    @Test
    void shouldCreateAllowedEventFlowStateCheck() {
        SchedulerInterceptor<CaseDetails> result = factory.forEvent(TAKE_CASE_OFFLINE);

        assertThat(result).isInstanceOf(AllowedEventFlowStateCheck.class);
        assertThat(result.getOrder()).isEqualTo(100);
    }

    @Test
    void shouldCreateNewInstanceOnEachCall() {
        assertThat(factory.forEvent(TAKE_CASE_OFFLINE)).isNotSameAs(factory.forEvent(TAKE_CASE_OFFLINE));
    }

    @Test
    void shouldCreateCheckConfiguredWithEventAndDependencies() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);
        @SuppressWarnings("unchecked")
        InterceptorChain<CaseDetails> chain = mock(InterceptorChain.class);
        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        factory.forEvent(TAKE_CASE_OFFLINE).accept(context, chain);

        verify(allowedEventService).isAllowed(caseData, TAKE_CASE_OFFLINE);
        verify(chain).next(context);
    }

    @Test
    void shouldCreateCheckThatAbortsWhenEventNotAllowed() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);
        @SuppressWarnings("unchecked")
        InterceptorChain<CaseDetails> chain = mock(InterceptorChain.class);
        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);
        SchedulerInterceptor<CaseDetails> check = factory.forEvent(TAKE_CASE_OFFLINE);

        assertThatThrownBy(() -> check.accept(context, chain))
            .isInstanceOf(TaskAbortedException.class)
            .hasMessageContaining("TAKE_CASE_OFFLINE");
    }
}
