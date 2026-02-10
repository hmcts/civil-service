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

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_BUNDLE_REQUIREMENT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_EXCHANGE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_FINAL_HEARING_LISTING_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_SEND;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_FUTURE_LOSS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_6;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_7_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_TRIAL_NOTE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_DJ;

@Service
@RequiredArgsConstructor
public class DjDisposalNarrativeService {

    private final DjDeadlineService deadlineService;

    public DisposalHearingJudgesRecitalDJ buildJudgesRecital(String judgeNameTitle) {
        return new DisposalHearingJudgesRecitalDJ()
            .setJudgeNameTitle(judgeNameTitle)
            .setInput(judgeNameTitle + ",");
    }

    public DisposalHearingDisclosureOfDocumentsDJ buildDisclosureOfDocuments() {
        return new DisposalHearingDisclosureOfDocumentsDJ()
            .setInput(DISPOSAL_DOCUMENTS_EXCHANGE)
            .setDate(deadlineService.nextWorkingDayInWeeks(4));
    }

    public DisposalHearingWitnessOfFactDJ buildWitnessOfFact() {
        return new DisposalHearingWitnessOfFactDJ()
            .setInput1(DISPOSAL_WITNESS_UPLOAD + " ")
            .setDate1(deadlineService.nextWorkingDayInWeeks(4))
            .setInput2(DISPOSAL_WITNESS_CPR32_6)
            .setInput3(DISPOSAL_WITNESS_CPR32_7_DEADLINE)
            .setDate2(deadlineService.nextWorkingDayInWeeks(2))
            .setInput4(DISPOSAL_WITNESS_TRIAL_NOTE_DJ);
    }

    public DisposalHearingMedicalEvidenceDJ buildMedicalEvidence() {
        return new DisposalHearingMedicalEvidenceDJ()
            .setInput1(PERSONAL_INJURY_PERMISSION_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(4));
    }

    public DisposalHearingQuestionsToExpertsDJ buildQuestionsToExperts() {
        return new DisposalHearingQuestionsToExpertsDJ()
            .setDate(deadlineService.nextWorkingDayInWeeks(6));
    }

    public DisposalHearingSchedulesOfLossDJ buildSchedulesOfLoss() {
        return new DisposalHearingSchedulesOfLossDJ()
            .setInput1(DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(10))
            .setInput2(DISPOSAL_SCHEDULE_COUNTER_SEND)
            .setDate2(deadlineService.nextWorkingDayInWeeks(12))
            .setInput3(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ)
            .setDate3(deadlineService.nextWorkingDayInWeeks(12))
            .setInputText4(DISPOSAL_SCHEDULE_FUTURE_LOSS);
    }

    public DisposalHearingFinalDisposalHearingDJ buildFinalDisposalHearing() {
        return new DisposalHearingFinalDisposalHearingDJ()
            .setInput(DISPOSAL_FINAL_HEARING_LISTING_DJ)
            .setDate(deadlineService.weeksFromNow(16));
    }

    public DisposalHearingFinalDisposalHearingTimeDJ buildFinalDisposalHearingTime() {
        return new DisposalHearingFinalDisposalHearingTimeDJ()
            .setInput(DISPOSAL_FINAL_HEARING_LISTING_DJ)
            .setDate(deadlineService.weeksFromNow(16));
    }

    public DisposalHearingBundleDJ buildBundle() {
        return new DisposalHearingBundleDJ()
            .setInput(DISPOSAL_BUNDLE_REQUIREMENT);
    }

    public DisposalHearingNotesDJ buildNotes() {
        return new DisposalHearingNotesDJ()
            .setInput(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_DJ)
            .setDate(deadlineService.nextWorkingDayInWeeks(1));
    }
}
