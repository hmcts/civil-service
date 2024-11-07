package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisposalHearingPopulator {

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final List<SdoCaseFieldBuilder> disposalHearingBuilders;

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                log.info("Updating deduction value to {}", deductionPercentage);
                DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
            });
    }

    public void setDisposalHearingFields(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.info("Setting disposal hearing fields for case data");
        disposalHearingBuilders.forEach(disposalHearingBuilder -> disposalHearingBuilder.build(updatedData));
        updateDeductionValue(caseData, updatedData);
        setDisclosureOfDocuments(updatedData);
        setWitnessOfFact(updatedData);
        setMedicalEvidence(updatedData);
        setQuestionsToExperts(updatedData);
        setSchedulesOfLoss(updatedData);
        setFinalDisposalHearing(updatedData);
        setHearingTime(updatedData);
        setOrderWithoutHearing(updatedData);
        setHearingBundle(updatedData);
        setHearingNotes(updatedData);
    }

    private void setDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting disclosure of documents");
        updatedData.disposalHearingDisclosureOfDocuments(
            DisposalHearingDisclosureOfDocuments.builder()
                .input1("The parties shall serve on each other copies of the documents upon "
                            + "which reliance is to be placed at the disposal hearing by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input2("The parties must upload to the Digital Portal copies of those documents "
                            + "which they wish the court to consider when deciding the amount of damages, "
                            + "by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .build());
    }

    private void setWitnessOfFact(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting witness of fact");
        updatedData.disposalHearingWitnessOfFact(DisposalHearingWitnessOfFact.builder()
                                                     .input3("The claimant must upload to the Digital Portal copies of the witness statements "
                                                                 + "of all witnesses of fact on whose evidence reliance is to be placed by 4pm on")
                                                     .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                                                     .input4("The provisions of CPR 32.6 apply to such evidence.")
                                                     .input5("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
                                                     .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                                                     .input6("and must be accompanied by proposed directions for allocation and listing for "
                                                                 + "trial on quantum. This is because cross-examination will cause the hearing to "
                                                                 + "exceed the 30-minute maximum time estimate for a disposal hearing.")
                                                     .build());
    }

    private void setMedicalEvidence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting medical evidence");
        updatedData.disposalHearingMedicalEvidence(DisposalHearingMedicalEvidence.builder()
                                                       .input("The claimant has permission to rely upon the written expert evidence already "
                                                                  + "uploaded to the Digital Portal with the particulars of claim and in addition has "
                                                                  + "permission to rely upon any associated correspondence or updating report which "
                                                                  + "is uploaded to the Digital Portal by 4pm on")
                                                       .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                                                       .build());
    }

    private void setQuestionsToExperts(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting questions to experts");
        updatedData.disposalHearingQuestionsToExperts(
            DisposalHearingQuestionsToExperts.builder()
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .build());
    }

    private void setSchedulesOfLoss(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting schedules of loss");
        updatedData.disposalHearingSchedulesOfLoss(DisposalHearingSchedulesOfLoss.builder()
                                                       .input2("If there is a claim for ongoing or future loss in the original schedule of losses, "
                                                                   + "the claimant must upload to the Digital Portal an up-to-date schedule of loss by "
                                                                   + "4pm on")
                                                       .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                                                       .input3("If the defendant wants to challenge this claim, they must send an up-to-date "
                                                                   + "counter-schedule of loss to the claimant by 4pm on")
                                                       .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                                                       .input4("If the defendant want to challenge the sums claimed in the schedule of loss they "
                                                                   + "must upload to the Digital Portal an updated counter schedule of loss by 4pm on")
                                                       .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
                                                       .build());
    }

    private void setFinalDisposalHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting final disposal hearing");
        updatedData.disposalHearingFinalDisposalHearing(
            DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first "
                           + "available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build());
    }

    private void setHearingTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing time");
        updatedData.disposalHearingHearingTime(DisposalHearingHearingTime.builder()
                                                   .input("This claim will be listed for final disposal before a judge on the first "
                                                              + "available date after")
                                                   .dateTo(LocalDate.now().plusWeeks(16))
                                                   .build());
    }

    private void setOrderWithoutHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting order without hearing");
        updatedData.disposalOrderWithoutHearing(DisposalOrderWithoutHearing.builder()
                                                    .input(String.format("This order has been made without hearing. Each party has the right "
                                                                             + "to apply to have this Order set aside or varied. Any such application must be "
                                                                             + "received by the Court (together with the appropriate fee) by 4pm on %s.",
                                                                         deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                                                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))))
                                                    .build());
    }

    private void setHearingBundle(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing bundle");
        updatedData.disposalHearingBundle(DisposalHearingBundle.builder()
                                              .input("At least 7 days before the disposal hearing, the claimant must file and serve")
                                              .build());
    }

    private void setHearingNotes(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing notes");
        updatedData.disposalHearingNotes(DisposalHearingNotes.builder()
                                             .input("This Order has been made without a hearing. Each party has the right to apply to "
                                                        + "have this Order set aside or varied. Any such application must be uploaded to the "
                                                        + "Digital Portal together with the appropriate fee, by 4pm on")
                                             .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
                                             .build());
    }
}
