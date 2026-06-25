package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaidEventStrategyTest {

    @Mock
    private HearingFeeHelper hearingFeeHelper;

    private PaidEventStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PaidEventStrategy(hearingFeeHelper);
    }

    @Test
    void shouldSupport_whenHearingFeeIsPaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(new PaymentDetails())
            .build();
        when(hearingFeeHelper.isHearingFeePaid(caseData.getHearingFeePaymentDetails(), caseData)).thenReturn(true);

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void shouldNotSupport_whenHearingFeeIsNotPaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(new PaymentDetails())
            .build();
        when(hearingFeeHelper.isHearingFeePaid(caseData.getHearingFeePaymentDetails(), caseData)).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void shouldReturnCorrectEventFactory() {
        Function<Long, Object> eventFactory = strategy.getEventFactory();
        Object event = eventFactory.apply(123L);
        assertThat(event).isInstanceOf(HearingFeePaidEvent.class);
        assertThat(((HearingFeePaidEvent) event).getCaseId()).isEqualTo(123L);
    }

    @Test
    void shouldReturnCorrectEventName() {
        assertThat(strategy.getEventName()).isEqualTo("HearingFeePaidEvent");
    }
}
