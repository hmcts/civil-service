package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.HearingFeeHelper;

import java.util.function.LongFunction;

@Component
@AllArgsConstructor
@Order(2)
public class PaidEventStrategy implements HearingFeeEventStrategy {

    private final HearingFeeHelper hearingFeeHelper;

    @Override
    public boolean supports(CaseData caseData) {
        return hearingFeeHelper.isHearingFeePaid(caseData.getHearingFeePaymentDetails(), caseData);
    }

    @Override
    public LongFunction<Object> getEventFactory() {
        return HearingFeePaidEvent::new;
    }

    @Override
    public String getEventName() {
        return "HearingFeePaidEvent";
    }
}
