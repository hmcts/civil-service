package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Function;

@Component
@Order(1)
public class NoFeeDueEventStrategy implements HearingFeeEventStrategy {

    @Override
    public boolean supports(CaseData caseData) {
        return caseData.getHearingDueDate() == null;
    }

    @Override
    public Function<Long, Object> getEventFactory() {
        return NoHearingFeeDueEvent::new;
    }

    @Override
    public String getEventName() {
        return "NoHearingFeeDueEvent";
    }
}
