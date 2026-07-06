package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy;

import uk.gov.hmcts.reform.civil.model.CaseData;
import java.util.function.Function;

public interface HearingFeeEventStrategy {

    boolean supports(CaseData caseData);

    Function<Long, Object> getEventFactory();

    String getEventName();
}
