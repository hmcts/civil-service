package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjHearingMethodFieldServiceTest {

    private final DjHearingMethodFieldService service = new DjHearingMethodFieldService();

    @Test
    void shouldResolveTelephoneFromDisposal() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .disposalHearingMethodTelephoneHearingDJ(HearingMethodTelephoneHearingDJ.telephoneTheCourt)
            .build();

        assertThat(service.resolveTelephoneOrganisedBy(caseData)).isEqualTo("the court");
    }

    @Test
    void shouldFallbackToTrialTelephone() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .trialHearingMethodTelephoneHearingDJ(HearingMethodTelephoneHearingDJ.telephoneTheDefendant)
            .build();

        assertThat(service.resolveTelephoneOrganisedBy(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldResolveVideoFromDisposal() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .disposalHearingMethodVideoConferenceHearingDJ(HearingMethodVideoConferenceDJ.videoTheCourt)
            .build();

        assertThat(service.resolveVideoOrganisedBy(caseData)).isEqualTo("the court");
    }

    @Test
    void shouldFallbackToTrialVideo() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .trialHearingMethodVideoConferenceHearingDJ(HearingMethodVideoConferenceDJ.videoTheDefendant)
            .build();

        assertThat(service.resolveVideoOrganisedBy(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldDetectInPerson() {
        assertThat(service.isInPerson(DisposalHearingMethodDJ.disposalHearingMethodInPerson)).isTrue();
        assertThat(service.isInPerson(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing)).isFalse();
    }
}
