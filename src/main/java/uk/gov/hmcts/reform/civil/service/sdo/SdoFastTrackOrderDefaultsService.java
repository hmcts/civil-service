package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
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
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;

@Service
@RequiredArgsConstructor
public class SdoFastTrackOrderDefaultsService {

    private static final List<DateToShowToggle> DATE_TO_SHOW_TRUE = List.of(DateToShowToggle.SHOW);

    private final SdoDeadlineService sdoDeadlineService;
    private final SdoFastTrackSpecialistDirectionsService specialistDirectionsService;

    public void populateFastTrackOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackJudgesRecital tempFastTrackJudgesRecital = FastTrackJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.fastTrackJudgesRecital(tempFastTrackJudgesRecital).build();

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1(FAST_TRACK_DISCLOSURE_STANDARD_SDO)
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2(FAST_TRACK_DISCLOSURE_INSPECTION)
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
            .input3(FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO)
            .input4(FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX + " " + FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE)
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
        updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact()).build();

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = FastTrackSchedulesOfLoss.builder()
            .input1(FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD)
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
            .input2(FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD)
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
            .input3(FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO)
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1(FAST_TRACK_TRIAL_TIME_ALLOWED_SDO)
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2(FAST_TRACK_TRIAL_TIME_WARNING_SDO)
            .input3(FAST_TRACK_TRIAL_BUNDLE_NOTICE)
            .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackHearingTime tempFastTrackHearingTime = FastTrackHearingTime.builder()
            .dateFrom(LocalDate.now().plusWeeks(22))
            .dateTo(LocalDate.now().plusWeeks(30))
            .dateToToggle(DATE_TO_SHOW_TRUE)
            .helpText1(FAST_TRACK_TRIAL_HEARING_HELP_TEXT)
            .build();
        updatedData.fastTrackHearingTime(tempFastTrackHearingTime);

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF)
            .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(1))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

        FastTrackOrderWithoutJudgement tempFastTrackOrderWithoutJudgement = FastTrackOrderWithoutJudgement.builder()
            .input(String.format(
                "%s %s.",
                ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                sdoDeadlineService.orderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ))
            .build();

        updatedData.fastTrackOrderWithoutJudgement(tempFastTrackOrderWithoutJudgement);
        specialistDirectionsService.populateSpecialistDirections(updatedData);
    }

    private SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
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
            .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
            .sdoWitnessDeadlineText(DEADLINE_EVIDENCE)
            .build();
    }
}
