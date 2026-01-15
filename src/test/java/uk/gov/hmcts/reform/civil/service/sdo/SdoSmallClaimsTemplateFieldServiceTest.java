package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SdoSmallClaimsTemplateFieldServiceTest {

    private final SdoMediationSectionService mediationSectionService = new SdoMediationSectionService();
    private final SdoSmallClaimsTemplateFieldService service =
        new SdoSmallClaimsTemplateFieldService(mediationSectionService);

    @Test
    void shouldFormatHearingTimeLabels() {
        SmallClaimsHearing otherHearing = new SmallClaimsHearing();
        otherHearing.setTime(SmallClaimsTimeEstimate.OTHER);
        otherHearing.setOtherHours(BigDecimal.valueOf(2));
        otherHearing.setOtherMinutes(BigDecimal.valueOf(45));
        CaseData otherEstimate = CaseDataBuilder.builder().build();
        otherEstimate.setSmallClaimsHearing(otherHearing);

        CaseData standardEstimate = CaseDataBuilder.builder().build();
        SmallClaimsHearing standardHearing = new SmallClaimsHearing();
        standardHearing.setTime(SmallClaimsTimeEstimate.THREE_HOURS);
        standardEstimate.setSmallClaimsHearing(standardHearing);

        assertThat(service.getHearingTimeLabel(otherEstimate)).isEqualTo("2 hours 45 minutes");
        assertThat(service.getHearingTimeLabel(standardEstimate)).isEqualTo("three hours");
    }

    @Test
    void shouldReturnMethodLabels() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSmallClaimsMethodTelephoneHearing(SmallClaimsMethodTelephoneHearing.telephoneTheClaimant);
        caseData.setSmallClaimsMethodVideoConferenceHearing(SmallClaimsMethodVideoConferenceHearing.videoTheDefendant);

        assertThat(service.getMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldHandleMediationSections() {
        SmallClaimsMediation standardMediation = new SmallClaimsMediation();
        standardMediation.setInput("Schedule ADR");
        SdoR2SmallClaimsMediation drhMediation = new SdoR2SmallClaimsMediation();
        drhMediation.setInput("Schedule ADR");

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSmallClaimsMediationSectionStatement(standardMediation);
        caseData.setSdoR2SmallClaimsMediationSectionStatement(drhMediation);

        assertThat(service.getMediationText(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showMediationSection(caseData, true)).isTrue();
        assertThat(service.showMediationSection(caseData, false)).isFalse();

        assertThat(service.getMediationTextDrh(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showMediationSectionDrh(caseData, true)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMediationMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(service.showMediationSection(caseData, true)).isFalse();
        assertThat(service.showMediationSectionDrh(caseData, true)).isFalse();
        assertThat(service.getMediationText(caseData)).isNull();
        assertThat(service.getMediationTextDrh(caseData)).isNull();
    }
}
