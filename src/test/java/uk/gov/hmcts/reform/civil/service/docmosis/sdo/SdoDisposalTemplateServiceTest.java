package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalDirectionsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SdoDisposalTemplateServiceTest {

    private final DocumentHearingLocationHelper locationHelper = Mockito.mock(DocumentHearingLocationHelper.class);
    private final SdoDisposalDirectionsService disposalDirectionsService = Mockito.mock(SdoDisposalDirectionsService.class);
    private final SdoCaseClassificationService classificationService = new SdoCaseClassificationService();
    private final SdoDisposalTemplateService service = new SdoDisposalTemplateService(
        locationHelper,
        classificationService,
        disposalDirectionsService
    );

    @Test
    void shouldPopulateDisposalTemplateWithTogglesAndLocations() {
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("000MC001")
            .atStateNotificationAcknowledged()
            .build();

        when(disposalDirectionsService.getFinalHearingTimeLabel(caseData)).thenReturn("4 hours");
        when(disposalDirectionsService.getTelephoneHearingLabel(caseData)).thenReturn("claimant");
        when(disposalDirectionsService.getVideoConferenceHearingLabel(caseData)).thenReturn("defendant");
        when(disposalDirectionsService.getBundleTypeText(caseData)).thenReturn("bundle type");
        when(disposalDirectionsService.hasDisposalVariable(eq(caseData), any())).thenReturn(true);

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        caseData = caseData.toBuilder()
            .disposalHearingHearingTime(
                new uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime()
                    .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                    .setOtherHours("2")
                    .setOtherMinutes("30")
            )
            .build();

        SdoDocumentFormDisposal result = service.buildTemplate(caseData, "Judge Judy", true, "token");

        assertThat(result.getJudgeName()).isEqualTo("Judge Judy");
        assertThat(result.isDisposalHearingWitnessOfFactToggle()).isTrue();
        assertThat(result.getDisposalHearingTimeEstimate()).isEqualTo("2 hours 30 minutes");
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isHasDisposalWelshToggle()).isFalse();
    }
}
