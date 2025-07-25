package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackHearingTimeFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FastTrackHearingTimeFieldBuilderTest {

    @InjectMocks
    private FastTrackHearingTimeFieldBuilder fastTrackHearingTimeFieldBuilder;

    @Test
    void shouldBuildFastTrackHearingTimeFields() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackHearingTimeFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackHearingTime hearingTime = caseData.getFastTrackHearingTime();
        assertThat(hearingTime).isNotNull();
        assertThat(hearingTime.getDateFrom()).isEqualTo(LocalDate.now().plusWeeks(22));
        assertThat(hearingTime.getDateTo()).isEqualTo(LocalDate.now().plusWeeks(30));
        assertThat(hearingTime.getDateToToggle()).contains(DateToShowToggle.SHOW);
        assertThat(hearingTime.getHelpText1()).isEqualTo("If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date " +
                "of this order.");
    }
}