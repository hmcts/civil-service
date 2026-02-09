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

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_BUNDLE_REQUIREMENT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_EXCHANGE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_FINAL_HEARING_LISTING_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_JUDGES_RECITAL_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_SEND;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_6;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_7_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_TRIAL_NOTE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_SDO;

@Service
@RequiredArgsConstructor
public class SdoDisposalNarrativeService {

    private static final DateTimeFormatter DEADLINE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final SdoDeadlineService sdoDeadlineService;

    public void applyJudgesRecital(CaseData caseData) {
        caseData.setDisposalHearingJudgesRecital(new DisposalHearingJudgesRecital()
                                                    .setInput(DISPOSAL_JUDGES_RECITAL_CLAIM_FORM));
    }

    public void applyDisclosureOfDocuments(CaseData caseData) {
        caseData.setDisposalHearingDisclosureOfDocuments(new DisposalHearingDisclosureOfDocuments()
                                                             .setInput1(DISPOSAL_DOCUMENTS_EXCHANGE)
                                                             .setDate1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                                                             .setInput2(DISPOSAL_DOCUMENTS_UPLOAD)
                                                             .setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10)));
    }

    public void applyWitnessOfFact(CaseData caseData) {
        caseData.setDisposalHearingWitnessOfFact(new DisposalHearingWitnessOfFact()
                                                            .setInput3(DISPOSAL_WITNESS_UPLOAD)
                                                            .setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                                                            .setInput4(DISPOSAL_WITNESS_CPR32_6)
                                                            .setInput5(DISPOSAL_WITNESS_CPR32_7_DEADLINE)
                                                            .setDate3(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                                                            .setInput6(DISPOSAL_WITNESS_TRIAL_NOTE_SDO));
    }

    public void applyMedicalEvidence(CaseData caseData) {
        caseData.setDisposalHearingMedicalEvidence(new DisposalHearingMedicalEvidence()
                                                           .setInput(PERSONAL_INJURY_PERMISSION_SDO)
                                                           .setDate(sdoDeadlineService.nextWorkingDayFromNowWeeks(4)));
    }

    public void applyQuestionsToExperts(CaseData caseData) {
        caseData.setDisposalHearingQuestionsToExperts(new DisposalHearingQuestionsToExperts()
                                                               .setDate(sdoDeadlineService.nextWorkingDayFromNowWeeks(6)));
    }

    public void applySchedulesOfLoss(CaseData caseData) {
        caseData.setDisposalHearingSchedulesOfLoss(new DisposalHearingSchedulesOfLoss()
                                                              .setInput2(DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO)
                                                              .setDate2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                                                              .setInput3(DISPOSAL_SCHEDULE_COUNTER_SEND)
                                                              .setDate3(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                                                              .setInput4(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO)
                                                              .setDate4(sdoDeadlineService.nextWorkingDayFromNowWeeks(12)));
    }

    public void applyFinalDisposalHearing(CaseData caseData) {
        caseData.setDisposalHearingFinalDisposalHearing(new DisposalHearingFinalDisposalHearing()
                                                                       .setInput(DISPOSAL_FINAL_HEARING_LISTING_SDO)
                                                                       .setDate(LocalDate.now().plusWeeks(16)));
    }

    public void applyHearingTime(CaseData caseData) {
        caseData.setDisposalHearingHearingTime(new DisposalHearingHearingTime()
                                                       .setInput(DISPOSAL_FINAL_HEARING_LISTING_SDO)
                                                       .setDateTo(LocalDate.now().plusWeeks(16)));
    }

    public void applyOrderWithoutHearing(CaseData caseData) {
        caseData.setDisposalOrderWithoutHearing(new DisposalOrderWithoutHearing()
                                                          .setInput(String.format(
                                                              "%s %s.",
                                                              ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                                                              sdoDeadlineService.workingDaysFromNow(5).format(DEADLINE_FORMATTER)
                                                          )));
    }

    public void applyBundle(CaseData caseData) {
        caseData.setDisposalHearingBundle(new DisposalHearingBundle()
                                                .setInput(DISPOSAL_BUNDLE_REQUIREMENT));
    }

    public void applyNotes(CaseData caseData) {
        caseData.setDisposalHearingNotes(new DisposalHearingNotes()
                                              .setInput(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO)
                                              .setDate(sdoDeadlineService.nextWorkingDayFromNowWeeks(1)));
    }
}
