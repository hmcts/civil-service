package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION;

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
            "This order has been made without a hearing. "
                + "Each party has the right to apply to have this Order "
                + "set aside or varied. Any such application must be received by the Court "
                + "(together with the appropriate fee) by 4pm on %s.",
            deadlineDate.format(DEADLINE_FORMATTER)
        );
    }
}
