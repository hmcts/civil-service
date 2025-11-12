package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingDisclosureOfDocumentsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingMedicalEvidenceDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingQuestionsToExpertsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingSchedulesOfLossDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingWitnessOfFactDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;

@Service
@RequiredArgsConstructor
public class DjDisposalNarrativeService {

    private final DjDeadlineService deadlineService;

    public DisposalHearingJudgesRecitalDJ buildJudgesRecital(String judgeNameTitle) {
        return DisposalHearingJudgesRecitalDJ.builder()
            .judgeNameTitle(judgeNameTitle)
            .input(judgeNameTitle + ",")
            .build();
    }

    public DisposalHearingDisclosureOfDocumentsDJ buildDisclosureOfDocuments() {
        return DisposalHearingDisclosureOfDocumentsDJ.builder()
            .input("The parties shall serve on each other copies of the documents upon which "
                       + "reliance is to be placed at the disposal hearing by 4pm on")
            .date(deadlineService.nextWorkingDayInWeeks(4))
            .build();
    }

    public DisposalHearingWitnessOfFactDJ buildWitnessOfFact() {
        return DisposalHearingWitnessOfFactDJ.builder()
            .input1("The claimant must upload to the Digital Portal copies of "
                        + "the witness statements of all witnesses of fact on whose evidence reliance "
                        + "is to be placed by 4pm on ")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2("The provisions of CPR 32.6 apply to such evidence.")
            .input3("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
            .date2(deadlineService.nextWorkingDayInWeeks(2))
            .input4("and must be accompanied by proposed directions for allocation and listing for trial on "
                        + "quantum. This is because cross-examination will cause the hearing to exceed the "
                        + "30 minute maximum time estimate for a disposal hearing.")
            .build();
    }

    public DisposalHearingMedicalEvidenceDJ buildMedicalEvidence() {
        return DisposalHearingMedicalEvidenceDJ.builder()
            .input1("The claimant has permission to rely upon the written expert evidence already uploaded to"
                        + " the Digital Portal with the particulars of claim and in addition has permission to rely"
                        + " upon any associated correspondence or updating report which is uploaded to the"
                        + " Digital Portal by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .build();
    }

    public DisposalHearingQuestionsToExpertsDJ buildQuestionsToExperts() {
        return DisposalHearingQuestionsToExpertsDJ.builder()
            .date(deadlineService.nextWorkingDayInWeeks(6))
            .build();
    }

    public DisposalHearingSchedulesOfLossDJ buildSchedulesOfLoss() {
        return DisposalHearingSchedulesOfLossDJ.builder()
            .input1("If there is a claim for ongoing or future loss in the original schedule of losses then the"
                        + " claimant must send an up to date schedule of loss to the defendant by 4pm on the")
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input2("If the defendant wants to challenge this claim, they must send an up-to-date counter-schedule"
                        + " of loss to the claimant by 4pm on")
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .input3("If the defendant wants to challenge the sums claimed in the schedule of loss they must"
                        + " upload to the Digital Portal an updated counter schedule of loss by 4pm on")
            .date3(deadlineService.nextWorkingDayInWeeks(12))
            .inputText4("If there is a claim for future pecuniary loss and the parties have not already set out their"
                            + " case on periodical payments, they must do so in the respective schedule and "
                            + "counter-schedule.")
            .build();
    }

    public DisposalHearingFinalDisposalHearingDJ buildFinalDisposalHearing() {
        return DisposalHearingFinalDisposalHearingDJ.builder()
            .input("This claim will be listed for final disposal before a Judge on the first available date after")
            .date(deadlineService.weeksFromNow(16))
            .build();
    }

    public DisposalHearingFinalDisposalHearingTimeDJ buildFinalDisposalHearingTime() {
        return DisposalHearingFinalDisposalHearingTimeDJ.builder()
            .input("This claim will be listed for final disposal before a Judge on the first available date after")
            .date(deadlineService.weeksFromNow(16))
            .build();
    }

    public DisposalHearingBundleDJ buildBundle() {
        return DisposalHearingBundleDJ.builder()
            .input("At least 7 days before the disposal hearing, the claimant must file and serve")
            .build();
    }

    public DisposalHearingNotesDJ buildNotes() {
        return DisposalHearingNotesDJ.builder()
            .input("This order has been made without a hearing. Each party has the right to apply to have this "
                       + "order set aside or varied. Any such application must be uploaded to the Digital Portal together"
                       + " with payment of any appropriate fee, by 4pm on")
            .date(deadlineService.nextWorkingDayInWeeks(1))
            .build();
    }
}
