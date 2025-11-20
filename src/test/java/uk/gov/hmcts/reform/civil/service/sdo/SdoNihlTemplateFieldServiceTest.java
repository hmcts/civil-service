package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class SdoNihlTemplateFieldServiceTest {

    private final SdoNihlTemplateFieldService service = new SdoNihlTemplateFieldService();

    @Test
    void shouldDetectNestedIncludeToggles() {
        CaseData caseData = CaseData.builder()
            .sdoAltDisputeResolution(SdoR2FastTrackAltDisputeResolution.builder()
                                         .includeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE)).build())
            .sdoVariationOfDirections(SdoR2VariationOfDirections.builder()
                                          .includeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE)).build())
            .sdoR2Settlement(SdoR2Settlement.builder()
                                  .includeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE)).build())
            .build();

        assertThat(service.hasAltDisputeResolution(caseData)).isTrue();
        assertThat(service.hasVariationOfDirections(caseData)).isTrue();
        assertThat(service.hasSettlement(caseData)).isTrue();
    }

    @Test
    void shouldDetectSimpleToggleLists() {
        CaseData caseData = CaseData.builder()
            .sdoR2DisclosureOfDocumentsToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorWitnessesOfFactToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorExpertEvidenceToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorAddendumReportToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorFurtherAudiogramToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorQuestionsClaimantExpertToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorPermissionToRelyOnExpertToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorEvidenceAcousticEngineerToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorQuestionsToEntExpertToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2ScheduleOfLossToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2SeparatorUploadOfDocumentsToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .build();

        assertThat(service.hasDisclosureOfDocuments(caseData)).isTrue();
        assertThat(service.hasWitnessOfFact(caseData)).isTrue();
        assertThat(service.hasExpertEvidence(caseData)).isTrue();
        assertThat(service.hasAddendumReport(caseData)).isTrue();
        assertThat(service.hasFurtherAudiogram(caseData)).isTrue();
        assertThat(service.hasQuestionsOfClaimantExpert(caseData)).isTrue();
        assertThat(service.hasPermissionFromEntExpert(caseData)).isTrue();
        assertThat(service.hasEvidenceFromAcousticEngineer(caseData)).isTrue();
        assertThat(service.hasQuestionsToEntAfterReport(caseData)).isTrue();
        assertThat(service.hasScheduleOfLoss(caseData)).isTrue();
        assertThat(service.hasUploadDocuments(caseData)).isTrue();
        assertThat(service.hasTrial(caseData)).isTrue();
    }

    @Test
    void shouldDetectTrialWindowOnlyWhenToggleAndOptionPresent() {
        CaseData caseData = CaseData.builder()
            .sdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2Trial(SdoR2Trial.builder()
                            .trialOnOptions(TrialOnRadioOptions.TRIAL_WINDOW)
                            .build())
            .build();

        assertThat(service.hasTrialWindow(caseData)).isTrue();
        assertThat(service.hasTrialPhysicalBundleParty(caseData)).isFalse();
    }

    @Test
    void shouldDetectPhysicalBundleParty() {
        CaseData caseData = CaseData.builder()
            .sdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2Trial(SdoR2Trial.builder()
                            .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                            .build())
            .build();

        assertThat(service.hasTrialPhysicalBundleParty(caseData)).isTrue();
    }

    @Test
    void shouldExposeWelshLanguageDetails() {
        CaseData caseData = CaseData.builder()
            .sdoR2NihlUseOfWelshIncludeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE))
            .sdoR2NihlUseOfWelshLanguage(
                SdoR2WelshLanguageUsage.builder().description("Welsh text").build()
            )
            .build();

        assertThat(service.hasWelshLanguageToggle(caseData)).isTrue();
        assertThat(service.getWelshLanguageDescription(caseData)).isEqualTo("Welsh text");
    }

    @Test
    void shouldDetectNewDirectionsWhenPresent() {
        CaseData caseData = CaseData.builder()
            .sdoR2AddNewDirection(wrapElements(SdoR2AddNewDirection.builder().directionComment("note").build()))
            .build();

        assertThat(service.hasNewDirections(caseData)).isTrue();
    }
}
