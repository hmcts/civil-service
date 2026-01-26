package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjTrialTemplateFieldServiceTest {

    private final DjTrialTemplateFieldService service = new DjTrialTemplateFieldService();

    @Test
    void shouldShowCreditHireDetailsWhenTogglePresent() {
        SdoDJR2TrialCreditHire creditHire = new SdoDJR2TrialCreditHire();
        creditHire.setDetailsShowToggle(List.of(AddOrRemoveToggle.ADD));
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .sdoDJR2TrialCreditHire(creditHire)
            .build();

        assertThat(service.showCreditHireDetails(caseData)).isTrue();
    }

    @Test
    void shouldNotShowCreditHireDetailsWithoutToggle() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(service.showCreditHireDetails(caseData)).isFalse();
    }

    @Test
    void shouldExposeDateToToggleFlag() {
        TrialHearingTimeDJ hearingTime = new TrialHearingTimeDJ();
        hearingTime.setDateToToggle(List.of(DateToShowToggle.SHOW));
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .trialHearingTimeDJ(hearingTime)
            .build();

        assertThat(service.hasDateToToggle(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoDateToToggle() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(service.hasDateToToggle(caseData)).isFalse();
    }
}
