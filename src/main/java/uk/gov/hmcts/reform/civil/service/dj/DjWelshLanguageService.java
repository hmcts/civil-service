package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_WITH_ARTICLE;

@Service
public class DjWelshLanguageService {

    private static final DateTimeFormatter DEADLINE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    public SdoR2WelshLanguageUsage buildWelshUsage() {
        return SdoR2WelshLanguageUsage.builder()
            .description(WELSH_LANG_DESCRIPTION)
            .build();
    }

    public String buildOrderMadeWithoutHearingText(LocalDate deadlineDate) {
        return String.format(
            "%s %s.",
            ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_WITH_ARTICLE,
            deadlineDate.format(DEADLINE_FORMATTER)
        );
    }
}
