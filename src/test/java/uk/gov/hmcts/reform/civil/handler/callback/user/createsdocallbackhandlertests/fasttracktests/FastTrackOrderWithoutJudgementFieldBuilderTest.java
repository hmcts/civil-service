package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackOrderWithoutJudgementFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackOrderWithoutJudgementFieldBuilderTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private FastTrackOrderWithoutJudgementFieldBuilder fastTrackOrderWithoutJudgementFieldBuilder;

    @Test
    void shouldBuildFastTrackOrderWithoutJudgementFields() {
        LocalDateTime now = LocalDateTime.now();
        when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
                .thenReturn(LocalDate.from(now.plusWeeks(1)));

        final String formattedDate = now.plusWeeks(1).format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH));

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackOrderWithoutJudgementFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackOrderWithoutJudgement orderWithoutJudgement = caseData.getFastTrackOrderWithoutJudgement();
        assertThat(orderWithoutJudgement).isNotNull();
        assertThat(orderWithoutJudgement.getInput()).isEqualTo(
                "This order has been made without hearing. Each party has the right to apply to have this Order set aside or varied. Any such application must be received by the" +
                        " Court (together with the appropriate fee) by 4pm on " + formattedDate + "."
        );
    }
}