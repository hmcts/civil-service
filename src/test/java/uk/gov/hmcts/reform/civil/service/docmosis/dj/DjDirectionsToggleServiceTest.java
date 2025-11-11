package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingAddNewDirectionsDJ;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;

class DjDirectionsToggleServiceTest {

    private final DjDirectionsToggleService service = new DjDirectionsToggleService();

    @Test
    void shouldDetectToggleEnabled() {
        assertThat(service.isToggleEnabled(List.of(SHOW))).isTrue();
        assertThat(service.isToggleEnabled(null)).isFalse();
    }

    @Test
    void shouldDetectAdditionalDirections() {
        CaseData caseData = CaseData.builder()
            .disposalHearingAddNewDirectionsDJ(List.of(
                Element.<DisposalHearingAddNewDirectionsDJ>builder()
                    .value(DisposalHearingAddNewDirectionsDJ.builder().build())
                    .build()
            ))
            .build();
        assertThat(service.hasAdditionalDirections(caseData)).isTrue();
    }

    @Test
    void shouldDetectEmployerLiability() {
        assertThat(service.hasEmployerLiability(
            List.of(CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability)
        )).isTrue();
    }
}
