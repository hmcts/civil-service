package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_SDO;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.CLAIMANT_EVIDENCE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.LATER_THAN_FOUR_PM_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.PARTIES_LIASE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.WITNESS_STATEMENT;

/**
 * Builds the specialist fast-track sections (credit hire, building/clinical disputes, PI, housing, RTA) so the
 * parent defaults service remains a thin orchestrator.
 */
@Service
@RequiredArgsConstructor
public class SdoFastTrackSpecialistDirectionsService {

    private final SdoDeadlineService deadlineService;

    public void populateSpecialistDirections(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackBuildingDispute(buildBuildingDispute()).build();
        updatedData.fastTrackClinicalNegligence(buildClinicalNegligence()).build();
        updatedData.sdoR2FastTrackCreditHire(buildCreditHire()).build();
        updatedData.fastTrackHousingDisrepair(buildHousingDisrepair()).build();
        updatedData.fastTrackPersonalInjury(buildPersonalInjury()).build();
        updatedData.fastTrackRoadTrafficAccident(buildRoadTrafficAccident()).build();
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
            .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
            .input5("If the parties fail to agree basic hire rates pursuant to the paragraph above, "
                        + "each party may rely upon written evidence by way of witness statement of one witness to"
                        + " provide evidence of basic hire rates available within the claimant's geographical location,"
                        + " from a mainstream supplier, or a local reputable supplier if none is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .input7(CLAIMANT_EVIDENCE_TEXT)
            .date4(deadlineService.nextWorkingDayFromNowWeeks(10))
            .input8(WITNESS_STATEMENT)
            .detailsShowToggle(toggleList)
            .sdoR2FastTrackCreditHireDetails(buildCreditHireDetails())
            .build();
    }

    private SdoR2FastTrackCreditHireDetails buildCreditHireDetails() {
        return SdoR2FastTrackCreditHireDetails.builder()
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4(PARTIES_LIASE_TEXT + LATER_THAN_FOUR_PM_TEXT)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(6))
            .build();
    }

    private FastTrackHousingDisrepair buildHousingDisrepair() {
        return FastTrackHousingDisrepair.builder()
            .input1(HOUSING_SCHEDULE_INTRO_SDO)
            .input2(HOUSING_SCHEDULE_COLUMNS_SDO)
            .input3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .date1(deadlineService.nextWorkingDayFromNowWeeks(10))
            .input4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .date2(deadlineService.nextWorkingDayFromNowWeeks(12))
            .build();
    }

    private FastTrackPersonalInjury buildPersonalInjury() {
        return FastTrackPersonalInjury.builder()
            .input1("The claimant has permission to rely upon the written expert evidence already uploaded to "
                        + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
                        + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
                        + " 4pm on")
            .date1(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input2("Any questions which are to be addressed to an expert must be sent to the expert directly "
                        + "and uploaded to the Digital Portal by 4pm on")
            .date2(deadlineService.nextWorkingDayFromNowWeeks(4))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(deadlineService.nextWorkingDayFromNowWeeks(8))
            .input4("and uploaded to the Digital Portal by")
            .date4(deadlineService.nextWorkingDayFromNowWeeks(8))
            .build();
    }

    private FastTrackRoadTrafficAccident buildRoadTrafficAccident() {
        return FastTrackRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                       + "parties and uploaded to the Digital Portal by 4pm on")
            .date(deadlineService.nextWorkingDayFromNowWeeks(8))
            .build();
    }
}
