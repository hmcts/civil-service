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

    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    private static final DateTimeFormatter DEADLINE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final SdoDeadlineService sdoDeadlineService;

    public void applyJudgesRecital(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingJudgesRecital(DisposalHearingJudgesRecital.builder()
                .input(UPON_CONSIDERING)
                .build())
            .build();
    }

    public void applyDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingDisclosureOfDocuments(DisposalHearingDisclosureOfDocuments.builder()
                .input1(DISPOSAL_DOCUMENTS_EXCHANGE)
                .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .input2(DISPOSAL_DOCUMENTS_UPLOAD)
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .build())
            .build();
    }

    public void applyWitnessOfFact(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingWitnessOfFact(DisposalHearingWitnessOfFact.builder()
                .input3(DISPOSAL_WITNESS_UPLOAD)
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                .input4(DISPOSAL_WITNESS_CPR32_6)
                .input5(DISPOSAL_WITNESS_CPR32_7_DEADLINE)
                .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                .input6(DISPOSAL_WITNESS_TRIAL_NOTE_SDO)
                .build())
            .build();
    }

    public void applyMedicalEvidence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingMedicalEvidence(DisposalHearingMedicalEvidence.builder()
                .input(PERSONAL_INJURY_PERMISSION_SDO)
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
                .build())
            .build();
    }

    public void applyQuestionsToExperts(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingQuestionsToExperts(DisposalHearingQuestionsToExperts.builder()
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(6))
                .build())
            .build();
    }

    public void applySchedulesOfLoss(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingSchedulesOfLoss(DisposalHearingSchedulesOfLoss.builder()
                .input2(DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO)
                .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(10))
                .input3(DISPOSAL_SCHEDULE_COUNTER_SEND)
                .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                .input4(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO)
                .date4(sdoDeadlineService.nextWorkingDayFromNowWeeks(12))
                .build())
            .build();
    }

    public void applyFinalDisposalHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingFinalDisposalHearing(DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build())
            .build();
    }

    public void applyHearingTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingHearingTime(DisposalHearingHearingTime.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .dateTo(LocalDate.now().plusWeeks(16))
                .build())
            .build();
    }

    public void applyOrderWithoutHearing(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalOrderWithoutHearing(DisposalOrderWithoutHearing.builder()
                .input(String.format(
                    "%s %s.",
                    ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE,
                    sdoDeadlineService.workingDaysFromNow(5).format(DEADLINE_FORMATTER)
                ))
                .build())
            .build();
    }

    public void applyBundle(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingBundle(DisposalHearingBundle.builder()
                .input(DISPOSAL_BUNDLE_REQUIREMENT)
                .build())
            .build();
    }

    public void applyNotes(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.disposalHearingNotes(DisposalHearingNotes.builder()
                .input(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO)
                .date(sdoDeadlineService.nextWorkingDayFromNowWeeks(1))
                .build())
            .build();
    }
}
