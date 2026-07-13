package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class NoFeeDueEventStrategyTest {

    private NoFeeDueEventStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NoFeeDueEventStrategy();
    }

    @Test
    void shouldSupport_whenHearingDueDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void shouldNotSupport_whenHearingDueDateIsNotNull() {
        CaseData caseData = CaseDataBuilder.builder().hearingDueDate(LocalDate.now()).build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void shouldReturnCorrectEventFactory() {
        Function<Long, Object> eventFactory = strategy.getEventFactory();
        Object event = eventFactory.apply(123L);
        assertThat(event).isInstanceOf(NoHearingFeeDueEvent.class);
        assertThat(((NoHearingFeeDueEvent) event).getCaseId()).isEqualTo(123L);
    }

    @Test
    void shouldReturnCorrectEventName() {
        assertThat(strategy.getEventName()).isEqualTo("NoHearingFeeDueEvent");
    }
}
