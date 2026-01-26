package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmallDrh;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2SmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SdoSmallClaimsDrhTemplateServiceTest {

    private final DocumentHearingLocationHelper locationHelper = Mockito.mock(DocumentHearingLocationHelper.class);
    private final SdoR2SmallClaimsDirectionsService r2Directions = Mockito.mock(SdoR2SmallClaimsDirectionsService.class);
    private final SdoSmallClaimsDirectionsService smallDirections = Mockito.mock(SdoSmallClaimsDirectionsService.class);
    private final SdoSmallClaimsTemplateFieldService templateFieldService = Mockito.mock(SdoSmallClaimsTemplateFieldService.class);
    private final FeatureToggleService featureToggleService = Mockito.mock(FeatureToggleService.class);
    private final SdoCaseClassificationService classificationService = new SdoCaseClassificationService();
    private final SdoSmallClaimsDrhTemplateService service = new SdoSmallClaimsDrhTemplateService(
        locationHelper,
        classificationService,
        r2Directions,
        smallDirections,
        featureToggleService,
        templateFieldService
    );

    @Test
    void shouldPopulateDrhTemplateWithFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("000MC001")
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .sdoR2SmallClaimsHearing(uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing.builder().build())
            .build();

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        when(r2Directions.hasHearingTrialWindow(caseData)).thenReturn(true);
        when(r2Directions.getPhysicalTrialBundleText(caseData)).thenReturn("bundle");
        when(r2Directions.getHearingMethod(caseData)).thenReturn("in person");
        when(r2Directions.getHearingTime(caseData)).thenReturn("2 hours");
        DynamicListElement locationElement = new DynamicListElement();
        locationElement.setLabel("Court A");
        locationElement.setCode("123");
        DynamicList locationList = new DynamicList();
        locationList.setValue(locationElement);
        locationList.setListItems(java.util.List.of(locationElement));
        when(r2Directions.getHearingLocation(caseData)).thenReturn(locationList);
        when(templateFieldService.getMediationTextDrh(caseData)).thenReturn("mediation text");
        when(templateFieldService.showMediationSectionDrh(caseData, true)).thenReturn(true);

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        SdoDocumentFormSmallDrh result = service.buildTemplate(caseData, "Judge Judy", true, "token");

        assertThat(result.getJudgeName()).isEqualTo("Judge Judy");
        assertThat(result.isHasSdoR2HearingTrialWindow()).isTrue();
        assertThat(result.getSmallClaimsMethod()).isEqualTo("in person");
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isSdoR2SmallClaimsMediationSectionToggle()).isTrue();
    }
}
