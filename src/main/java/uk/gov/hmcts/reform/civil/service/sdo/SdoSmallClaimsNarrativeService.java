package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;

import java.util.List;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_A;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_B;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_D;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_E;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DEFENDANT_UPLOAD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DISCLOSURE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_NON_COMPLIANCE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_PARTIES_LIAISE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_PROMPT_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_WITNESS_LIMIT_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_RELATED_CLAIMS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_DOCUMENTS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_DOCUMENTS_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_HEARING_FEE_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_HEARING_LISTING_NOTICE;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsNarrativeService {

    private final SdoDeadlineService sdoDeadlineService;

    public void applyJudgesRecital(CaseData caseData) {
        caseData.setSmallClaimsJudgesRecital(SmallClaimsJudgesRecital.builder()
                                                  .input(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA)
                                                  .build());
    }

    public void applyDocumentDirections(CaseData caseData) {
        caseData.setSmallClaimsDocuments(SmallClaimsDocuments.builder()
                                             .input1(SMALL_CLAIMS_DOCUMENTS_UPLOAD)
                                             .input2(SMALL_CLAIMS_DOCUMENTS_WARNING)
                                             .build());
    }

    public void applyWitnessStatements(CaseData caseData) {
        caseData.setSdoR2SmallClaimsWitnessStatementOther(SdoR2SmallClaimsWitnessStatements.builder()
                                                             .sdoStatementOfWitness(WITNESS_STATEMENT_TEXT)
                                                             .isRestrictWitness(NO)
                                                             .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                                                                                              .noOfWitnessClaimant(2)
                                                                                              .noOfWitnessDefendant(2)
                                                                                              .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                                                                                              .build())
                                                             .isRestrictPages(NO)
                                                             .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                                                                                         .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                                                                                         .noOfPages(12)
                                                                                         .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                                                                                         .build())
                                                             .text(WITNESS_DESCRIPTION_TEXT)
                                                             .build());
    }

    public void applyCreditHire(CaseData caseData) {
        caseData.setSmallClaimsCreditHire(SmallClaimsCreditHire.builder()
                                              .input1(CREDIT_HIRE_DISCLOSURE_SDO)
                                              .input2(CREDIT_HIRE_STATEMENT_PROMPT_SDO)
                                              .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                                              .input3(CREDIT_HIRE_NON_COMPLIANCE_SDO)
                                              .input4(CREDIT_HIRE_PARTIES_LIAISE)
                                              .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                                              .input5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY)
                                              .input6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO)
                                              .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
                                              .input7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO)
                                              .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                                              .input11(CREDIT_HIRE_WITNESS_LIMIT_SDO)
                                              .build());
    }

    public void applyRoadTrafficAccident(CaseData caseData) {
        caseData.setSmallClaimsRoadTrafficAccident(SmallClaimsRoadTrafficAccident.builder()
                                                       .input(ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS)
                                                       .build());
    }

    public void applyFlightDelaySection(CaseData caseData,
                                        List<OrderDetailsPagesSectionsToggle> checkList) {
        caseData.setSmallClaimsFlightDelay(SmallClaimsFlightDelay.builder()
                                                   .smallClaimsFlightDelayToggle(checkList)
                                                   .relatedClaimsInput(FLIGHT_DELAY_RELATED_CLAIMS_NOTICE)
                                                   .legalDocumentsInput(FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE)
                                                   .build());
    }

    public void applyHearingSection(CaseData caseData) {
        caseData.setSmallClaimsHearing(SmallClaimsHearing.builder()
                                               .input1(SMALL_CLAIMS_HEARING_LISTING_NOTICE)
                                               .input2(SMALL_CLAIMS_HEARING_FEE_WARNING)
                                               .build());
    }

    public void applyNotesSection(CaseData caseData) {
        caseData.setSmallClaimsNotes(SmallClaimsNotes.builder()
                                              .input(String.format(
                                                  "%s %s.",
                                                  ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                                                  DateFormatHelper.formatLocalDate(sdoDeadlineService.workingDaysFromNow(5), DATE)
                                              ))
                                              .build());
    }

    public void applyHousingDisrepair(CaseData caseData) {
        HousingDisrepair housingDisrepair = new HousingDisrepair();
        housingDisrepair.setClauseA(HOUSING_DISREPAIR_CLAUSE_A);
        housingDisrepair.setClauseB(HOUSING_DISREPAIR_CLAUSE_B);
        housingDisrepair.setFirstReportDateBy(sdoDeadlineService.nextWorkingDayFromNowWeeks(4));
        housingDisrepair.setClauseCBeforeDate(HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE);
        housingDisrepair.setJointStatementDateBy(sdoDeadlineService.nextWorkingDayFromNowWeeks(8));
        housingDisrepair.setClauseCAfterDate(HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE);
        housingDisrepair.setClauseD(HOUSING_DISREPAIR_CLAUSE_D);
        housingDisrepair.setClauseE(HOUSING_DISREPAIR_CLAUSE_E);
        caseData.setSmallClaimsHousingDisrepair(housingDisrepair);
    }
}
