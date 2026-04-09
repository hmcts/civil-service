package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjDisposalTemplateFieldServiceTest {

    private final DjDisposalTemplateFieldService service = new DjDisposalTemplateFieldService();

    @Test
    void shouldResolveCourtLocationLabel() {
        DynamicListElement element = new DynamicListElement();
        element.setLabel("Central Court");
        DynamicList list = new DynamicList();
        list.setValue(element);
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .disposalHearingMethodInPersonDJ(list)
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
        DisposalHearingFinalDisposalHearingDJ hearing = new DisposalHearingFinalDisposalHearingDJ();
        hearing.setTime(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES);
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .disposalHearingFinalDisposalHearingDJ(hearing)
            .build();

        assertThat(service.getHearingDuration(caseData)).isEqualTo("30 minutes");
    }

    @Test
    void shouldReturnNullWhenNoDuration() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(service.getHearingDuration(caseData)).isNull();
    }
}
