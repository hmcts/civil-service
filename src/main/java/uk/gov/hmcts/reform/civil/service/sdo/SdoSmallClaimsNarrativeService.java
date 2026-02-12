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
        SmallClaimsJudgesRecital judgesRecital = new SmallClaimsJudgesRecital();
        judgesRecital.setInput(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA);
        caseData.setSmallClaimsJudgesRecital(judgesRecital);
    }

    public void applyDocumentDirections(CaseData caseData) {
        SmallClaimsDocuments documents = new SmallClaimsDocuments();
        documents.setInput1(SMALL_CLAIMS_DOCUMENTS_UPLOAD);
        documents.setInput2(SMALL_CLAIMS_DOCUMENTS_WARNING);
        caseData.setSmallClaimsDocuments(documents);
    }

    public void applyWitnessStatements(CaseData caseData) {
        SdoR2SmallClaimsRestrictWitness restrictWitness = new SdoR2SmallClaimsRestrictWitness();
        restrictWitness.setNoOfWitnessClaimant(2);
        restrictWitness.setNoOfWitnessDefendant(2);
        restrictWitness.setPartyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT);

        SdoR2SmallClaimsRestrictPages restrictPages = new SdoR2SmallClaimsRestrictPages();
        restrictPages.setWitnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1);
        restrictPages.setNoOfPages(12);
        restrictPages.setFontDetails(RESTRICT_NUMBER_PAGES_TEXT2);

        SdoR2SmallClaimsWitnessStatements witnessStatements = new SdoR2SmallClaimsWitnessStatements();
        witnessStatements.setSdoStatementOfWitness(WITNESS_STATEMENT_TEXT);
        witnessStatements.setIsRestrictWitness(NO);
        witnessStatements.setSdoR2SmallClaimsRestrictWitness(restrictWitness);
        witnessStatements.setIsRestrictPages(NO);
        witnessStatements.setSdoR2SmallClaimsRestrictPages(restrictPages);
        witnessStatements.setText(WITNESS_DESCRIPTION_TEXT);
        caseData.setSdoR2SmallClaimsWitnessStatementOther(witnessStatements);
    }

    public void applyCreditHire(CaseData caseData) {
        SmallClaimsCreditHire creditHire = new SmallClaimsCreditHire();
        creditHire.setInput1(CREDIT_HIRE_DISCLOSURE_SDO);
        creditHire.setInput2(CREDIT_HIRE_STATEMENT_PROMPT_SDO);
        creditHire.setDate1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4));
        creditHire.setInput3(CREDIT_HIRE_NON_COMPLIANCE_SDO);
        creditHire.setInput4(CREDIT_HIRE_PARTIES_LIAISE);
        creditHire.setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6));
        creditHire.setInput5(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY);
        creditHire.setInput6(CREDIT_HIRE_DEFENDANT_UPLOAD_SDO);
        creditHire.setDate3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8));
        creditHire.setInput7(CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO);
        creditHire.setDate4(sdoDeadlineService.nextWorkingDayFromNowWeeks(10));
        creditHire.setInput11(CREDIT_HIRE_WITNESS_LIMIT_SDO);
        caseData.setSmallClaimsCreditHire(creditHire);
    }

    public void applyRoadTrafficAccident(CaseData caseData) {
        SmallClaimsRoadTrafficAccident roadTrafficAccident = new SmallClaimsRoadTrafficAccident();
        roadTrafficAccident.setInput(ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS);
        caseData.setSmallClaimsRoadTrafficAccident(roadTrafficAccident);
    }

    public void applyFlightDelaySection(CaseData caseData,
                                        List<OrderDetailsPagesSectionsToggle> checkList) {
        SmallClaimsFlightDelay flightDelay = new SmallClaimsFlightDelay();
        flightDelay.setSmallClaimsFlightDelayToggle(checkList);
        flightDelay.setRelatedClaimsInput(FLIGHT_DELAY_RELATED_CLAIMS_NOTICE);
        flightDelay.setLegalDocumentsInput(FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE);
        caseData.setSmallClaimsFlightDelay(flightDelay);
    }

    public void applyHearingSection(CaseData caseData) {
        SmallClaimsHearing hearing = new SmallClaimsHearing();
        hearing.setInput1(SMALL_CLAIMS_HEARING_LISTING_NOTICE);
        hearing.setInput2(SMALL_CLAIMS_HEARING_FEE_WARNING);
        caseData.setSmallClaimsHearing(hearing);
    }

    public void applyNotesSection(CaseData caseData) {
        SmallClaimsNotes notes = new SmallClaimsNotes();
        var orderDeadline = sdoDeadlineService.workingDaysFromNow(5);
        notes.setInput(String.format(
            "%s %s.",
            ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
            DateFormatHelper.formatLocalDate(orderDeadline, DATE)
        ));
        notes.setDate(orderDeadline);
        caseData.setSmallClaimsNotes(notes);
    }
}
