package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
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
import static uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType.DOCUMENTS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_BUNDLE_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_HEARING_HELP_TEXT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_TIME_ALLOWED_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_TIME_WARNING_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;

@Service
@RequiredArgsConstructor
public class SdoFastTrackNarrativeService {

    private static final List<DateToShowToggle> DATE_TO_SHOW_TRUE = List.of(DateToShowToggle.SHOW);
    private static final DateTimeFormatter ORDER_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final int TRIAL_WINDOW_START_WEEKS = 22;
    private static final int TRIAL_WINDOW_END_WEEKS = 30;

    private final SdoDeadlineService sdoDeadlineService;

    public void populateFastTrackNarrative(CaseData caseData) {
        applyJudgesRecital(caseData);
        applyDisclosureOfDocuments(caseData);
        applyWitnessesOfFact(caseData);
        applySchedulesOfLoss(caseData);
        applyTrial(caseData);
        applyHearingTime(caseData);
        applyNotes(caseData);
        applyOrderWithoutHearing(caseData);
    }

    private void applyJudgesRecital(CaseData caseData) {
        caseData.setFastTrackJudgesRecital((new FastTrackJudgesRecital())
                                                       .setInput(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA));
    }

    private void applyDisclosureOfDocuments(CaseData caseData) {
        caseData.setFastTrackDisclosureOfDocuments((new FastTrackDisclosureOfDocuments())
                                                       .setInput1(FAST_TRACK_DISCLOSURE_STANDARD_SDO)
                                                       .setDate1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                                                       .setInput2(FAST_TRACK_DISCLOSURE_INSPECTION)
                                                       .setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                                                       .setInput3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO)
                                                       .setInput4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX + " " + FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
                                                       .setDate3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8)));
    }

    private void applyWitnessesOfFact(CaseData caseData) {
        caseData.setSdoR2FastTrackWitnessOfFact(buildWitnessesOfFact());
    }

    private void applySchedulesOfLoss(CaseData caseData) {
        caseData.setFastTrackSchedulesOfLoss(new FastTrackSchedulesOfLoss()
                                                         .setInput1(FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD)
                                                         .setDate1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                                                         .setInput2(FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD)
                                                         .setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                                                         .setInput3(FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO));
    }

    private void applyTrial(CaseData caseData) {
        LocalDate trialStart = calendarWeeksFromNow(TRIAL_WINDOW_START_WEEKS);
        LocalDate trialEnd = calendarWeeksFromNow(TRIAL_WINDOW_END_WEEKS);
        caseData.setFastTrackTrial(new FastTrackTrial()
                                            .setInput1(FAST_TRACK_TRIAL_TIME_ALLOWED_SDO)
                                            .setDate1(trialStart)
                                            .setDate2(trialEnd)
                                            .setInput2(FAST_TRACK_TRIAL_TIME_WARNING_SDO)
                                            .setInput3(FAST_TRACK_TRIAL_BUNDLE_NOTICE)
                                            .setType(Collections.singletonList(DOCUMENTS)));
    }

    private void applyHearingTime(CaseData caseData) {
        LocalDate trialStart = calendarWeeksFromNow(TRIAL_WINDOW_START_WEEKS);
        LocalDate trialEnd = calendarWeeksFromNow(TRIAL_WINDOW_END_WEEKS);
        caseData.setFastTrackHearingTime(new FastTrackHearingTime()
                                                 .setDateFrom(trialStart)
                                                 .setDateTo(trialEnd)
                                                 .setDateToToggle(DATE_TO_SHOW_TRUE)
                                                 .setHelpText1(FAST_TRACK_TRIAL_HEARING_HELP_TEXT));
    }

    private void applyNotes(CaseData caseData) {
        caseData.setFastTrackNotes((new FastTrackNotes())
                                           .setInput(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF)
                                           .setDate(sdoDeadlineService.nextWorkingDayFromNowWeeks(1)));
    }

    private void applyOrderWithoutHearing(CaseData caseData) {
        LocalDate deadline = sdoDeadlineService.orderSetAsideOrVariedApplicationDeadline(LocalDateTime.now());
        caseData.setFastTrackOrderWithoutJudgement((new FastTrackOrderWithoutJudgement())
                                                             .setInput(String.format(
                                                                 "%s %s.",
                                                                 ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                                                                 deadline.format(ORDER_DATE_FORMAT)
                                                             )));
    }

    private SdoR2WitnessOfFact buildWitnessesOfFact() {
        SdoR2RestrictNoOfWitnessDetails restrictWitnessDetails = new SdoR2RestrictNoOfWitnessDetails();
        restrictWitnessDetails.setNoOfWitnessClaimant(3);
        restrictWitnessDetails.setNoOfWitnessDefendant(3);
        restrictWitnessDetails.setPartyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT);

        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setIsRestrictWitness(NO);
        restrictWitness.setRestrictNoOfWitnessDetails(restrictWitnessDetails);

        SdoR2RestrictNoOfPagesDetails restrictPagesDetails = new SdoR2RestrictNoOfPagesDetails();
        restrictPagesDetails.setWitnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1);
        restrictPagesDetails.setNoOfPages(12);
        restrictPagesDetails.setFontDetails(RESTRICT_NUMBER_PAGES_TEXT2);

        SdoR2RestrictPages restrictPages = new SdoR2RestrictPages();
        restrictPages.setIsRestrictPages(NO);
        restrictPages.setRestrictNoOfPagesDetails(restrictPagesDetails);

        SdoR2WitnessOfFact witnessOfFact = new SdoR2WitnessOfFact();
        witnessOfFact.setSdoStatementOfWitness(STATEMENT_WITNESS);
        witnessOfFact.setSdoR2RestrictWitness(restrictWitness);
        witnessOfFact.setSdoRestrictPages(restrictPages);
        witnessOfFact.setSdoWitnessDeadline(DEADLINE);
        witnessOfFact.setSdoWitnessDeadlineDate(sdoDeadlineService.calendarDaysFromNow(70));
        witnessOfFact.setSdoWitnessDeadlineText(DEADLINE_EVIDENCE);
        return witnessOfFact;
    }

    private LocalDate calendarWeeksFromNow(int weeks) {
        return sdoDeadlineService.calendarDaysFromNow(weeks * 7);
    }
}
