package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistDirectionsServiceTest {

    @Mock
    private DjSpecialistNarrativeService narrativeService;

    @InjectMocks
    private DjSpecialistDirectionsService service;

    @Test
    void shouldPopulateSpecialistDirectionsUsingNarratives() {
        TrialBuildingDispute buildingDispute = new TrialBuildingDispute();
        TrialClinicalNegligence clinicalNegligence = new TrialClinicalNegligence();
        SdoDJR2TrialCreditHire creditHire = new SdoDJR2TrialCreditHire();
        TrialPersonalInjury personalInjury = new TrialPersonalInjury();
        TrialRoadTrafficAccident rta = new TrialRoadTrafficAccident();
        TrialHousingDisrepair housingDisrepair = new TrialHousingDisrepair();

        when(narrativeService.buildTrialBuildingDispute()).thenReturn(buildingDispute);
        when(narrativeService.buildTrialClinicalNegligence()).thenReturn(clinicalNegligence);
        when(narrativeService.buildCreditHireDirections()).thenReturn(creditHire);
        when(narrativeService.buildTrialPersonalInjury()).thenReturn(personalInjury);
        when(narrativeService.buildTrialRoadTrafficAccident()).thenReturn(rta);
        when(narrativeService.buildTrialHousingDisrepair()).thenReturn(housingDisrepair);

        CaseData caseData = CaseDataBuilder.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateSpecialistDirections(builder);

        CaseData result = builder.build();

        assertThat(result.getTrialBuildingDispute()).isSameAs(buildingDispute);
        assertThat(result.getTrialClinicalNegligence()).isSameAs(clinicalNegligence);
        assertThat(result.getSdoDJR2TrialCreditHire()).isSameAs(creditHire);
        assertThat(result.getTrialPersonalInjury()).isSameAs(personalInjury);
        assertThat(result.getTrialRoadTrafficAccident()).isSameAs(rta);
        assertThat(result.getTrialHousingDisrepair()).isSameAs(housingDisrepair);

        verify(narrativeService).buildTrialBuildingDispute();
        verify(narrativeService).buildTrialClinicalNegligence();
        verify(narrativeService).buildCreditHireDirections();
        verify(narrativeService).buildTrialPersonalInjury();
        verify(narrativeService).buildTrialRoadTrafficAccident();
        verify(narrativeService).buildTrialHousingDisrepair();
    }
}
