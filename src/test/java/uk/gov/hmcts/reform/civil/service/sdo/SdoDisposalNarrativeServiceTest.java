package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_BUNDLE_REQUIREMENT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_EXCHANGE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_UPLOAD;
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

@ExtendWith(MockitoExtension.class)
class SdoDisposalNarrativeServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoDisposalNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new SdoDisposalNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 4, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 5, 1)
                .plusDays(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateDisclosureAndWitnessSections() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyDisclosureOfDocuments(caseData);
        service.applyWitnessOfFact(caseData);

        assertThat(caseData.getDisposalHearingDisclosureOfDocuments().getDate1())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(10));
        assertThat(caseData.getDisposalHearingDisclosureOfDocuments().getInput1())
            .isEqualTo(DISPOSAL_DOCUMENTS_EXCHANGE);
        assertThat(caseData.getDisposalHearingDisclosureOfDocuments().getInput2())
            .isEqualTo(DISPOSAL_DOCUMENTS_UPLOAD);
        assertThat(caseData.getDisposalHearingWitnessOfFact().getDate2())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(4));
        assertThat(caseData.getDisposalHearingWitnessOfFact().getInput3())
            .isEqualTo(DISPOSAL_WITNESS_UPLOAD);
        assertThat(caseData.getDisposalHearingWitnessOfFact().getInput4())
            .isEqualTo(DISPOSAL_WITNESS_CPR32_6);
        assertThat(caseData.getDisposalHearingWitnessOfFact().getInput5())
            .isEqualTo(DISPOSAL_WITNESS_CPR32_7_DEADLINE);
        assertThat(caseData.getDisposalHearingWitnessOfFact().getInput6())
            .isEqualTo(DISPOSAL_WITNESS_TRIAL_NOTE_SDO);
    }

    @Test
    void shouldPopulateJudgesRecitalUsingSharedConstant() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyJudgesRecital(caseData);

        assertThat(caseData.getDisposalHearingJudgesRecital().getInput())
            .isEqualTo(DISPOSAL_JUDGES_RECITAL_CLAIM_FORM);
    }

    @Test
    void shouldPopulateOrderWithoutHearingAndNotes() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyOrderWithoutHearing(caseData);
        service.applyNotes(caseData);

        assertThat(caseData.getDisposalOrderWithoutHearing().getInput())
            .startsWith(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE);
        assertThat(caseData.getDisposalHearingNotes().getInput())
            .isEqualTo(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO);
        assertThat(caseData.getDisposalHearingNotes().getDate())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(1));
    }

    @Test
    void shouldPopulateSchedulesAndBundleUsingLibraryText() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.applySchedulesOfLoss(caseData);
        service.applyBundle(caseData);

        assertThat(caseData.getDisposalHearingSchedulesOfLoss().getInput2())
            .isEqualTo(DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO);
        assertThat(caseData.getDisposalHearingSchedulesOfLoss().getInput3())
            .isEqualTo(DISPOSAL_SCHEDULE_COUNTER_SEND);
        assertThat(caseData.getDisposalHearingSchedulesOfLoss().getInput4())
            .isEqualTo(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO);
        assertThat(caseData.getDisposalHearingBundle().getInput())
            .isEqualTo(DISPOSAL_BUNDLE_REQUIREMENT);
    }

    @Test
    void shouldPopulateHearingDatesUsingCurrentClock() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyFinalDisposalHearing(caseData);
        service.applyHearingTime(caseData);

        LocalDate expected = LocalDate.now().plusWeeks(16);
        assertThat(caseData.getDisposalHearingFinalDisposalHearing().getDate()).isEqualTo(expected);
        assertThat(caseData.getDisposalHearingHearingTime().getDateTo()).isEqualTo(expected);
    }
}
