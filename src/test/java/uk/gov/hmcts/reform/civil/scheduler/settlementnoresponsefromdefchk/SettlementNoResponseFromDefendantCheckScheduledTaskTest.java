package uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SettlementNoResponseFromDefendantCheckScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    private SettlementNoResponseFromDefendantCheckScheduledTask task;

    @BeforeEach
    void setUp() {
        task = new SettlementNoResponseFromDefendantCheckScheduledTask(
            applicationEventPublisher,
            defaultBackPressureConfiguration
        );
    }

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(123L);

        Long result = task.getItemId(caseDetails);

        assertThat(result).isEqualTo(123L);
    }

    @Test
    void shouldPublishSettlementNoResponseFromDefendantEvent() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(123L);

        task.accept(caseDetails);

        ArgumentCaptor<SettlementNoResponseFromDefendantEvent> captor =
            ArgumentCaptor.forClass(SettlementNoResponseFromDefendantEvent.class);

        verify(applicationEventPublisher).publishEvent(captor.capture());

        SettlementNoResponseFromDefendantEvent event = captor.getValue();
        assertThat(event.getCaseId()).isEqualTo(123L);
    }

    @Test
    void shouldNotThrowWhenPublishEventFails() {
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getId()).thenReturn(123L);

        doThrow(new RuntimeException("Failure"))
            .when(applicationEventPublisher)
            .publishEvent(any(Object.class));

        assertDoesNotThrow(() -> task.accept(caseDetails));
    }

    @Test
    void shouldReturnBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure())
            .thenReturn(backPressureConfiguration);

        ScheduledTaskBackPressureConfiguration result =
            task.backPressureConfiguration();

        assertThat(result).isSameAs(backPressureConfiguration);

        verify(defaultBackPressureConfiguration).getDefaultBackPressure();
    }
}
