package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;

import java.util.List;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.CLAIMANT_EVIDENCE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.HEARING_TIME_TEXT_AFTER;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.LATER_THAN_FOUR_PM_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.PARTIES_LIASE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsOrderDefaultsService {

    private final SdoDeadlineService sdoDeadlineService;
    private final SdoJourneyToggleService sdoJourneyToggleService;

    public void populateSmallClaimsOrderDetails(CaseData caseData,
                                                CaseData.CaseDataBuilder<?, ?> updatedData,
                                                List<OrderDetailsPagesSectionsToggle> checkList) {
        SmallClaimsJudgesRecital tempSmallClaimsJudgesRecital = SmallClaimsJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.smallClaimsJudgesRecital(tempSmallClaimsJudgesRecital).build();

        SmallClaimsDocuments tempSmallClaimsDocuments = SmallClaimsDocuments.builder()
            .input1("Each party must upload to the Digital Portal copies of all documents which they wish the court to"
                        + " consider when reaching its decision not less than 21 days before the hearing.")
            .input2("The court may refuse to consider any document which has not been uploaded to the "
                        + "Digital Portal by the above date.")
            .build();

        updatedData.smallClaimsDocuments(tempSmallClaimsDocuments).build();

        SdoR2SmallClaimsWitnessStatements tempSdoR2SmallClaimsWitnessStatements = SdoR2SmallClaimsWitnessStatements
            .builder()
            .sdoStatementOfWitness(
                "Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                    + " upon whose evidence they intend to rely at the hearing not less than 21 days before"
                    + " the hearing.")
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
            .build();
        updatedData.sdoR2SmallClaimsWitnessStatementOther(tempSdoR2SmallClaimsWitnessStatements).build();

        sdoJourneyToggleService.applySmallClaimsMediationStatement(caseData, updatedData);

        SmallClaimsFlightDelay tempSmallClaimsFlightDelay = SmallClaimsFlightDelay.builder()
            .smallClaimsFlightDelayToggle(checkList)
            .relatedClaimsInput("In the event that the Claimant(s) or Defendant(s) are aware if other \n"
                                    + "claims relating to the same flight they must notify the court \n"
                                    + "where the claim is being managed within 14 days of receipt of \n"
                                    + "this Order providing all relevant details of those claims including \n"
                                    + "case number(s), hearing date(s) and copy final substantive order(s) \n"
                                    + "if any, to assist the Court with ongoing case management which may \n"
                                    + "include the cases being heard together.")
            .legalDocumentsInput("Any arguments as to the law to be applied to this claim, together with \n"
                                     + "copies of legal authorities or precedents relied on, shall be uploaded \n"
                                     + "to the Digital Portal not later than 3 full working days before the \n"
                                     + "final hearing date.")
            .build();

        updatedData.smallClaimsFlightDelay(tempSmallClaimsFlightDelay).build();

        SmallClaimsHearing tempSmallClaimsHearing = SmallClaimsHearing.builder()
            .input1("The hearing of the claim will be on a date to be notified to you by a separate notification. "
                        + "The hearing will have a time estimate of")
            .input2(HEARING_TIME_TEXT_AFTER)
            .build();

        updatedData.smallClaimsHearing(tempSmallClaimsHearing).build();

        SmallClaimsNotes.SmallClaimsNotesBuilder tempSmallClaimsNotes = SmallClaimsNotes.builder();
        tempSmallClaimsNotes.input("This order has been made without hearing. "
                                       + "Each party has the right to apply to have this Order set aside or varied. "
                                       + "Any such application must be received by the Court "
                                       + "(together with the appropriate fee) by 4pm on "
                                       + DateFormatHelper.formatLocalDate(
                                           sdoDeadlineService.workingDaysFromNow(5), DATE)
                                       + ".");

        updatedData.smallClaimsNotes(tempSmallClaimsNotes.build()).build();

        SmallClaimsCreditHire tempSmallClaimsCreditHire = SmallClaimsCreditHire.builder()
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
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4(PARTIES_LIASE_TEXT + LATER_THAN_FOUR_PM_TEXT)
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .input7(CLAIMANT_EVIDENCE_TEXT)
            .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input11(WITNESS_STATEMENT)
            .build();

        updatedData.smallClaimsCreditHire(tempSmallClaimsCreditHire).build();

        SmallClaimsRoadTrafficAccident tempSmallClaimsRoadTrafficAccident = SmallClaimsRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the parties"
                       + " and uploaded to the Digital Portal no later than 21 days before the hearing.")
            .build();

        updatedData.smallClaimsRoadTrafficAccident(tempSmallClaimsRoadTrafficAccident).build();

        if (CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions())) {
            updatedData.drawDirectionsOrderRequired(null);
            updatedData.drawDirectionsOrderSmallClaims(null);
            updatedData.fastClaims(null);
            updatedData.smallClaims(null);
            updatedData.claimsTrack(null);
            updatedData.orderType(null);
            updatedData.trialAdditionalDirectionsForFastTrack(null);
            updatedData.drawDirectionsOrderSmallClaimsAdditionalDirections(null);
            updatedData.fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(null).build());
            updatedData.disposalHearingAddNewDirections(null);
            updatedData.smallClaimsAddNewDirections(null);
            updatedData.fastTrackAddNewDirections(null);
            updatedData.sdoHearingNotes(null);
            updatedData.fastTrackHearingNotes(null);
            updatedData.disposalHearingHearingNotes(null);
            updatedData.sdoR2SmallClaimsHearing(null);
            updatedData.sdoR2SmallClaimsUploadDoc(null);
            updatedData.sdoR2SmallClaimsPPI(null);
            updatedData.sdoR2SmallClaimsImpNotes(null);
            updatedData.sdoR2SmallClaimsWitnessStatements(null);
            updatedData.sdoR2SmallClaimsHearingToggle(null);
            updatedData.sdoR2SmallClaimsJudgesRecital(null);
            updatedData.sdoR2SmallClaimsWitnessStatementsToggle(null);
            updatedData.sdoR2SmallClaimsPPIToggle(null);
            updatedData.sdoR2SmallClaimsUploadDocToggle(null);
        }
    }
}
