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
        return DisposalHearingJudgesRecitalDJ.builder()
            .judgeNameTitle(judgeNameTitle)
            .input(judgeNameTitle + ",")
            .build();
    }

    public DisposalHearingDisclosureOfDocumentsDJ buildDisclosureOfDocuments() {
        return DisposalHearingDisclosureOfDocumentsDJ.builder()
            .input(DISPOSAL_DOCUMENTS_EXCHANGE)
            .date(deadlineService.nextWorkingDayInWeeks(4))
            .build();
    }

    public DisposalHearingWitnessOfFactDJ buildWitnessOfFact() {
        return DisposalHearingWitnessOfFactDJ.builder()
            .input1(DISPOSAL_WITNESS_UPLOAD + " ")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2(DISPOSAL_WITNESS_CPR32_6)
            .input3(DISPOSAL_WITNESS_CPR32_7_DEADLINE)
            .date2(deadlineService.nextWorkingDayInWeeks(2))
            .input4(DISPOSAL_WITNESS_TRIAL_NOTE_DJ)
            .build();
    }

    public DisposalHearingMedicalEvidenceDJ buildMedicalEvidence() {
        return DisposalHearingMedicalEvidenceDJ.builder()
            .input1(PERSONAL_INJURY_PERMISSION_DJ)
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
            .input1(DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ)
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input2(DISPOSAL_SCHEDULE_COUNTER_SEND)
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .input3(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ)
            .date3(deadlineService.nextWorkingDayInWeeks(12))
            .inputText4(DISPOSAL_SCHEDULE_FUTURE_LOSS)
            .build();
    }

    public DisposalHearingFinalDisposalHearingDJ buildFinalDisposalHearing() {
        return DisposalHearingFinalDisposalHearingDJ.builder()
            .input(DISPOSAL_FINAL_HEARING_LISTING_DJ)
            .date(deadlineService.weeksFromNow(16))
            .build();
    }

    public DisposalHearingFinalDisposalHearingTimeDJ buildFinalDisposalHearingTime() {
        return DisposalHearingFinalDisposalHearingTimeDJ.builder()
            .input(DISPOSAL_FINAL_HEARING_LISTING_DJ)
            .date(deadlineService.weeksFromNow(16))
            .build();
    }

    public DisposalHearingBundleDJ buildBundle() {
        return DisposalHearingBundleDJ.builder()
            .input(DISPOSAL_BUNDLE_REQUIREMENT)
            .build();
    }

    public DisposalHearingNotesDJ buildNotes() {
        return DisposalHearingNotesDJ.builder()
            .input(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_DJ)
            .date(deadlineService.nextWorkingDayInWeeks(1))
            .build();
    }
}
