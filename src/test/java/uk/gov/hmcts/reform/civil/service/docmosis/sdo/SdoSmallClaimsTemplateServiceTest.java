package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SmallClaimsVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SdoSmallClaimsTemplateServiceTest {

    private final DocumentHearingLocationHelper locationHelper = Mockito.mock(DocumentHearingLocationHelper.class);
    private final SdoSmallClaimsDirectionsService directionsService = Mockito.mock(SdoSmallClaimsDirectionsService.class);
    private final SdoSmallClaimsTemplateFieldService templateFieldService = Mockito.mock(SdoSmallClaimsTemplateFieldService.class);
    private final FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
    private final SdoCaseClassificationService classificationService = new SdoCaseClassificationService();
    private final SdoSmallClaimsTemplateService service = new SdoSmallClaimsTemplateService(
        locationHelper,
        classificationService,
        directionsService,
        featureToggleService,
        templateFieldService,
        true
    );

    @Test
    void shouldPopulateSmallClaimsTemplateWithDirectionFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("000MC001")
            .atStateNotificationAcknowledged()
            .build();

        when(directionsService.hasSmallAdditionalDirections(eq(caseData), any(SmallTrack.class))).thenReturn(true);
        when(directionsService.hasSmallClaimsVariable(eq(caseData), any(SmallClaimsVariable.class))).thenReturn(true);
        when(templateFieldService.getHearingTimeLabel(caseData)).thenReturn("2 hours");
        when(templateFieldService.getMethodTelephoneHearingLabel(caseData)).thenReturn("claimant");
        when(templateFieldService.getMethodVideoConferenceHearingLabel(caseData)).thenReturn("defendant");
        when(templateFieldService.getMediationText(caseData)).thenReturn("mediation");
        when(templateFieldService.showMediationSection(caseData, true)).thenReturn(true);

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        SdoDocumentFormSmall result = service.buildTemplate(caseData, "Judge Judy", true, "token");

        assertThat(result.getJudgeName()).isEqualTo("Judge Judy");
        assertThat(result.isSmallClaimsMediationSectionToggle()).isTrue();
        assertThat(result.getSmallClaimsHearingTime()).isEqualTo("2 hours");
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isSmallClaimsWelshLanguageToggle()).isTrue();
    }
}
