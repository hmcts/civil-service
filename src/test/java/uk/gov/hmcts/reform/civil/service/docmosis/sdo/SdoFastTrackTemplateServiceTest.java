package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.FastTrackVariable;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackTemplateFieldService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SdoFastTrackTemplateServiceTest {

    private final DocumentHearingLocationHelper locationHelper = Mockito.mock(DocumentHearingLocationHelper.class);
    private final SdoFastTrackDirectionsService fastTrackDirectionsService = Mockito.mock(SdoFastTrackDirectionsService.class);
    private final SdoCaseClassificationService classificationService = new SdoCaseClassificationService();
    private final SdoFastTrackTemplateFieldService templateFieldService = Mockito.mock(SdoFastTrackTemplateFieldService.class);
    private final SdoFastTrackTemplateService service = new SdoFastTrackTemplateService(
        locationHelper,
        classificationService,
        fastTrackDirectionsService,
        templateFieldService
    );

    @Test
    void shouldPopulateFastTrackTemplateWithDirectionFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("000MC001")
            .atStateNotificationAcknowledged()
            .build();

        when(fastTrackDirectionsService.hasFastAdditionalDirections(eq(caseData), any(FastTrack.class))).thenReturn(true);
        when(fastTrackDirectionsService.hasFastTrackVariable(eq(caseData), any(FastTrackVariable.class))).thenReturn(true);
        when(templateFieldService.getMethodTelephoneHearingLabel(caseData)).thenReturn("claimant");
        when(templateFieldService.getMethodVideoConferenceHearingLabel(caseData)).thenReturn("defendant");
        when(templateFieldService.getAllocationSummary(caseData)).thenReturn("allocation text");
        when(templateFieldService.getHearingTimeLabel(caseData)).thenReturn("4 hours");
        when(templateFieldService.getTrialBundleTypeText(caseData)).thenReturn("bundle text");

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        SdoDocumentFormFast result = service.buildTemplate(caseData, "Judge Judy", true, "token");

        assertThat(result.getJudgeName()).isEqualTo("Judge Judy");
        assertThat(result.isWrittenByJudge()).isTrue();
        assertThat(result.isFastTrackWelshLanguageToggle()).isTrue();
        assertThat(result.getFastTrackAllocation()).isEqualTo("allocation text");
        assertThat(result.getHearingLocation()).isEqualTo(location);
    }
}
