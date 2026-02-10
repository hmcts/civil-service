package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DEFENDANT_UPLOAD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DISCLOSURE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_NON_COMPLIANCE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_PARTIES_LIAISE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_PROMPT_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_WITNESS_LIMIT_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_A;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_B;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_D;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_E;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO;

@Service
@RequiredArgsConstructor
public class SdoFastTrackSpecialistDirectionsService {

    private final SdoDeadlineService deadlineService;

    public void populateSpecialistDirections(CaseData caseData) {
        caseData.setFastTrackBuildingDispute(buildBuildingDispute());
        caseData.setFastTrackClinicalNegligence(buildClinicalNegligence());
        caseData.setSdoR2FastTrackCreditHire(buildCreditHire());
        caseData.setFastTrackCreditHire(buildFastTrackCreditHire());
        caseData.setFastTrackHousingDisrepair(buildHousingDisrepair());
        caseData.setFastTrackPersonalInjury(buildPersonalInjury());
        caseData.setFastTrackRoadTrafficAccident(buildRoadTrafficAccident());
    }

    private FastTrackBuildingDispute buildBuildingDispute() {
        return FastTrackBuildingDispute.builder()
            .input1(BUILDING_SCHEDULE_INTRO_SDO)
            .input2(BUILDING_SCHEDULE_COLUMNS_SDO)
            .input3(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .date1(deadlineService.nextWorkingDayFromNowWeeks(10))
            .input4(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(12))
            .build();
    }

    private FastTrackClinicalNegligence buildClinicalNegligence() {
        return FastTrackClinicalNegligence.builder()
            .input1(CLINICAL_DOCUMENTS_HEADING)
            .input2(CLINICAL_PARTIES_SDO)
            .input3(CLINICAL_NOTES_SDO)
            .input4(CLINICAL_BUNDLE_SDO)
            .build();
    }

    private SdoR2FastTrackCreditHire buildCreditHire() {
        List<AddOrRemoveToggle> toggleList = List.of(AddOrRemoveToggle.ADD);

        return SdoR2FastTrackCreditHire.builder()
            .input1(CREDIT_HIRE_DISCLOSURE_SDO)
            .input5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY)
            .input6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO)
            .date3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .input7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO)
            .date4(deadlineService.nextWorkingDayFromNowWeeks(10))
            .input8(CREDIT_HIRE_WITNESS_LIMIT_SDO)
            .detailsShowToggle(toggleList)
            .sdoR2FastTrackCreditHireDetails(buildCreditHireDetails())
            .build();
    }

    private FastTrackCreditHire buildFastTrackCreditHire() {
        return FastTrackCreditHire.builder()
            .input1(CREDIT_HIRE_DISCLOSURE_SDO)
            .input2(CREDIT_HIRE_STATEMENT_PROMPT_SDO)
            .date1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input3(CREDIT_HIRE_NON_COMPLIANCE_SDO)
            .input4(CREDIT_HIRE_PARTIES_LIAISE)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(6))
            .input5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY)
            .input6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO)
            .date3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .input7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO)
            .date4(deadlineService.nextWorkingDayFromNowWeeks(10))
            .input8(CREDIT_HIRE_WITNESS_LIMIT_SDO)
            .build();
    }

    private SdoR2FastTrackCreditHireDetails buildCreditHireDetails() {
        return SdoR2FastTrackCreditHireDetails.builder()
            .input2(CREDIT_HIRE_STATEMENT_PROMPT_SDO)
            .date1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input3(CREDIT_HIRE_NON_COMPLIANCE_SDO)
            .input4(CREDIT_HIRE_PARTIES_LIAISE)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(6))
            .build();
    }

    //ToDo: Need to discuss with Ruban
    private HousingDisrepair buildHousingDisrepair() {
        HousingDisrepair housingDisrepair = new HousingDisrepair();
        housingDisrepair.setClauseA(HOUSING_DISREPAIR_CLAUSE_A);
        housingDisrepair.setClauseB(HOUSING_DISREPAIR_CLAUSE_B);
        housingDisrepair.setFirstReportDateBy(deadlineService.nextWorkingDayFromNowWeeks(4));
        housingDisrepair.setClauseCBeforeDate(HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE);
        housingDisrepair.setJointStatementDateBy(deadlineService.nextWorkingDayFromNowWeeks(8));
        housingDisrepair.setClauseCAfterDate(HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE);
        housingDisrepair.setClauseD(HOUSING_DISREPAIR_CLAUSE_D);
        housingDisrepair.setClauseE(HOUSING_DISREPAIR_CLAUSE_E);
        return housingDisrepair;
    }

    private FastTrackPersonalInjury buildPersonalInjury() {
        return FastTrackPersonalInjury.builder()
            .input1(PERSONAL_INJURY_PERMISSION_SDO)
            .date1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input2(PERSONAL_INJURY_QUESTIONS)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input3(PERSONAL_INJURY_ANSWERS)
            .date3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .input4(PERSONAL_INJURY_UPLOAD)
            .date4(deadlineService.nextWorkingDayFromNowWeeks(8))
            .build();
    }

    private FastTrackRoadTrafficAccident buildRoadTrafficAccident() {
        return FastTrackRoadTrafficAccident.builder()
            .input(ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO)
            .date(deadlineService.nextWorkingDayFromNowWeeks(8))
            .build();
    }
}
