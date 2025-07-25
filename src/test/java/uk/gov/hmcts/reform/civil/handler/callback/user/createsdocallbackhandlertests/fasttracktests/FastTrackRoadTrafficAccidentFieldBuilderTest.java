package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackRoadTrafficAccidentFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackRoadTrafficAccidentFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackRoadTrafficAccidentFieldBuilder fastTrackRoadTrafficAccidentFieldBuilder;

    @Test
    void shouldBuildFastTrackRoadTrafficAccidentFields() {
        LocalDate now = LocalDate.now();
        LocalDate expectedDate = now.plusWeeks(8);
        when(workingDayIndicator.getNextWorkingDay(expectedDate)).thenReturn(expectedDate);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackRoadTrafficAccidentFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackRoadTrafficAccident roadTrafficAccident = caseData.getFastTrackRoadTrafficAccident();
        assertThat(roadTrafficAccident).isNotNull();
        assertThat(roadTrafficAccident.getInput()).isEqualTo("Photographs and/or a plan of the accident location shall be prepared and agreed by the parties and uploaded to the " +
                "Digital Portal by 4pm on");
        assertThat(roadTrafficAccident.getDate()).isEqualTo(expectedDate);
    }
}