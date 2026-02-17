package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.beans.factory.annotation.Value;
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
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_SDO;
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
public class SdoFastTrackSpecialistDirectionsService {

    private final SdoDeadlineService deadlineService;
    private final boolean otherRemedyEnabled;

    public SdoFastTrackSpecialistDirectionsService(SdoDeadlineService deadlineService,
                                                   @Value("${other_remedy.enabled:false}") boolean otherRemedyEnabled) {
        this.deadlineService = deadlineService;
        this.otherRemedyEnabled = otherRemedyEnabled;
    }

    public void populateSpecialistDirections(CaseData caseData) {
        caseData.setFastTrackBuildingDispute(buildBuildingDispute());
        caseData.setFastTrackClinicalNegligence(buildClinicalNegligence());
        caseData.setSdoR2FastTrackCreditHire(buildCreditHire());
        caseData.setFastTrackCreditHire(buildFastTrackCreditHire());
        if (otherRemedyEnabled) {
            caseData.setFastTrackHousingDisrepair(buildHousingDisrepair());
        } else {
            caseData.setFastTrackHousingDisrepair(buildFastTrackHousingDisrepairAsHousingDisrepair());
        }
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

        SdoR2FastTrackCreditHire creditHire = new SdoR2FastTrackCreditHire();
        creditHire.setInput1(CREDIT_HIRE_DISCLOSURE_SDO);
        creditHire.setInput5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY);
        creditHire.setInput6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO);
        creditHire.setDate3(deadlineService.nextWorkingDayFromNowWeeks(8));
        creditHire.setInput7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO);
        creditHire.setDate4(deadlineService.nextWorkingDayFromNowWeeks(10));
        creditHire.setInput8(CREDIT_HIRE_WITNESS_LIMIT_SDO);
        creditHire.setDetailsShowToggle(toggleList);
        creditHire.setSdoR2FastTrackCreditHireDetails(buildCreditHireDetails());
        return creditHire;
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
        SdoR2FastTrackCreditHireDetails creditHireDetails = new SdoR2FastTrackCreditHireDetails();
        creditHireDetails.setInput2(CREDIT_HIRE_STATEMENT_PROMPT_SDO);
        creditHireDetails.setDate1(deadlineService.nextWorkingDayFromNowWeeks(4));
        creditHireDetails.setInput3(CREDIT_HIRE_NON_COMPLIANCE_SDO);
        creditHireDetails.setInput4(CREDIT_HIRE_PARTIES_LIAISE);
        creditHireDetails.setDate2(deadlineService.nextWorkingDayFromNowWeeks(6));
        return creditHireDetails;
    }

    private HousingDisrepair buildFastTrackHousingDisrepairAsHousingDisrepair() {
        HousingDisrepair housingDisrepair = new HousingDisrepair();
        housingDisrepair.setInput1(HOUSING_SCHEDULE_INTRO_SDO);
        housingDisrepair.setInput2(HOUSING_SCHEDULE_COLUMNS_SDO);
        housingDisrepair.setInput3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION);
        housingDisrepair.setDate1(deadlineService.nextWorkingDayFromNowWeeks(10));
        housingDisrepair.setInput4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION);
        housingDisrepair.setDate2(deadlineService.nextWorkingDayFromNowWeeks(12));
        return housingDisrepair;
    }

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
