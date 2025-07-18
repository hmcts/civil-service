package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackTrialFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FastTrackTrialFieldBuilderTest {

    @InjectMocks
    private FastTrackTrialFieldBuilder fastTrackTrialFieldBuilder;

    @Test
    void shouldBuildFastTrackTrialFields() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackTrialFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackTrial fastTrackTrial = caseData.getFastTrackTrial();
        assertThat(fastTrackTrial).isNotNull();
        assertThat(fastTrackTrial.getInput1()).isEqualTo("The time provisionally allowed for this trial is");
        assertThat(fastTrackTrial.getDate1()).isEqualTo(LocalDate.now().plusWeeks(22));
        assertThat(fastTrackTrial.getDate2()).isEqualTo(LocalDate.now().plusWeeks(30));
        assertThat(fastTrackTrial.getInput2()).isEqualTo("If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date " +
                "stated on this order.");
        assertThat(fastTrackTrial.getInput3()).isEqualTo("At least 7 days before the trial, the claimant must upload to the Digital Portal");
        assertThat(fastTrackTrial.getType()).isEqualTo(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS));
    }
}