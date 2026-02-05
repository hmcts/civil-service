package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_FUTURE_LOSS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_BUNDLE_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_HEARING_HELP_TEXT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_MANUAL_BUNDLE_GUIDANCE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_TIME_ALLOWED_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_TIME_WARNING_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_LOWERCASE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_LATE_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_COUNT_LIMIT_NOTE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_PAGE_LIMIT_PREFIX;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_PAGE_LIMIT_SUFFIX_DJ;

@Service
@RequiredArgsConstructor
public class DjTrialNarrativeService {

    private static final List<DateToShowToggle> DATE_TO_SHOW = List.of(DateToShowToggle.SHOW);

    private final DjDeadlineService deadlineService;

    public TrialHearingJudgesRecital buildJudgesRecital(String judgeNameTitle) {
        return new TrialHearingJudgesRecital()
            .setJudgeNameTitle(judgeNameTitle)
            .setInput(judgeNameTitle + ",");
    }

    public TrialHearingDisclosureOfDocuments buildDisclosureOfDocuments() {
        return new TrialHearingDisclosureOfDocuments()
            .setInput1(FAST_TRACK_DISCLOSURE_STANDARD_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(4))
            .setInput2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .setDate2(deadlineService.nextWorkingDayInWeeks(6))
            .setInput3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ)
            .setInput4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX)
            .setInput5(FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .setDate3(deadlineService.nextWorkingDayInWeeks(8));
    }

    public TrialHearingDisclosureOfDocuments buildUpdatedDisclosureOfDocuments() {
        return new TrialHearingDisclosureOfDocuments()
            .setInput1(FAST_TRACK_DISCLOSURE_STANDARD_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(4))
            .setInput2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .setDate2(deadlineService.nextWorkingDayInWeeks(5))
            .setInput3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ)
            .setInput4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX)
            .setInput5(FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .setDate3(deadlineService.nextWorkingDayInWeeks(8));
    }

    public TrialHearingWitnessOfFact buildWitnessOfFact() {
        return new TrialHearingWitnessOfFact()
            .setInput1(TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE)
            .setInput2("3")
            .setInput3("3")
            .setInput4(WITNESS_COUNT_LIMIT_NOTE_DJ)
            .setInput5(WITNESS_PAGE_LIMIT_PREFIX)
            .setInput6("10")
            .setInput7(WITNESS_PAGE_LIMIT_SUFFIX_DJ)
            .setInput8(SMALL_CLAIMS_WITNESS_DEADLINE)
            .setDate1(deadlineService.nextWorkingDayInWeeks(8))
            .setInput9(SMALL_CLAIMS_WITNESS_LATE_WARNING);
    }

    public TrialHearingSchedulesOfLoss buildSchedulesOfLoss() {
        return new TrialHearingSchedulesOfLoss()
            .setInput1(FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD)
            .setDate1(deadlineService.nextWorkingDayInWeeks(10))
            .setInput2(FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD)
            .setDate2(deadlineService.nextWorkingDayInWeeks(12))
            .setInput3(FAST_TRACK_SCHEDULE_FUTURE_LOSS_DJ);
    }

    public TrialHearingTrial buildTrialHearingTrial() {
        return new TrialHearingTrial()
            .setInput1(FAST_TRACK_TRIAL_TIME_ALLOWED_DJ)
            .setDate1(deadlineService.weeksFromNow(22))
            .setDate2(deadlineService.weeksFromNow(34))
            .setInput2(FAST_TRACK_TRIAL_TIME_WARNING_DJ)
            .setInput3(FAST_TRACK_TRIAL_BUNDLE_NOTICE + " ");
    }

    public TrialHearingTimeDJ buildTrialHearingTime() {
        return TrialHearingTimeDJ.builder()
            .helpText1(FAST_TRACK_TRIAL_HEARING_HELP_TEXT)
            .helpText2(FAST_TRACK_TRIAL_MANUAL_BUNDLE_GUIDANCE)
            .dateToToggle(DATE_TO_SHOW)
            .date1(deadlineService.weeksFromNow(22))
            .date2(deadlineService.weeksFromNow(30))
            .build();
    }

    public TrialHearingNotes buildTrialHearingNotes() {
        return new TrialHearingNotes()
            .setInput(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_LOWERCASE)
            .setDate(deadlineService.nextWorkingDayInWeeks(1));
    }
}
