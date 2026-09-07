package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import java.util.function.LongFunction;

public interface HearingFeeEventStrategy {

    boolean supports(CaseData caseData);

    LongFunction<Object> getEventFactory();

    String getEventName();
}
