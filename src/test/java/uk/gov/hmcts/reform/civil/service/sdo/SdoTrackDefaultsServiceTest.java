package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoTrackDefaultsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;
    @Mock
    private SdoFeatureToggleService featureToggleService;

    private SdoTrackDefaultsService service;

    @BeforeEach
    void setUp() {
        SdoJourneyToggleService journeyToggleService = new SdoJourneyToggleService(featureToggleService);
        SdoChecklistService checklistService = new SdoChecklistService(journeyToggleService);
        SdoJudgementDeductionService judgementDeductionService = new SdoJudgementDeductionService();
        SdoDisposalOrderDefaultsService disposalOrderDefaultsService = new SdoDisposalOrderDefaultsService(
                new SdoDisposalNarrativeService(deadlineService)
        );
        SdoFastTrackSpecialistDirectionsService specialistDirectionsService =
            new SdoFastTrackSpecialistDirectionsService(deadlineService, true);
        SdoFastTrackNarrativeService fastTrackNarrativeService = new SdoFastTrackNarrativeService(deadlineService);
        SdoFastTrackOrderDefaultsService fastTrackOrderDefaultsService = new SdoFastTrackOrderDefaultsService(
                fastTrackNarrativeService,
                specialistDirectionsService
        );
        SdoSmallClaimsOrderDefaultsService smallClaimsOrderDefaultsService = new SdoSmallClaimsOrderDefaultsService(
                new SdoSmallClaimsNarrativeService(deadlineService),
                journeyToggleService
        );
        SdoExpertEvidenceFieldsService expertEvidenceFieldsService = new SdoExpertEvidenceFieldsService(deadlineService);
        SdoDisclosureOfDocumentsFieldsService disclosureOfDocumentsFieldsService = new SdoDisclosureOfDocumentsFieldsService(deadlineService);
        service = new SdoTrackDefaultsService(
                journeyToggleService,
                checklistService,
                disposalOrderDefaultsService,
                fastTrackOrderDefaultsService,
                smallClaimsOrderDefaultsService,
                expertEvidenceFieldsService,
                disclosureOfDocumentsFieldsService,
                judgementDeductionService
        );

        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.nextWorkingDayFromNowDays(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusDays(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusDays(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.orderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDateTime.class).toLocalDate());
    }

    @Test
    void shouldApplyFeatureFlagsAndPopulateTrackDefaults() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);
        when(featureToggleService.isWelshJourneyEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyBaseTrackDefaults(caseData);

        assertThat(caseData.getShowCarmFields()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getBilingualHint()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getDisposalHearingJudgesRecital()).isNotNull();
        assertThat(caseData.getSmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldApplyR2Defaults() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        service.applyR2Defaults(caseData);

        SdoR2FastTrackAltDisputeResolution disputeResolution = caseData.getSdoAltDisputeResolution();
        assertThat(disputeResolution).isNotNull();
        assertThat(disputeResolution.getIncludeInOrderToggle()).containsExactly(IncludeInOrderToggle.INCLUDE);
        assertThat(caseData.getSdoR2FastTrackUseOfWelshLanguage()).isNotNull();
        assertThat(caseData.getSdoR2DisposalHearingUseOfWelshLanguage()).isNotNull();
    }
}
