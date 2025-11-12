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
        return TrialHearingJudgesRecital.builder()
            .judgeNameTitle(judgeNameTitle)
            .input(judgeNameTitle + ",")
            .build();
    }

    public TrialHearingDisclosureOfDocuments buildDisclosureOfDocuments() {
        return TrialHearingDisclosureOfDocuments.builder()
            .input1(FAST_TRACK_DISCLOSURE_STANDARD_DJ)
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .date2(deadlineService.nextWorkingDayInWeeks(6))
            .input3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ)
            .input4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX)
            .input5(FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .date3(deadlineService.nextWorkingDayInWeeks(8))
            .build();
    }

    public TrialHearingDisclosureOfDocuments buildUpdatedDisclosureOfDocuments() {
        return TrialHearingDisclosureOfDocuments.builder()
            .input1(FAST_TRACK_DISCLOSURE_STANDARD_DJ)
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .date2(deadlineService.nextWorkingDayInWeeks(5))
            .input3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ)
            .input4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX)
            .input5(FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .date3(deadlineService.nextWorkingDayInWeeks(8))
            .build();
    }

    public TrialHearingWitnessOfFact buildWitnessOfFact() {
        return TrialHearingWitnessOfFact.builder()
            .input1(TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE)
            .input2("3")
            .input3("3")
            .input4(WITNESS_COUNT_LIMIT_NOTE_DJ)
            .input5(WITNESS_PAGE_LIMIT_PREFIX)
            .input6("10")
            .input7(WITNESS_PAGE_LIMIT_SUFFIX_DJ)
            .input8(SMALL_CLAIMS_WITNESS_DEADLINE)
            .date1(deadlineService.nextWorkingDayInWeeks(8))
            .input9(SMALL_CLAIMS_WITNESS_LATE_WARNING)
            .build();
    }

    public TrialHearingSchedulesOfLoss buildSchedulesOfLoss() {
        return TrialHearingSchedulesOfLoss.builder()
            .input1(FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD)
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input2(FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD)
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .input3(FAST_TRACK_SCHEDULE_FUTURE_LOSS_DJ)
            .build();
    }

    public TrialHearingTrial buildTrialHearingTrial() {
        return TrialHearingTrial.builder()
            .input1(FAST_TRACK_TRIAL_TIME_ALLOWED_DJ)
            .date1(deadlineService.weeksFromNow(22))
            .date2(deadlineService.weeksFromNow(34))
            .input2(FAST_TRACK_TRIAL_TIME_WARNING_DJ)
            .input3(FAST_TRACK_TRIAL_BUNDLE_NOTICE + " ")
            .build();
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
        return TrialHearingNotes.builder()
            .input(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_LOWERCASE)
            .date(deadlineService.nextWorkingDayInWeeks(1))
            .build();
    }
}
