package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_WITH_ARTICLE;

class DjWelshLanguageServiceTest {

    private final DjWelshLanguageService service = new DjWelshLanguageService();

    @Test
    void shouldBuildWelshUsage() {
        SdoR2WelshLanguageUsage usage = service.buildWelshUsage();

        assertThat(usage.getDescription()).isEqualTo(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
    }

    @Test
    void shouldBuildOrderMadeWithoutHearingText() {
        LocalDate deadline = LocalDate.of(2025, 1, 15);

        String result = service.buildOrderMadeWithoutHearingText(deadline);

        String formattedDate = deadline.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH));
        assertThat(result).isEqualTo(String.format(
            "%s %s.",
            ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_WITH_ARTICLE,
            formattedDate
        ));
    }
}
