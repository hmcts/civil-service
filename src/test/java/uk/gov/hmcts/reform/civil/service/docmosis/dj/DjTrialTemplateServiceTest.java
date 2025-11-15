package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.dj.DjBuildingDisputeDirectionsService;
import uk.gov.hmcts.reform.civil.service.dj.DjClinicalDirectionsService;
import uk.gov.hmcts.reform.civil.service.dj.DjDeadlineService;
import uk.gov.hmcts.reform.civil.service.dj.DjTrialNarrativeService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_LATE_WARNING;

@ExtendWith(MockitoExtension.class)
class DjTrialTemplateServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DocumentHearingLocationHelper locationHelper;

    private DjTrialTemplateService service;
    private DjAuthorisationFieldService authorisationFieldService;
    private DjBundleFieldService bundleFieldService;
    private DjDirectionsToggleService directionsToggleService;
    private DjPartyFieldService partyFieldService;
    private DjHearingMethodFieldService hearingMethodFieldService;
    private DjTrialTemplateFieldService trialTemplateFieldService;

    @BeforeEach
    void setUp() {
        authorisationFieldService = new DjAuthorisationFieldService();
        bundleFieldService = new DjBundleFieldService();
        directionsToggleService = new DjDirectionsToggleService();
        partyFieldService = new DjPartyFieldService();
        hearingMethodFieldService = new DjHearingMethodFieldService();
        trialTemplateFieldService = new DjTrialTemplateFieldService();
        service = new DjTrialTemplateService(
            userService,
            locationHelper,
            authorisationFieldService,
            bundleFieldService,
            directionsToggleService,
            partyFieldService,
            hearingMethodFieldService,
            trialTemplateFieldService
        );

        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder()
            .forename("Judge")
            .surname("Dredd")
            .roles(Collections.singletonList("judge"))
            .build());
    }

    @Test
    void shouldPopulateTrialTemplate() {
        CaseData baseCase = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        DjDeadlineService witnessDeadlineService = Mockito.mock(DjDeadlineService.class);
        when(witnessDeadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 2, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        DjTrialNarrativeService trialNarrativeService = new DjTrialNarrativeService(witnessDeadlineService);
        CaseData caseData = baseCase.toBuilder()
            .trialHearingWitnessOfFactDJ(trialNarrativeService.buildWitnessOfFact())
            .build();

        LocationRefData location = LocationRefData.builder()
            .epimmsId("321")
            .siteName("Court B")
            .build();
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        DefaultJudgmentSDOOrderForm result = service.buildTemplate(caseData, "token");

        assertThat(result.getCaseNumber()).isEqualTo(caseData.getLegacyCaseReference());
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isHasTrialHearingWelshSectionDJ()).isFalse();
        assertThat(result.getTrialHearingWitnessOfFactDJ().getInput8())
            .isEqualTo(SMALL_CLAIMS_WITNESS_DEADLINE);
        assertThat(result.getTrialHearingWitnessOfFactDJ().getInput9())
            .isEqualTo(SMALL_CLAIMS_WITNESS_LATE_WARNING);
    }

    @Test
    void shouldCarrySharedSpecialistTextIntoTrialTemplate() {
        CaseData baseCase = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseData caseData = trialCaseWithSpecialistSections(baseCase);

        LocationRefData location = LocationRefData.builder()
            .epimmsId("321")
            .siteName("Court B")
            .build();
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        DefaultJudgmentSDOOrderForm result = service.buildTemplate(caseData, "token");

        assertThat(result.getTrialBuildingDispute())
            .extracting(TrialBuildingDispute::getInput1, TrialBuildingDispute::getInput2,
                TrialBuildingDispute::getInput3, TrialBuildingDispute::getInput4)
            .containsExactly(
                BUILDING_SCHEDULE_INTRO_DJ,
                BUILDING_SCHEDULE_COLUMNS_DJ,
                BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION,
                BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION
            );
        assertThat(result.getTrialClinicalNegligence())
            .extracting(TrialClinicalNegligence::getInput1, TrialClinicalNegligence::getInput2,
                TrialClinicalNegligence::getInput3, TrialClinicalNegligence::getInput4)
            .containsExactly(
                CLINICAL_DOCUMENTS_HEADING,
                CLINICAL_PARTIES_DJ,
                CLINICAL_NOTES_DJ,
                CLINICAL_BUNDLE_DJ
            );
    }

    private CaseData trialCaseWithSpecialistSections(CaseData baseCase) {
        DjDeadlineService deadlineService = Mockito.mock(DjDeadlineService.class);
        when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 2, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));

        DjBuildingDisputeDirectionsService buildingService = new DjBuildingDisputeDirectionsService(deadlineService);
        DjClinicalDirectionsService clinicalService = new DjClinicalDirectionsService(deadlineService);

        return baseCase.toBuilder()
            .trialBuildingDispute(buildingService.buildTrialBuildingDispute())
            .trialHousingDisrepair(buildingService.buildTrialHousingDisrepair())
            .trialClinicalNegligence(clinicalService.buildTrialClinicalNegligence())
            .build();
    }
}
