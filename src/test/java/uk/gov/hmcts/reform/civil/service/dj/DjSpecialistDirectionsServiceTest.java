package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjSpecialistDirectionsServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private DjSpecialistDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjSpecialistDirectionsService(workingDayIndicator);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDate.class));
    }

    @Test
    void shouldPopulateSpecialistDirections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateSpecialistDirections(builder);

        CaseData result = builder.build();

        TrialBuildingDispute buildingDispute = result.getTrialBuildingDispute();
        assertThat(buildingDispute).isNotNull();
        assertThat(buildingDispute.getDate1()).isEqualTo(LocalDate.now().plusWeeks(10));

        TrialClinicalNegligence clinicalNegligence = result.getTrialClinicalNegligence();
        assertThat(clinicalNegligence).isNotNull();
        assertThat(clinicalNegligence.getInput2()).contains("electronically stored documents");

        SdoDJR2TrialCreditHire creditHire = result.getSdoDJR2TrialCreditHire();
        assertThat(creditHire.getDetailsShowToggle()).isEqualTo(List.of(AddOrRemoveToggle.ADD));

        TrialPersonalInjury personalInjury = result.getTrialPersonalInjury();
        assertThat(personalInjury.getDate2()).isEqualTo(LocalDate.now().plusWeeks(8));

        TrialRoadTrafficAccident rta = result.getTrialRoadTrafficAccident();
        assertThat(rta.getDate1()).isEqualTo(LocalDate.now().plusWeeks(4));

        TrialHousingDisrepair housingDisrepair = result.getTrialHousingDisrepair();
        assertThat(housingDisrepair.getDate2()).isEqualTo(LocalDate.now().plusWeeks(12));
    }
}
