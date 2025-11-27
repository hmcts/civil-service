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

/**
 * Encapsulates the fast-track narrative blocks so {@link SdoFastTrackOrderDefaultsService}
 * becomes a thin orchestrator that delegates to this service plus the specialist directions helper.
 */
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
        caseData.setFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                                                       .input(JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA)
                                                       .build());
    }

    private void applyDisclosureOfDocuments(CaseData caseData) {
        caseData.setFastTrackDisclosureOfDocuments(FastTrackDisclosureOfDocuments.builder()
                                                       .input1(FAST_TRACK_DISCLOSURE_STANDARD_SDO)
                                                       .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                                                       .input2(FAST_TRACK_DISCLOSURE_INSPECTION)
                                                       .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                                                       .input3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO)
                                                       .input4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX + " " + FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
                                                       .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
                                                       .build());
    }

    private void applyWitnessesOfFact(CaseData caseData) {
        caseData.setSdoR2FastTrackWitnessOfFact(buildWitnessesOfFact());
    }

    private void applySchedulesOfLoss(CaseData caseData) {
        caseData.setFastTrackSchedulesOfLoss(FastTrackSchedulesOfLoss.builder()
                                                         .input1(FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD)
                                                         .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                                                         .input2(FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD)
                                                         .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                                                         .input3(FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO)
                                                         .build());
    }

    private void applyTrial(CaseData caseData) {
        LocalDate trialStart = calendarWeeksFromNow(TRIAL_WINDOW_START_WEEKS);
        LocalDate trialEnd = calendarWeeksFromNow(TRIAL_WINDOW_END_WEEKS);
        caseData.setFastTrackTrial(FastTrackTrial.builder()
                                            .input1(FAST_TRACK_TRIAL_TIME_ALLOWED_SDO)
                                            .date1(trialStart)
                                            .date2(trialEnd)
                                            .input2(FAST_TRACK_TRIAL_TIME_WARNING_SDO)
                                            .input3(FAST_TRACK_TRIAL_BUNDLE_NOTICE)
                                            .type(Collections.singletonList(DOCUMENTS))
                                            .build());
    }

    private void applyHearingTime(CaseData caseData) {
        LocalDate trialStart = calendarWeeksFromNow(TRIAL_WINDOW_START_WEEKS);
        LocalDate trialEnd = calendarWeeksFromNow(TRIAL_WINDOW_END_WEEKS);
        caseData.setFastTrackHearingTime(FastTrackHearingTime.builder()
                                                 .dateFrom(trialStart)
                                                 .dateTo(trialEnd)
                                                 .dateToToggle(DATE_TO_SHOW_TRUE)
                                                 .helpText1(FAST_TRACK_TRIAL_HEARING_HELP_TEXT)
                                                 .build());
    }

    private void applyNotes(CaseData caseData) {
        caseData.setFastTrackNotes(FastTrackNotes.builder()
                                           .input(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF)
                                           .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(1))
                                           .build());
    }

    private void applyOrderWithoutHearing(CaseData caseData) {
        LocalDate deadline = sdoDeadlineService.orderSetAsideOrVariedApplicationDeadline(LocalDateTime.now());
        caseData.setFastTrackOrderWithoutJudgement(FastTrackOrderWithoutJudgement.builder()
                                                             .input(String.format(
                                                                 "%s %s.",
                                                                 ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                                                                 deadline.format(ORDER_DATE_FORMAT)
                                                             ))
                                                             .build());
    }

    private SdoR2WitnessOfFact buildWitnessesOfFact() {
        return SdoR2WitnessOfFact.builder()
            .sdoStatementOfWitness(STATEMENT_WITNESS)
            .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                      .isRestrictWitness(NO)
                                      .restrictNoOfWitnessDetails(
                                          SdoR2RestrictNoOfWitnessDetails.builder()
                                              .noOfWitnessClaimant(3)
                                              .noOfWitnessDefendant(3)
                                              .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                                              .build())
                                      .build())
            .sdoRestrictPages(SdoR2RestrictPages.builder()
                                  .isRestrictPages(NO)
                                  .restrictNoOfPagesDetails(
                                      SdoR2RestrictNoOfPagesDetails.builder()
                                          .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                                          .noOfPages(12)
                                          .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                                          .build())
                                  .build())
            .sdoWitnessDeadline(DEADLINE)
            .sdoWitnessDeadlineDate(sdoDeadlineService.calendarDaysFromNow(70))
            .sdoWitnessDeadlineText(DEADLINE_EVIDENCE)
            .build();
    }

    private LocalDate calendarWeeksFromNow(int weeks) {
        return sdoDeadlineService.calendarDaysFromNow(weeks * 7);
    }
}
