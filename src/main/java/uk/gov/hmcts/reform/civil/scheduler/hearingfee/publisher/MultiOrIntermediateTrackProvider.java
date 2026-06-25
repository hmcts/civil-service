package uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class MultiOrIntermediateTrackProvider implements HearingFeeLogging {

    private static final String PUBLISHING = "Publishing ";

    private final HearingFeeEventPublisher hearingFeeEventPublisher;
    private final List<HearingFeeEventStrategy> strategies;

    public Consumer<Long> getPublisher(CaseData caseData) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(caseData))
            .findFirst()
            .map(strategy -> hearingFeeEventPublisher.createPublisher(
                getLogMessage(PUBLISHING, strategy.getEventName(), caseData),
                strategy.getEventFactory()
            ))
            .orElseThrow(() -> new IllegalStateException("Hearing fee payment details are not set for case: " + caseData.getCcdCaseReference()));
    }
}
