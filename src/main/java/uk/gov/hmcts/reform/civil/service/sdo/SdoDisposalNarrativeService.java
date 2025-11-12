package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SdoDisposalNarrativeService {

    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    private static final DateTimeFormatter DEADLINE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final SdoDeadlineService sdoDeadlineService;

    public void applyJudgesRecital(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingJudgesRecital(DisposalHearingJudgesRecital.builder()
                .input(UPON_CONSIDERING)
                .build())
            .build();
    }

    public void applyDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingDisclosureOfDocuments(DisposalHearingDisclosureOfDocuments.builder()
                .input1("The parties shall serve on each other copies of the documents upon which reliance is to be"
                            + " placed at the disposal hearing by 4pm on")
                .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .input2("The parties must upload to the Digital Portal copies of those documents which they wish the "
                            + "court to consider when deciding the amount of damages, by 4pm on")
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .build())
            .build();
    }

    public void applyWitnessOfFact(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingWitnessOfFact(DisposalHearingWitnessOfFact.builder()
                .input3("The claimant must upload to the Digital Portal copies of the witness statements of all "
                            + "witnesses of fact on whose evidence reliance is to be placed by 4pm on")
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                .input4("The provisions of CPR 32.6 apply to such evidence.")
                .input5("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
                .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                .input6("and must be accompanied by proposed directions for allocation and listing for trial on quantum."
                            + " This is because cross-examination will cause the hearing to exceed the 30-minute maximum"
                            + " time estimate for a disposal hearing.")
                .build())
            .build();
    }

    public void applyMedicalEvidence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingMedicalEvidence(DisposalHearingMedicalEvidence.builder()
                .input("The claimant has permission to rely upon the written expert evidence already uploaded to the"
                           + " Digital Portal with the particulars of claim and in addition has permission to rely upon"
                           + " any associated correspondence or updating report which is uploaded to the Digital Portal"
                           + " by 4pm on")
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                .build())
            .build();
    }

    public void applyQuestionsToExperts(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingQuestionsToExperts(DisposalHearingQuestionsToExperts.builder()
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                .build())
            .build();
    }

    public void applySchedulesOfLoss(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingSchedulesOfLoss(DisposalHearingSchedulesOfLoss.builder()
                .input2("If there is a claim for ongoing or future loss in the original schedule of losses, the "
                            + "claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .input3("If the defendant wants to challenge this claim, they must send an up-to-date counter-schedule"
                            + " of loss to the claimant by 4pm on")
                .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                .input4("If the defendant want to challenge the sums claimed in the schedule of loss they must upload"
                            + " to the Digital Portal an updated counter schedule of loss by 4pm on")
                .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                .build())
            .build();
    }

    public void applyFinalDisposalHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingFinalDisposalHearing(DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build())
            .build();
    }

    public void applyHearingTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingHearingTime(DisposalHearingHearingTime.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .dateTo(LocalDate.now().plusWeeks(16))
                .build())
            .build();
    }

    public void applyOrderWithoutHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalOrderWithoutHearing(DisposalOrderWithoutHearing.builder()
                .input(String.format(
                    "This order has been made without hearing. Each party has the right to apply to have this Order set "
                        + "aside or varied. Any such application must be received by the Court (together with the "
                        + "appropriate fee) by 4pm on %s.",
                    sdoDeadlineService.workingDaysFromNow(5).format(DEADLINE_FORMATTER)
                ))
                .build())
            .build();
    }

    public void applyBundle(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingBundle(DisposalHearingBundle.builder()
                .input("At least 7 days before the disposal hearing, the claimant must file and serve")
                .build())
            .build();
    }

    public void applyNotes(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingNotes(DisposalHearingNotes.builder()
                .input("This Order has been made without a hearing. Each party has the right to apply to have this "
                           + "Order set aside or varied. Any such application must be uploaded to the Digital Portal "
                           + "together with the appropriate fee, by 4pm on")
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(1))
                .build())
            .build();
    }
}
