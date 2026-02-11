package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_A;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_B;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_D;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_E;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO;

@ExtendWith(MockitoExtension.class)
class SdoFastTrackSpecialistDirectionsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoFastTrackSpecialistDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new SdoFastTrackSpecialistDirectionsService(deadlineService, true);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateAllSpecialistSections() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateSpecialistDirections(caseData);

        FastTrackBuildingDispute buildingDispute = caseData.getFastTrackBuildingDispute();
        assertThat(buildingDispute).isNotNull();
        assertThat(buildingDispute.getInput1()).isEqualTo(BUILDING_SCHEDULE_INTRO_SDO);
        assertThat(buildingDispute.getInput2()).isEqualTo(BUILDING_SCHEDULE_COLUMNS_SDO);
        assertThat(buildingDispute.getInput3()).isEqualTo(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(buildingDispute.getInput4()).isEqualTo(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION);

        HousingDisrepair housing = caseData.getFastTrackHousingDisrepair();
        assertThat(housing).isNotNull();
        assertThat(housing.getClauseA()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_A);
        assertThat(housing.getClauseB()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_B);
        assertThat(housing.getClauseCAfterDate()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE);
        assertThat(housing.getClauseCBeforeDate()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE);
        assertThat(housing.getClauseD()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_D);
        assertThat(housing.getClauseE()).isEqualTo(HOUSING_DISREPAIR_CLAUSE_E);
        assertThat(housing.getFirstReportDateBy()).isNotNull();
        assertThat(housing.getJointStatementDateBy()).isNotNull();

        FastTrackClinicalNegligence clinicalNegligence = caseData.getFastTrackClinicalNegligence();
        assertThat(clinicalNegligence).isNotNull();
        assertThat(clinicalNegligence.getInput1()).isEqualTo(CLINICAL_DOCUMENTS_HEADING);
        assertThat(clinicalNegligence.getInput2()).isEqualTo(CLINICAL_PARTIES_SDO);
        assertThat(clinicalNegligence.getInput3()).isEqualTo(CLINICAL_NOTES_SDO);
        assertThat(clinicalNegligence.getInput4()).isEqualTo(CLINICAL_BUNDLE_SDO);

        assertThat(caseData.getSdoR2FastTrackCreditHire()).isNotNull();
        FastTrackPersonalInjury personalInjury = caseData.getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getInput1()).isEqualTo(PERSONAL_INJURY_PERMISSION_SDO);
        assertThat(personalInjury.getInput2()).isEqualTo(PERSONAL_INJURY_QUESTIONS);
        assertThat(personalInjury.getInput3()).isEqualTo(PERSONAL_INJURY_ANSWERS);
        assertThat(personalInjury.getInput4()).isEqualTo(PERSONAL_INJURY_UPLOAD);

        FastTrackRoadTrafficAccident roadTrafficAccident = caseData.getFastTrackRoadTrafficAccident();
        assertThat(roadTrafficAccident).isNotNull();
        assertThat(roadTrafficAccident.getInput()).isEqualTo(ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO);
    }

    @Test
    void shouldPopulateLegacyHousingDisrepairWhenFlagDisabled() {
        service = new SdoFastTrackSpecialistDirectionsService(deadlineService, false);
        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateSpecialistDirections(caseData);

        HousingDisrepair housing = caseData.getFastTrackHousingDisrepair();
        assertThat(housing).isNotNull();
        assertThat(housing.getInput1()).isEqualTo(HOUSING_SCHEDULE_INTRO_SDO);
        assertThat(housing.getInput2()).isEqualTo(HOUSING_SCHEDULE_COLUMNS_SDO);
        assertThat(housing.getInput3()).isEqualTo(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(housing.getInput4()).isEqualTo(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION);
        assertThat(housing.getDate1()).isNotNull();
        assertThat(housing.getDate2()).isNotNull();
    }
}
