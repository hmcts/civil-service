package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPPI;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DjPpiDirectionsServiceTest {

    @Test
    void shouldBuildTrialPpiWithCalendarDayDeadline() {
        DjPpiDirectionsService service = new DjPpiDirectionsService();

        TrialPPI ppi = service.buildTrialPPI();

        assertThat(ppi.getPpiDate()).isEqualTo(LocalDate.now().plusDays(28));
        assertThat(ppi.getText()).isEqualTo(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
    }
}

