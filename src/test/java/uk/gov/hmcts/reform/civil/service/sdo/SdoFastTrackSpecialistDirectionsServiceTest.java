package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;

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
        service = new SdoFastTrackSpecialistDirectionsService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateAllSpecialistSections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateSpecialistDirections(builder);
        CaseData result = builder.build();

        FastTrackBuildingDispute buildingDispute = result.getFastTrackBuildingDispute();
        assertThat(buildingDispute).isNotNull();
        assertThat(buildingDispute.getInput1()).isEqualTo(BUILDING_SCHEDULE_INTRO_SDO);
        assertThat(buildingDispute.getInput2()).isEqualTo(BUILDING_SCHEDULE_COLUMNS_SDO);
        assertThat(buildingDispute.getInput3()).isEqualTo(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(buildingDispute.getInput4()).isEqualTo(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION);

        FastTrackHousingDisrepair housing = result.getFastTrackHousingDisrepair();
        assertThat(housing).isNotNull();
        assertThat(housing.getInput1()).isEqualTo(HOUSING_SCHEDULE_INTRO_SDO);
        assertThat(housing.getInput2()).isEqualTo(HOUSING_SCHEDULE_COLUMNS_SDO);
        assertThat(housing.getInput3()).isEqualTo(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION);
        assertThat(housing.getInput4()).isEqualTo(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION);

        FastTrackClinicalNegligence clinicalNegligence = result.getFastTrackClinicalNegligence();
        assertThat(clinicalNegligence).isNotNull();
        assertThat(clinicalNegligence.getInput1()).isEqualTo(CLINICAL_DOCUMENTS_HEADING);
        assertThat(clinicalNegligence.getInput2()).isEqualTo(CLINICAL_PARTIES_SDO);
        assertThat(clinicalNegligence.getInput3()).isEqualTo(CLINICAL_NOTES_SDO);
        assertThat(clinicalNegligence.getInput4()).isEqualTo(CLINICAL_BUNDLE_SDO);

        assertThat(result.getSdoR2FastTrackCreditHire()).isNotNull();
        FastTrackPersonalInjury personalInjury = result.getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getInput1()).isEqualTo(PERSONAL_INJURY_PERMISSION_SDO);
        assertThat(personalInjury.getInput2()).isEqualTo(PERSONAL_INJURY_QUESTIONS);
        assertThat(personalInjury.getInput3()).isEqualTo(PERSONAL_INJURY_ANSWERS);
        assertThat(personalInjury.getInput4()).isEqualTo(PERSONAL_INJURY_UPLOAD);

        FastTrackRoadTrafficAccident roadTrafficAccident = result.getFastTrackRoadTrafficAccident();
        assertThat(roadTrafficAccident).isNotNull();
        assertThat(roadTrafficAccident.getInput()).isEqualTo(ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO);
    }
}
