package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjTrialTemplateFieldServiceTest {

    private final DjTrialTemplateFieldService service = new DjTrialTemplateFieldService();

    @Test
    void shouldShowCreditHireDetailsWhenTogglePresent() {
        CaseData caseData = CaseData.builder()
            .sdoDJR2TrialCreditHire(
                SdoDJR2TrialCreditHire.builder()
                    .detailsShowToggle(List.of(AddOrRemoveToggle.ADD))
                    .build())
            .build();

        assertThat(service.showCreditHireDetails(caseData)).isTrue();
    }

    @Test
    void shouldNotShowCreditHireDetailsWithoutToggle() {
        CaseData caseData = CaseData.builder().build();
        assertThat(service.showCreditHireDetails(caseData)).isFalse();
    }

    @Test
    void shouldExposeDateToToggleFlag() {
        CaseData caseData = CaseData.builder()
            .trialHearingTimeDJ(TrialHearingTimeDJ.builder()
                                     .dateToToggle(List.of(DateToShowToggle.SHOW))
                                     .build())
            .build();

        assertThat(service.hasDateToToggle(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoDateToToggle() {
        CaseData caseData = CaseData.builder().build();
        assertThat(service.hasDateToToggle(caseData)).isFalse();
    }
}
