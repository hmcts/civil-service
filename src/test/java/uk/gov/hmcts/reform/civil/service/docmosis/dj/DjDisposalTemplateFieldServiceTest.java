package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;

import static org.assertj.core.api.Assertions.assertThat;

class DjDisposalTemplateFieldServiceTest {

    private final DjDisposalTemplateFieldService service = new DjDisposalTemplateFieldService();

    @Test
    void shouldResolveCourtLocationLabel() {
        CaseData caseData = CaseData.builder()
            .disposalHearingMethodInPersonDJ(
                DynamicList.builder()
                    .value(DynamicListElement.builder().label("Central Court").build())
                    .build())
            .build();

        assertThat(service.getCourtLocation(caseData)).isEqualTo("Central Court");
    }

    @Test
    void shouldRenderAttendanceLabels() {
        assertThat(service.getAttendanceLabel(DisposalHearingMethodDJ.disposalHearingMethodInPerson))
            .isEqualTo("in person");
        assertThat(service.getAttendanceLabel(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing))
            .isEqualTo("by telephone");
        assertThat(service.getAttendanceLabel(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing))
            .isEqualTo("by video conference");
    }

    @Test
    void shouldReturnHearingDurationFromCaseData() {
        CaseData caseData = CaseData.builder()
            .disposalHearingFinalDisposalHearingDJ(
                DisposalHearingFinalDisposalHearingDJ.builder()
                    .time(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES)
                    .build())
            .build();

        assertThat(service.getHearingDuration(caseData)).isEqualTo("30 minutes");
    }

    @Test
    void shouldReturnNullWhenNoDuration() {
        CaseData caseData = CaseData.builder().build();
        assertThat(service.getHearingDuration(caseData)).isNull();
    }
}
