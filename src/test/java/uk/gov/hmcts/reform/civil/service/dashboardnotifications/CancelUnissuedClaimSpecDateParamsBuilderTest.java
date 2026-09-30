package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class CancelUnissuedClaimSpecDateParamsBuilderTest {

    private final CancelUnissuedClaimSpecDateParamsBuilder builder = new CancelUnissuedClaimSpecDateParamsBuilder();

    @Test
    void shouldAddEnglishAndWelshDatesWhenCancelledDateIsPresent() {
        CaseData caseData = CaseData.builder().cancelUnissuedClaimSpecDate(LocalDate.of(2026, 7, 3)).build();
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params)
            .containsEntry("cancelUnissuedClaimSpecDateEn", "3 July 2026")
            .containsEntry("cancelUnissuedClaimSpecDateCy", "3 Gorffennaf 2026");
    }

    @Test
    void shouldNotAddParamsWhenCancelledDateIsNull() {
        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(CaseData.builder().build(), params);

        assertThat(params).isEmpty();
    }
}
