package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPPI;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjDirectionsToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsHousingDisrepair;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsPPI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistDirectionsServiceTest {

    @Mock
    private DjSpecialistNarrativeService narrativeService;

    @Mock
    private FeatureToggleService featureToggleService;

    private final DjDirectionsToggleService directionsToggleService = new DjDirectionsToggleService();

    private DjSpecialistDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjSpecialistDirectionsService(narrativeService, featureToggleService, directionsToggleService);
    }

    @Test
    void shouldPopulateSpecialistDirectionsUsingNarrativesWhenOtherRemedyDisabled() {
        TrialBuildingDispute buildingDispute = new TrialBuildingDispute();
        TrialClinicalNegligence clinicalNegligence = new TrialClinicalNegligence();
        SdoDJR2TrialCreditHire creditHire = new SdoDJR2TrialCreditHire();
        TrialPersonalInjury personalInjury = new TrialPersonalInjury();
        TrialRoadTrafficAccident rta = new TrialRoadTrafficAccident();
        TrialHousingDisrepair housingDisrepair = new TrialHousingDisrepair();

        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(false);
        when(narrativeService.buildTrialBuildingDispute()).thenReturn(buildingDispute);
        when(narrativeService.buildTrialClinicalNegligence()).thenReturn(clinicalNegligence);
        when(narrativeService.buildCreditHireDirections()).thenReturn(creditHire);
        when(narrativeService.buildTrialPersonalInjury()).thenReturn(personalInjury);
        when(narrativeService.buildTrialRoadTrafficAccident()).thenReturn(rta);
        when(narrativeService.buildTrialHousingDisrepair()).thenReturn(housingDisrepair);

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .caseManagementOrderAdditional(List.of(OrderTypeTrialAdditionalDirectionsHousingDisrepair))
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateSpecialistDirections(caseData, builder);

        CaseData result = builder.build();

        assertThat(result.getTrialBuildingDispute()).isSameAs(buildingDispute);
        assertThat(result.getTrialClinicalNegligence()).isSameAs(clinicalNegligence);
        assertThat(result.getSdoDJR2TrialCreditHire()).isSameAs(creditHire);
        assertThat(result.getTrialPersonalInjury()).isSameAs(personalInjury);
        assertThat(result.getTrialRoadTrafficAccident()).isSameAs(rta);
        assertThat(result.getTrialHousingDisrepair()).isSameAs(housingDisrepair);
        assertThat(result.getTrialPPI()).isNull();

        verify(narrativeService).buildTrialBuildingDispute();
        verify(narrativeService).buildTrialClinicalNegligence();
        verify(narrativeService).buildCreditHireDirections();
        verify(narrativeService).buildTrialPersonalInjury();
        verify(narrativeService).buildTrialRoadTrafficAccident();
        verify(narrativeService).buildTrialHousingDisrepair();
        verify(narrativeService, never()).buildTrialHousingDisrepairOtherRemedy();
        verify(narrativeService, never()).buildTrialPPI();
        verifyNoMoreInteractions(narrativeService);
    }

    @Test
    void shouldPopulateOtherRemedyHousingDisrepairAndPpiWhenToggleEnabled() {
        TrialBuildingDispute buildingDispute = new TrialBuildingDispute();
        TrialClinicalNegligence clinicalNegligence = new TrialClinicalNegligence();
        SdoDJR2TrialCreditHire creditHire = new SdoDJR2TrialCreditHire();
        TrialPersonalInjury personalInjury = new TrialPersonalInjury();
        TrialRoadTrafficAccident rta = new TrialRoadTrafficAccident();
        TrialHousingDisrepair housingOtherRemedy = new TrialHousingDisrepair();
        TrialPPI trialPpi = new TrialPPI();

        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(true);
        when(narrativeService.buildTrialBuildingDispute()).thenReturn(buildingDispute);
        when(narrativeService.buildTrialClinicalNegligence()).thenReturn(clinicalNegligence);
        when(narrativeService.buildCreditHireDirections()).thenReturn(creditHire);
        when(narrativeService.buildTrialPersonalInjury()).thenReturn(personalInjury);
        when(narrativeService.buildTrialRoadTrafficAccident()).thenReturn(rta);
        when(narrativeService.buildTrialHousingDisrepairOtherRemedy()).thenReturn(housingOtherRemedy);
        when(narrativeService.buildTrialPPI()).thenReturn(trialPpi);

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .caseManagementOrderAdditional(List.of(
                OrderTypeTrialAdditionalDirectionsPPI,
                OrderTypeTrialAdditionalDirectionsHousingDisrepair
            ))
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateSpecialistDirections(caseData, builder);

        CaseData result = builder.build();

        assertThat(result.getTrialHousingDisrepair()).isSameAs(housingOtherRemedy);
        assertThat(result.getTrialPPI()).isSameAs(trialPpi);

        verify(narrativeService).buildTrialBuildingDispute();
        verify(narrativeService).buildTrialClinicalNegligence();
        verify(narrativeService).buildCreditHireDirections();
        verify(narrativeService).buildTrialPersonalInjury();
        verify(narrativeService).buildTrialRoadTrafficAccident();
        verify(narrativeService).buildTrialHousingDisrepairOtherRemedy();
        verify(narrativeService).buildTrialPPI();
        verify(narrativeService, never()).buildTrialHousingDisrepair();
    }

    @Test
    void shouldClearPpiAndHousingWhenOtherRemedyEnabledButNotSelectedOnInitialScreen() {
        TrialBuildingDispute buildingDispute = new TrialBuildingDispute();
        TrialClinicalNegligence clinicalNegligence = new TrialClinicalNegligence();
        SdoDJR2TrialCreditHire creditHire = new SdoDJR2TrialCreditHire();
        TrialPersonalInjury personalInjury = new TrialPersonalInjury();
        TrialRoadTrafficAccident rta = new TrialRoadTrafficAccident();
        TrialHousingDisrepair housingOtherRemedy = new TrialHousingDisrepair();
        TrialPPI trialPpi = new TrialPPI();

        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(true);
        when(narrativeService.buildTrialBuildingDispute()).thenReturn(buildingDispute);
        when(narrativeService.buildTrialClinicalNegligence()).thenReturn(clinicalNegligence);
        when(narrativeService.buildCreditHireDirections()).thenReturn(creditHire);
        when(narrativeService.buildTrialPersonalInjury()).thenReturn(personalInjury);
        when(narrativeService.buildTrialRoadTrafficAccident()).thenReturn(rta);
        when(narrativeService.buildTrialHousingDisrepairOtherRemedy()).thenReturn(housingOtherRemedy);
        when(narrativeService.buildTrialPPI()).thenReturn(trialPpi);

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .caseManagementOrderAdditional(List.of())
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateSpecialistDirections(caseData, builder);

        CaseData result = builder.build();

        assertThat(result.getTrialPPI()).isNull();
        assertThat(result.getTrialHousingDisrepair()).isNull();

        verify(narrativeService).buildTrialHousingDisrepairOtherRemedy();
        verify(narrativeService).buildTrialPPI();
    }
}
