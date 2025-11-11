package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.STATEMENT_WITNESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.CLAIMANT_EVIDENCE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.LATER_THAN_FOUR_PM_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.PARTIES_LIASE_TEXT;
import static uk.gov.hmcts.reform.civil.service.sdo.SdoTrackOrderText.WITNESS_STATEMENT;

@Service
@RequiredArgsConstructor
public class SdoFastTrackOrderDefaultsService {

    private static final List<DateToShowToggle> DATE_TO_SHOW_TRUE = List.of(DateToShowToggle.SHOW);

    private final SdoDeadlineService sdoDeadlineService;

    public void populateFastTrackOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackJudgesRecital tempFastTrackJudgesRecital = FastTrackJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.fastTrackJudgesRecital(tempFastTrackJudgesRecital).build();

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
        updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact()).build();

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = FastTrackSchedulesOfLoss.builder()
            .input1("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input2("If the defendant wants to challenge this claim, upload to the Digital Portal "
                        + "counter-schedule of loss by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
            .input3("If there is a claim for future pecuniary loss and the parties have not already set out "
                        + "their case on periodical payments, they must do so in the respective schedule and "
                        + "counter-schedule.")
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1("The time provisionally allowed for this trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date stated on this order.")
            .input3("At least 7 days before the trial, the claimant must upload to the Digital Portal")
            .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackHearingTime tempFastTrackHearingTime = FastTrackHearingTime.builder()
            .dateFrom(LocalDate.now().plusWeeks(22))
            .dateTo(LocalDate.now().plusWeeks(30))
            .dateToToggle(DATE_TO_SHOW_TRUE)
            .helpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.")
            .build();
        updatedData.fastTrackHearingTime(tempFastTrackHearingTime);

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on")
            .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(1))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

        FastTrackOrderWithoutJudgement tempFastTrackOrderWithoutJudgement = FastTrackOrderWithoutJudgement.builder()
            .input(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply "
                    + "to have this Order set aside or varied. Any such application must be "
                    + "received by the Court (together with the appropriate fee) by 4pm "
                    + "on %s.",
                sdoDeadlineService.orderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ))
            .build();

        updatedData.fastTrackOrderWithoutJudgement(tempFastTrackOrderWithoutJudgement);

        FastTrackBuildingDispute tempFastTrackBuildingDispute = FastTrackBuildingDispute.builder()
            .input1("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                        + "or any other relevant matters")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged defect\n"
                        + "  •  Claimant’s costing\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Defendant’s costing\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                        + " completed by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input4("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                        + "with the relevant columns in response completed by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
            .build();

        updatedData.fastTrackBuildingDispute(tempFastTrackBuildingDispute).build();

        FastTrackClinicalNegligence tempFastTrackClinicalNegligence = FastTrackClinicalNegligence.builder()
            .input1("Documents should be retained as follows:")
            .input2("a) The parties must retain all electronically stored documents relating to the issues in this "
                        + "claim.")
            .input3("b) the defendant must retain the original clinical notes relating to the issues in this claim. "
                        + "The defendant must give facilities for inspection by the claimant, the claimant's legal "
                        + "advisers and experts of these original notes on 7 days written notice.")
            .input4("c) Legible copies of the medical and educational records of the claimant "
                        + "are to be placed in a separate paginated bundle by the claimant's "
                        + "solicitors and kept up to date. All references to medical notes are to be made by reference "
                        + "to the pages in that bundle.")
            .build();

        updatedData.fastTrackClinicalNegligence(tempFastTrackClinicalNegligence).build();

        List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
        SdoR2FastTrackCreditHireDetails tempSdoR2FastTrackCreditHireDetails = SdoR2FastTrackCreditHireDetails.builder()
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4(PARTIES_LIASE_TEXT + LATER_THAN_FOUR_PM_TEXT)
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
            .build();

        SdoR2FastTrackCreditHire tempSdoR2FastTrackCreditHire = SdoR2FastTrackCreditHire.builder()
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
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .input7(CLAIMANT_EVIDENCE_TEXT)
            .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input8(WITNESS_STATEMENT)
            .detailsShowToggle(addOrRemoveToggleList)
            .sdoR2FastTrackCreditHireDetails(tempSdoR2FastTrackCreditHireDetails)
            .build();

        updatedData.sdoR2FastTrackCreditHire(tempSdoR2FastTrackCreditHire).build();

        FastTrackHousingDisrepair tempFastTrackHousingDisrepair = FastTrackHousingDisrepair.builder()
            .input1("The claimant must prepare a Scott Schedule of the items in disrepair.")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged disrepair\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                        + "columns completed by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input4("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                        + "relevant columns in response completed by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
            .build();

        updatedData.fastTrackHousingDisrepair(tempFastTrackHousingDisrepair).build();

        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("The claimant has permission to rely upon the written expert evidence already uploaded to "
                        + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
                        + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
                        + " 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2("Any questions which are to be addressed to an expert must be sent to the expert directly "
                        + "and uploaded to the Digital Portal by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .input4("and uploaded to the Digital Portal by")
            .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();

        FastTrackRoadTrafficAccident tempFastTrackRoadTrafficAccident = FastTrackRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                       + "parties and uploaded to the Digital Portal by 4pm on")
            .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackRoadTrafficAccident(tempFastTrackRoadTrafficAccident).build();
    }

    private SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
        return SdoR2WitnessOfFact.builder()
            .sdoStatementOfWitness(STATEMENT_WITNESS)
            .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                      .isRestrictWitness(NO)
                                      .restrictNoOfWitnessDetails(
                                          SdoR2RestrictNoOfWitnessDetails.builder()
                                              .noOfWitnessClaimant(3)
                                              .noOfWitnessDefendant(3)
                                              .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                                              .build())
                                      .build())
            .sdoRestrictPages(SdoR2RestrictPages.builder()
                                  .isRestrictPages(NO)
                                  .restrictNoOfPagesDetails(
                                      SdoR2RestrictNoOfPagesDetails.builder()
                                          .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                                          .noOfPages(12)
                                          .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                                          .build())
                                  .build())
            .sdoWitnessDeadline(DEADLINE)
            .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
            .sdoWitnessDeadlineText(DEADLINE_EVIDENCE)
            .build();
    }
}
