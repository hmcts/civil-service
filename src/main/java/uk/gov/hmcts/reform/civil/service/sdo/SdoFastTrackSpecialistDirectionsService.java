package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
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
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_SDO;
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
        return new FastTrackBuildingDispute()
            .setInput1(BUILDING_SCHEDULE_INTRO_SDO)
            .setInput2(BUILDING_SCHEDULE_COLUMNS_SDO)
            .setInput3(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .setDate1(deadlineService.nextWorkingDayFromNowWeeks(10))
            .setInput4(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .setDate2(deadlineService.nextWorkingDayFromNowWeeks(12));
    }

    private FastTrackClinicalNegligence buildClinicalNegligence() {
        return new FastTrackClinicalNegligence()
            .setInput1(CLINICAL_DOCUMENTS_HEADING)
            .setInput2(CLINICAL_PARTIES_SDO)
            .setInput3(CLINICAL_NOTES_SDO)
            .setInput4(CLINICAL_BUNDLE_SDO);
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
        return new FastTrackCreditHire()
            .setInput1(CREDIT_HIRE_DISCLOSURE_SDO)
            .setInput2(CREDIT_HIRE_STATEMENT_PROMPT_SDO)
            .setDate1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .setInput3(CREDIT_HIRE_NON_COMPLIANCE_SDO)
            .setInput4(CREDIT_HIRE_PARTIES_LIAISE)
            .setDate2(deadlineService.nextWorkingDayFromNowWeeks(6))
            .setInput5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY)
            .setInput6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO)
            .setDate3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .setInput7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO)
            .setDate4(deadlineService.nextWorkingDayFromNowWeeks(10))
            .setInput8(CREDIT_HIRE_WITNESS_LIMIT_SDO);
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

    private FastTrackHousingDisrepair buildHousingDisrepair() {
        return new FastTrackHousingDisrepair()
            .setInput1(HOUSING_SCHEDULE_INTRO_SDO)
            .setInput2(HOUSING_SCHEDULE_COLUMNS_SDO)
            .setInput3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .setDate1(deadlineService.nextWorkingDayFromNowWeeks(10))
            .setInput4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .setDate2(deadlineService.nextWorkingDayFromNowWeeks(12));
    }

    private FastTrackPersonalInjury buildPersonalInjury() {
        return new FastTrackPersonalInjury()
            .setInput1(PERSONAL_INJURY_PERMISSION_SDO)
            .setDate1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .setInput2(PERSONAL_INJURY_QUESTIONS)
            .setDate2(deadlineService.nextWorkingDayFromNowWeeks(4))
            .setInput3(PERSONAL_INJURY_ANSWERS)
            .setDate3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .setInput4(PERSONAL_INJURY_UPLOAD)
            .setDate4(deadlineService.nextWorkingDayFromNowWeeks(8));
    }

    private FastTrackRoadTrafficAccident buildRoadTrafficAccident() {
        return new FastTrackRoadTrafficAccident()
            .setInput(ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO)
            .setDate(deadlineService.nextWorkingDayFromNowWeeks(8));
    }
}
