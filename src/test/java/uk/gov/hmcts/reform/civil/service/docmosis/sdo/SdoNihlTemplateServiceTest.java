package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFastNihl;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNihlTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2TrialTemplateFieldService;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SdoNihlTemplateServiceTest {

    private final DocumentHearingLocationHelper locationHelper = Mockito.mock(DocumentHearingLocationHelper.class);
    private final SdoR2TrialTemplateFieldService trialTemplateFieldService = Mockito.mock(
        SdoR2TrialTemplateFieldService.class);
    private final SdoNihlTemplateFieldService nihlTemplateFieldService = Mockito.mock(SdoNihlTemplateFieldService.class);
    private final SdoCaseClassificationService classificationService = new SdoCaseClassificationService();
    private final SdoNihlTemplateService service = new SdoNihlTemplateService(
        locationHelper,
        classificationService,
        trialTemplateFieldService,
        nihlTemplateFieldService
    );

    @Test
    void shouldPopulateNihlTemplateWithDirections() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("000MC001")
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .sdoAltDisputeResolution(buildAltDisputeResolution())
            .sdoVariationOfDirections(buildVariationOfDirections())
            .sdoR2Settlement(buildSettlement())
            .sdoR2Trial(buildTrial())
            .build();

        when(trialTemplateFieldService.hasRestrictWitness(caseData)).thenReturn(true);
        when(trialTemplateFieldService.hasRestrictPages(caseData)).thenReturn(true);
        when(trialTemplateFieldService.hasApplicationToRelyOnFurther(caseData)).thenReturn(true);
        when(trialTemplateFieldService.hasClaimForPecuniaryLoss(caseData)).thenReturn(true);
        when(trialTemplateFieldService.getTrialHearingTimeAllocated(caseData)).thenReturn("2 hours");
        when(trialTemplateFieldService.getTrialMethodOfHearing(caseData)).thenReturn("In person");
        when(trialTemplateFieldService.getPhysicalBundlePartyText(caseData)).thenReturn("party text");

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        SdoDocumentFormFastNihl result = service.buildTemplate(caseData, "Judge Judy", true, "token");

        assertThat(result.getJudgeName()).isEqualTo("Judge Judy");
        assertThat(result.isHasRestrictPages()).isTrue();
        assertThat(result.getSdoTrialHearingTimeAllocated()).isEqualTo("2 hours");
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.getPhysicalBundlePartyTxt()).isEqualTo("party text");
    }

    private SdoR2FastTrackAltDisputeResolution buildAltDisputeResolution() {
        SdoR2FastTrackAltDisputeResolution altDisputeResolution = new SdoR2FastTrackAltDisputeResolution();
        altDisputeResolution.setIncludeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE));
        return altDisputeResolution;
    }

    private SdoR2VariationOfDirections buildVariationOfDirections() {
        SdoR2VariationOfDirections variation = new SdoR2VariationOfDirections();
        variation.setIncludeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE));
        return variation;
    }

    private SdoR2Settlement buildSettlement() {
        SdoR2Settlement settlement = new SdoR2Settlement();
        settlement.setIncludeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE));
        return settlement;
    }

    private SdoR2Trial buildTrial() {
        SdoR2Trial trial = new SdoR2Trial();
        trial.setTrialOnOptions(TrialOnRadioOptions.TRIAL_WINDOW);
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.PARTY);
        return trial;
    }
}
