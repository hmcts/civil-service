package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackWitnessOfFactFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackWitnessOfFactFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private FastTrackWitnessOfFactFieldBuilder fastTrackWitnessOfFactFieldBuilder;

    @Test
    void shouldBuildFastTrackWitnessOfFactFields() {
        LocalDate now = LocalDate.now();
        LocalDate expectedDate = now.plusWeeks(8);
        when(workingDayIndicator.getNextWorkingDay(expectedDate)).thenReturn(expectedDate);
        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackWitnessOfFactFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackWitnessOfFact witnessOfFact = caseData.getFastTrackWitnessOfFact();
        assertThat(witnessOfFact).isNotNull();
        assertThat(witnessOfFact.getInput1()).isEqualTo("Each party must upload to the Digital Portal copies of the statements of all witnesses of fact on whom they intend to " +
                "rely.");
        assertThat(witnessOfFact.getInput2()).isEqualTo("3");
        assertThat(witnessOfFact.getInput3()).isEqualTo("3");
        assertThat(witnessOfFact.getInput4()).isEqualTo("For this limitation, a party is counted as a witness.");
        assertThat(witnessOfFact.getInput5()).isEqualTo("Each witness statement should be no more than");
        assertThat(witnessOfFact.getInput6()).isEqualTo("10");
        assertThat(witnessOfFact.getInput7()).isEqualTo("A4 pages. Statements should be double spaced using a font size of 12.");
        assertThat(witnessOfFact.getInput8()).isEqualTo("Witness statements shall be uploaded to the Digital Portal by 4pm on");
        assertThat(witnessOfFact.getDate()).isEqualTo(expectedDate);
        assertThat(witnessOfFact.getInput9()).isEqualTo("Evidence will not be permitted at trial from a witness whose statement has not been uploaded in accordance with this " +
                "Order. Evidence not uploaded, or uploaded late, will not be permitted except with permission from the Court.");
    }
}