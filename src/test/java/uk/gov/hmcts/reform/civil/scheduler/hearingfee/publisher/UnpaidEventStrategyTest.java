package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnpaidEventStrategyTest {

    @Mock
    private HearingFeeHelper hearingFeeHelper;

    private UnpaidEventStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UnpaidEventStrategy(hearingFeeHelper);
    }

    @Test
    void shouldSupport_whenHearingFeeIsUnpaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(new PaymentDetails())
            .build();
        when(hearingFeeHelper.isHearingFeeUnpaid(caseData.getHearingFeePaymentDetails(), caseData)).thenReturn(true);

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void shouldNotSupport_whenHearingFeeIsNotUnpaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(new PaymentDetails())
            .build();
        when(hearingFeeHelper.isHearingFeeUnpaid(caseData.getHearingFeePaymentDetails(), caseData)).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void shouldReturnCorrectEventFactory() {
        Function<Long, Object> eventFactory = strategy.getEventFactory();
        Object event = eventFactory.apply(123L);
        assertThat(event).isInstanceOf(HearingFeeUnpaidEvent.class);
        assertThat(((HearingFeeUnpaidEvent) event).getCaseId()).isEqualTo(123L);
    }

    @Test
    void shouldReturnCorrectEventName() {
        assertThat(strategy.getEventName()).isEqualTo("HearingFeeUnpaidEvent");
    }
}
