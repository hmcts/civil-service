package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy.HearingFeeEventStrategy;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy.PaidEventStrategy;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.strategy.UnpaidEventStrategy;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class PreMultiIntermediateClaimProvider implements HearingFeeLogging {

    private static final String PUBLISH_PREFIX = "preMultiIntermediateClaimLogic publishing ";

    private final HearingFeeEventPublisher hearingFeeEventPublisher;
    private final PaidEventStrategy paidEventStrategy;
    private final UnpaidEventStrategy unpaidEventStrategy;

    public Consumer<Long> getPublisher(CaseData caseData) {
        HearingFeeEventStrategy strategy;
        if (paidEventStrategy.supports(caseData)) {
            strategy = paidEventStrategy;
        } else {
            strategy = unpaidEventStrategy;
        }

        return hearingFeeEventPublisher.createPublisher(
            getLogMessage(PUBLISH_PREFIX, strategy.getEventName(), caseData),
            strategy.getEventFactory()
        );
    }
}
