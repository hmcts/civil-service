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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class SdoNihlTemplateFieldServiceTest {

    private final SdoNihlTemplateFieldService service = new SdoNihlTemplateFieldService();

    @Test
    void shouldDetectNestedIncludeToggles() {
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2FastTrackAltDisputeResolution disputeResolution = new SdoR2FastTrackAltDisputeResolution();
        disputeResolution.setIncludeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoAltDisputeResolution(disputeResolution);
        SdoR2VariationOfDirections variationOfDirections = new SdoR2VariationOfDirections();
        variationOfDirections.setIncludeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoVariationOfDirections(variationOfDirections);
        SdoR2Settlement settlement = new SdoR2Settlement();
        settlement.setIncludeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2Settlement(settlement);

        assertThat(service.hasAltDisputeResolution(caseData)).isTrue();
        assertThat(service.hasVariationOfDirections(caseData)).isTrue();
        assertThat(service.hasSettlement(caseData)).isTrue();
    }

    @Test
    void shouldDetectSimpleToggleLists() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2DisclosureOfDocumentsToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorWitnessesOfFactToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorExpertEvidenceToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorAddendumReportToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorFurtherAudiogramToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorQuestionsClaimantExpertToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorPermissionToRelyOnExpertToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorEvidenceAcousticEngineerToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorQuestionsToEntExpertToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2ScheduleOfLossToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2SeparatorUploadOfDocumentsToggle(List.of(IncludeInOrderToggle.INCLUDE));
        caseData.setSdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE));

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE));
        SdoR2Trial trial = new SdoR2Trial();
        trial.setTrialOnOptions(TrialOnRadioOptions.TRIAL_WINDOW);
        caseData.setSdoR2Trial(trial);

        assertThat(service.hasTrialWindow(caseData)).isTrue();
        assertThat(service.hasTrialPhysicalBundleParty(caseData)).isFalse();
    }

    @Test
    void shouldDetectPhysicalBundleParty() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2TrialToggle(List.of(IncludeInOrderToggle.INCLUDE));
        SdoR2Trial trial = new SdoR2Trial();
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.PARTY);
        caseData.setSdoR2Trial(trial);

        assertThat(service.hasTrialPhysicalBundleParty(caseData)).isTrue();
    }

    @Test
    void shouldExposeWelshLanguageDetails() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSdoR2NihlUseOfWelshIncludeInOrderToggle(List.of(IncludeInOrderToggle.INCLUDE));
        SdoR2WelshLanguageUsage welshLanguageUsage = new SdoR2WelshLanguageUsage();
        welshLanguageUsage.setDescription("Welsh text");
        caseData.setSdoR2NihlUseOfWelshLanguage(welshLanguageUsage);

        assertThat(service.hasWelshLanguageToggle(caseData)).isTrue();
        assertThat(service.getWelshLanguageDescription(caseData)).isEqualTo("Welsh text");
    }

    @Test
    void shouldDetectNewDirectionsWhenPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2AddNewDirection newDirection = new SdoR2AddNewDirection();
        newDirection.setDirectionComment("note");
        caseData.setSdoR2AddNewDirection(wrapElements(newDirection));

        assertThat(service.hasNewDirections(caseData)).isTrue();
    }
}
