package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Function;

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
    public Function<Long, Object> getEventFactory() {
        return HearingFeeUnpaidEvent::new;
    }

    @Override
    public String getEventName() {
        return "HearingFeeUnpaidEvent";
    }
}
