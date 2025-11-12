package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SdoSmallClaimsTemplateFieldServiceTest {

    private final SdoMediationSectionService mediationSectionService = new SdoMediationSectionService();
    private final SdoSmallClaimsTemplateFieldService service =
        new SdoSmallClaimsTemplateFieldService(mediationSectionService);

    @Test
    void shouldFormatHearingTimeLabels() {
        CaseData otherEstimate = CaseData.builder()
            .smallClaimsHearing(SmallClaimsHearing.builder()
                                    .time(SmallClaimsTimeEstimate.OTHER)
                                    .otherHours(BigDecimal.valueOf(2))
                                    .otherMinutes(BigDecimal.valueOf(45))
                                    .build())
            .build();

        CaseData standardEstimate = CaseData.builder()
            .smallClaimsHearing(SmallClaimsHearing.builder()
                                    .time(SmallClaimsTimeEstimate.THREE_HOURS)
                                    .build())
            .build();

        assertThat(service.getHearingTimeLabel(otherEstimate)).isEqualTo("2 hours 45 minutes");
        assertThat(service.getHearingTimeLabel(standardEstimate)).isEqualTo("three hours");
    }

    @Test
    void shouldReturnMethodLabels() {
        CaseData caseData = CaseData.builder()
            .smallClaimsMethodTelephoneHearing(SmallClaimsMethodTelephoneHearing.telephoneTheClaimant)
            .smallClaimsMethodVideoConferenceHearing(SmallClaimsMethodVideoConferenceHearing.videoTheDefendant)
            .build();

        assertThat(service.getMethodTelephoneHearingLabel(caseData)).isEqualTo("the claimant");
        assertThat(service.getMethodVideoConferenceHearingLabel(caseData)).isEqualTo("the defendant");
    }

    @Test
    void shouldHandleMediationSections() {
        SmallClaimsMediation standardMediation = SmallClaimsMediation.builder()
            .input("Schedule ADR")
            .build();
        SdoR2SmallClaimsMediation drhMediation = SdoR2SmallClaimsMediation.builder()
            .input("Schedule ADR")
            .build();

        CaseData caseData = CaseData.builder()
            .smallClaimsMediationSectionStatement(standardMediation)
            .sdoR2SmallClaimsMediationSectionStatement(drhMediation)
            .build();

        assertThat(service.getMediationText(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showMediationSection(caseData, true)).isTrue();
        assertThat(service.showMediationSection(caseData, false)).isFalse();

        assertThat(service.getMediationTextDrh(caseData)).isEqualTo("Schedule ADR");
        assertThat(service.showMediationSectionDrh(caseData, true)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMediationMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.showMediationSection(caseData, true)).isFalse();
        assertThat(service.showMediationSectionDrh(caseData, true)).isFalse();
        assertThat(service.getMediationText(caseData)).isNull();
        assertThat(service.getMediationTextDrh(caseData)).isNull();
    }
}
