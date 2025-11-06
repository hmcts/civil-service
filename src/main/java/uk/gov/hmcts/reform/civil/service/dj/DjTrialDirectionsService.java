package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DjTrialDirectionsService {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final DjSpecialistDirectionsService specialistDirectionsService;
    private final DjWelshLanguageService welshLanguageService;

    public void populateTrialDirections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String judgeNameTitle) {
        caseDataBuilder
            .trialHearingJudgesRecitalDJ(TrialHearingJudgesRecital
                                             .builder()
                                             .judgeNameTitle(judgeNameTitle)
                                             .input(judgeNameTitle + ","
                                             ).build());

        caseDataBuilder
            .trialHearingDisclosureOfDocumentsDJ(TrialHearingDisclosureOfDocuments
                                                     .builder()
                                                     .input1("Standard disclosure shall be provided by "
                                                                 + "the parties by uploading to the digital "
                                                                 + "portal their lists of documents by 4pm on")
                                                     .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                                                     .input2("Any request to inspect a document, or for a copy of a "
                                                                 + "document, shall be made directly to the other"
                                                                 + " party by 4pm on")
                                                     .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                                                     .input3("Requests will be complied with within 7 days of the"
                                                                 + " receipt of the request")
                                                     .input4("Each party must upload to the Digital Portal"
                                                                 + " copies of those documents on which they wish to rely"
                                                                 + " at trial")
                                                     .input5("by 4pm on")
                                                     .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                                                     .build());

        caseDataBuilder
            .trialHearingWitnessOfFactDJ(TrialHearingWitnessOfFact
                                             .builder()
                                             .input1("Each party must upload to the Digital Portal copies of the "
                                                         + "statements of all witnesses of fact on whom they "
                                                         + "intend to rely.")
                                             .input2("3")
                                             .input3("3")
                                             .input4("For this limitation, a party is counted as witness.")
                                             .input5("Each witness statement should be no more than")
                                             .input6("10")
                                             .input7("A4 pages. Statements should be double spaced "
                                                         + "using a font size of 12.")
                                             .input8("Witness statements shall be uploaded to the "
                                                         + "Digital Portal by 4pm on")
                                             .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                                             .input9("Evidence will not be permitted at trial from a witness whose "
                                                         + "statement has not been uploaded in accordance with this"
                                                         + " Order. Evidence not uploaded, or uploaded late, will not "
                                                         + "be permitted except with permission from the Court")
                                             .build());

        caseDataBuilder
            .trialHearingSchedulesOfLossDJ(TrialHearingSchedulesOfLoss
                                               .builder()
                                               .input1("The claimant must upload to the Digital Portal an "
                                                           + "up-to-date schedule of loss by 4pm on")
                                               .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                                               .input2("If the defendant wants to challenge this claim, "
                                                           + "upload to the Digital Portal counter-schedule"
                                                           + " of loss by 4pm on")
                                               .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                                               .input3("If there is a claim for future pecuniary loss and the parties"
                                                           + " have not already set out their "
                                                           + "case on periodical payments. "
                                                           + "then they must do so in the respective schedule "
                                                           + "and counter-schedule")
                                               .build());

        caseDataBuilder.trialHearingTrialDJ(TrialHearingTrial
                                                .builder()
                                                .input1("The time provisionally allowed for the trial is")
                                                .date1(LocalDate.now().plusWeeks(22))
                                                .date2(LocalDate.now().plusWeeks(34))
                                                .input2("If either party considers that the time estimates is"
                                                            + " insufficient, they must inform the court within "
                                                            + "7 days of the date of this order.")
                                                .input3("At least 7 days before the trial, the claimant must"
                                                            + " upload to the Digital Portal ")
                                                .build());

        List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
        // copy of above method as to not break existing cases
        caseDataBuilder.trialHearingTimeDJ(TrialHearingTimeDJ.builder()
                                               .helpText1(
                                                   "If either party considers that the time estimate is insufficient, "
                                                       + "they must inform the court within 7 days of the date of "
                                                       + "this order.")
                                               .helpText2(
                                                   "Not more than seven nor less than three clear days before the "
                                                       + "trial, the claimant must file at court and serve an indexed "
                                                       + "and paginated bundle of documents which complies with the "
                                                       + "requirements of Rule 39.5 Civil Procedure Rules "
                                                       + "and which complies with requirements of PD32. The parties "
                                                       + "must endeavour to agree the contents of the bundle before it "
                                                       + "is filed. The bundle will include a case summary and a "
                                                       + "chronology.")
                                               .dateToToggle(dateToShowTrue)
                                               .date1(LocalDate.now().plusWeeks(22))
                                               .date2(LocalDate.now().plusWeeks(30))
                                               .build());

        LocalDate trialOrderDeadline = deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5);
        caseDataBuilder.trialOrderMadeWithoutHearingDJ(
            TrialOrderMadeWithoutHearingDJ.builder()
                .input(welshLanguageService.buildOrderMadeWithoutHearingText(trialOrderDeadline))
                .build());

        caseDataBuilder.trialHearingNotesDJ(TrialHearingNotes
                                                .builder()
                                                .input("This order has been made without a hearing. Each party has "
                                                           + "the right to apply to have this order set "
                                                           + "aside or varied."
                                                           + " Any such application must be received by the court "
                                                           + "(together with the appropriate fee) by 4pm on")
                                                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
                                                .build());

        specialistDirectionsService.populateSpecialistDirections(caseDataBuilder);

        caseDataBuilder.sdoR2TrialWelshLanguageDJ(
            welshLanguageService.buildWelshUsage());

        updateDisclosureOfDocumentFields(caseDataBuilder);
    }

    private void updateDisclosureOfDocumentFields(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        caseDataBuilder
            .trialHearingDisclosureOfDocumentsDJ(TrialHearingDisclosureOfDocuments
                                                     .builder()
                                                     .input1("Standard disclosure shall be provided by "
                                                                 + "the parties by uploading to the digital "
                                                                 + "portal their lists of documents by 4pm on")
                                                     .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                                                     .input2("Any request to inspect a document, or for a copy of a "
                                                                 + "document, shall be made directly to the other"
                                                                 + " party by 4pm on")
                                                     .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)))
                                                     .input3("Requests will be complied with within 7 days of the"
                                                                 + " receipt of the request")
                                                     .input4("Each party must upload to the Digital Portal"
                                                                 + " copies of those documents on which they wish to rely"
                                                                 + " at trial")
                                                     .input5("by 4pm on")
                                                     .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                                                     .build());
    }
}
