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
            .sdoAltDisputeResolution(SdoR2FastTrackAltDisputeResolution.builder()
                .includeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE)).build())
            .sdoVariationOfDirections(SdoR2VariationOfDirections.builder()
                .includeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE)).build())
            .sdoR2Settlement(SdoR2Settlement.builder()
                .includeInOrderToggle(java.util.List.of(IncludeInOrderToggle.INCLUDE)).build())
            .sdoR2Trial(SdoR2Trial.builder()
                .trialOnOptions(TrialOnRadioOptions.TRIAL_WINDOW)
                .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                .build())
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
}
