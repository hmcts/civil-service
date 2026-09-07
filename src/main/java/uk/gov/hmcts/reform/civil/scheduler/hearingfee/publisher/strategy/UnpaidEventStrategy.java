package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.HearingFeeHelper;

import java.util.function.LongFunction;

@Component
@AllArgsConstructor
@Order(3)
public class UnpaidEventStrategy implements HearingFeeEventStrategy {

    private final HearingFeeHelper hearingFeeHelper;

    @Override
    public boolean supports(CaseData caseData) {
        return hearingFeeHelper.isHearingFeeUnpaid(caseData.getHearingFeePaymentDetails(), caseData);
    }

    @Override
    public LongFunction<Object> getEventFactory() {
        return HearingFeeUnpaidEvent::new;
    }

    @Override
    public String getEventName() {
        return "HearingFeeUnpaidEvent";
    }
}
