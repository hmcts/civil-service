package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
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
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastTrackPopulator {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService featureToggleService;
    private final List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
    static final String WITNESS_STATEMENT_STRING = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String LATER_THAN_FOUR_PM_STRING = "later than 4pm on";
    static final String CLAIMANT_EVIDENCE_STRING = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";

    public void setFastTrackFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        setFastTrackJudgesRecital(updatedData);
        setFastTrackDisclosureOfDocuments(updatedData);
        setFastTrackWitnessOfFact(updatedData);
        setFastTrackSchedulesOfLoss(updatedData);
        setFastTrackTrial(updatedData);
        setFastTrackHearingTime(updatedData);
        setFastTrackNotes(updatedData);
        setFastTrackOrderWithoutJudgement(updatedData);
        setFastTrackBuildingDispute(updatedData);
        setFastTrackClinicalNegligence(updatedData);
        setFastTrackCreditHire(updatedData);
        setFastTrackHousingDisrepair(updatedData);
        setFastTrackPersonalInjury(updatedData);
        setFastTrackRoadTrafficAccident(updatedData);
    }

    private void setFastTrackJudgesRecital(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input("Upon considering the statements of case and the information provided by the parties,")
                .build());
    }

    private void setFastTrackDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackDisclosureOfDocuments(FastTrackDisclosureOfDocuments.builder()
                .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .input3("Requests will be complied with within 7 days of the receipt of the request.")
                .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build());
    }

    private void setFastTrackWitnessOfFact(CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact());
        } else {
            updatedData.fastTrackWitnessOfFact(getFastTrackWitnessOfFact());
        }
    }

    private void setFastTrackSchedulesOfLoss(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackSchedulesOfLoss(FastTrackSchedulesOfLoss.builder()
                .input1("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input2("If the defendant wants to challenge this claim, upload to the Digital Portal counter-schedule of loss by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                .input3("If there is a claim for future pecuniary loss and the parties have not already set out their case on periodical payments," +
                        " they must do so in the respective schedule and counter-schedule.")
                .build());
    }

    private void setFastTrackTrial(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackTrial(FastTrackTrial.builder()
                .input1("The time provisionally allowed for this trial is")
                .date1(LocalDate.now().plusWeeks(22))
                .date2(LocalDate.now().plusWeeks(30))
                .input2("If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date stated on this order.")
                .input3("At least 7 days before the trial, the claimant must upload to the Digital Portal")
                .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
                .build());
    }

    private void setFastTrackHearingTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackHearingTime(FastTrackHearingTime.builder()
                .dateFrom(LocalDate.now().plusWeeks(22))
                .dateTo(LocalDate.now().plusWeeks(30))
                .dateToToggle(dateToShowTrue)
                .helpText1("If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.")
                .helpText2("Not more than seven nor less than three clear days before the trial, the claimant must file at court and serve an indexed and" +
                        " paginated bundle of documents which complies with the requirements of Rule 39.5 Civil Procedure Rules and which complies with requirements of PD32." +
                        " The parties must endeavour to agree the contents of the bundle before it is filed. The bundle will include a case summary and a chronology.")
                .build());
    }

    private void setFastTrackNotes(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackNotes(FastTrackNotes.builder()
                .input("This Order has been made without a hearing. Each party has the right to apply to have this Order set aside or varied." +
                        " Any application must be received by the Court, together with the appropriate fee by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
                .build());
    }

    private void setFastTrackOrderWithoutJudgement(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackOrderWithoutJudgement(FastTrackOrderWithoutJudgement.builder()
                .input(String.format(
                        "This order has been made without hearing. Each party has the right to apply to have this Order set aside or varied." +
                                " Any such application must be received by the Court (together with the appropriate fee) by 4pm on %s.",
                        deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
                ))
                .build());
    }

    private void setFastTrackBuildingDispute(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackBuildingDispute(FastTrackBuildingDispute.builder()
                .input1("The claimant must prepare a Scott Schedule of the defects, items of damage, or any other relevant matters")
                .input2("""
            The columns should be headed:
              •  Item
              •  Alleged defect
              •  Claimant’s costing
              •  Defendant’s response
              •  Defendant’s costing
              •  Reserved for Judge’s use""")
                .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input4("The defendant must upload to the Digital Portal an amended version of the Scott Schedule with the relevant columns in response completed by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                .build());
    }

    private void setFastTrackClinicalNegligence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackClinicalNegligence(FastTrackClinicalNegligence.builder()
                .input1("Documents should be retained as follows:")
                .input2("a) The parties must retain all electronically stored documents relating to the issues in this claim.")
                .input3("b) the defendant must retain the original clinical notes relating to the issues in this claim." +
                        " The defendant must give facilities for inspection by the claimant," +
                        " the claimant's legal advisers and experts of these original notes on 7 days written notice.")
                .input4("c) Legible copies of the medical and educational records of the claimant are to be placed in a" +
                        " separate paginated bundle by the claimant's solicitors and kept up to date." +
                        " All references to medical notes are to be made by reference to the pages in that bundle.")
                .build());
    }

    private void setFastTrackCreditHire(CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.sdoR2FastTrackCreditHire(getSdoR2FastTrackCreditHire());
        }
        updatedData.fastTrackCreditHire(getFastTrackCreditHire());
    }

    private void setFastTrackHousingDisrepair(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackHousingDisrepair(FastTrackHousingDisrepair.builder()
                .input1("The claimant must prepare a Scott Schedule of the items in disrepair.")
                .input2("""
            The columns should be headed:
              •  Item
              •  Alleged disrepair
              •  Defendant’s response
              •  Reserved for Judge’s use""")
                .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input4("The defendant must upload to the Digital Portal the amended Scott Schedule with the relevant columns in response completed by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                .build());
    }

    private void setFastTrackPersonalInjury(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackPersonalInjury(FastTrackPersonalInjury.builder()
                .input1("The claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the particulars of claim and in" +
                        " addition has permission to rely upon any associated correspondence or updating report which is uploaded to the Digital Portal by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input2("Any questions which are to be addressed to an expert must be sent to the expert directly and uploaded to the Digital Portal by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input3("The answers to the questions shall be answered by the Expert by")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input4("and uploaded to the Digital Portal by")
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build());
    }

    private void setFastTrackRoadTrafficAccident(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackRoadTrafficAccident(FastTrackRoadTrafficAccident.builder()
                .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the parties and uploaded to the Digital Portal by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build());
    }

    private FastTrackWitnessOfFact getFastTrackWitnessOfFact() {
        log.debug("Building FastTrackWitnessOfFact");
        return FastTrackWitnessOfFact.builder()
            .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely.")
            .input2("3")
            .input3("3")
            .input4("For this limitation, a party is counted as a witness.")
            .input5("Each witness statement should be no more than")
            .input6("10")
            .input7("A4 pages. Statements should be double spaced using a font size of 12.")
            .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input9("Evidence will not be permitted at trial from a witness whose statement has not been uploaded "
                        + "in accordance with this Order. Evidence not uploaded, or uploaded late, will not be "
                        + "permitted except with permission from the Court.")
            .build();
    }

    private FastTrackCreditHire getFastTrackCreditHire() {
        log.debug("Building FastTrackCreditHire");
        String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";
        return FastTrackCreditHire.builder()
            .input1("""
        If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's disclosure as ordered earlier in this Order must include:
        a) Evidence of all income from all sources for a period of 3 months prior to the commencement of hire until the earlier of:
              i) 3 months after cessation of hire
             ii) the repair or replacement of the claimant's vehicle
        b) Copies of all bank, credit card, and saving account statements for a period of 3 months prior to the commencement of hire until the earlier of:
             i) 3 months after cessation of hire
             ii) the repair or replacement of the claimant's vehicle
        c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""")
            .input2("""
        The claimant must upload to the Digital Portal a witness statement addressing
        a) the need to hire a replacement vehicle; and
        b) impecuniosity""")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from asserting need or relying on impecuniosity" +
                        " as the case may be at the final hearing, save with permission of the Trial Judge.")
            .input4(partiesLiaseString + LATER_THAN_FOUR_PM_STRING)
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above," +
                        " each party may rely upon written evidence by way of witness statement of one witness to provide evidence of" +
                        " basic hire rates available within the claimant's geographical location," +
                        " from a mainstream supplier, or a local reputable supplier if none is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input7(CLAIMANT_EVIDENCE_STRING)
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input8(WITNESS_STATEMENT_STRING)
            .build();
    }

    private SdoR2FastTrackCreditHire getSdoR2FastTrackCreditHire() {
        log.debug("Building SdoR2FastTrackCreditHire");
        List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
        return SdoR2FastTrackCreditHire.builder()
            .input1("""
            If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's \
            disclosure as ordered earlier in this Order must include:
            a) Evidence of all income from all sources for a period of 3 months prior to the \
            commencement of hire until the earlier of:

                 i) 3 months after cessation of hire
                 ii) the repair or replacement of the claimant's vehicle
            b) Copies of all bank, credit card, and saving account statements for a period of 3 months \
            prior to the commencement of hire until the earlier of:
                 i) 3 months after cessation of hire
                 ii) the repair or replacement of the claimant's vehicle
            c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""")
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above," +
                        " each party may rely upon written evidence by way of witness statement of" +
                        " one witness to provide evidence of basic hire rates available within the claimant's geographical location," +
                        " from a mainstream supplier, or a local reputable supplier if none is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input7(CLAIMANT_EVIDENCE_STRING)
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input8(WITNESS_STATEMENT_STRING)
            .detailsShowToggle(addOrRemoveToggleList)
            .sdoR2FastTrackCreditHireDetails(SdoR2FastTrackCreditHireDetails.builder()
                                                 .input2("""
                The claimant must upload to the Digital Portal a witness statement addressing
                a) the need to hire a replacement vehicle; and
                b) impecuniosity""")
                                                 .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                                                 .input3("A failure to comply with the paragraph above will result in the claimant being debarred" +
                                                             " from asserting need or relying on impecuniosity as the case may be at the final hearing," +
                                                             " save with permission of the Trial Judge.")
                                                 .input4("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no later than 4pm on")
                                                 .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                                                 .build())
            .build();
    }

    private static SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
        return SdoR2WitnessOfFact.builder()
            .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
            .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                      .isRestrictWitness(NO)
                                      .restrictNoOfWitnessDetails(
                                          SdoR2RestrictNoOfWitnessDetails.builder()
                                              .noOfWitnessClaimant(3)
                                              .noOfWitnessDefendant(3)
                                              .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                              .build())
                                      .build())
            .sdoRestrictPages(SdoR2RestrictPages.builder()
                                  .isRestrictPages(NO)
                                  .restrictNoOfPagesDetails(
                                      SdoR2RestrictNoOfPagesDetails.builder()
                                          .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                          .noOfPages(12)
                                          .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                          .build())
                                  .build())
            .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
            .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
            .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
            .build();
    }
}
