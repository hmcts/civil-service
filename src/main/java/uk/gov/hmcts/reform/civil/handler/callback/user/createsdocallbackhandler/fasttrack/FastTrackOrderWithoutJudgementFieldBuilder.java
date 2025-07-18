package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackOrderWithoutJudgementFieldBuilder implements SdoCaseFieldBuilder {

    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackOrderWithoutJudgement(FastTrackOrderWithoutJudgement.builder()
                .input(String.format(
                        "This order has been made without hearing. Each party has the right to apply to have this Order set aside or varied." +
                                " Any such application must be received by the Court (together with the appropriate fee) by 4pm on %s.",
                        deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(
                                        LocalDateTime.now())
                                .format(DateTimeFormatter.ofPattern(
                                        "dd MMMM yyyy",
                                        Locale.ENGLISH
                                ))
                ))
                .build());
    }
}
