package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

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
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyDisclosureOfDocuments(builder);
        service.applyWitnessOfFact(builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalHearingDisclosureOfDocuments().getDate1())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(10));
        assertThat(result.getDisposalHearingDisclosureOfDocuments().getInput1())
            .isEqualTo(DISPOSAL_DOCUMENTS_EXCHANGE);
        assertThat(result.getDisposalHearingDisclosureOfDocuments().getInput2())
            .isEqualTo(DISPOSAL_DOCUMENTS_UPLOAD);
        assertThat(result.getDisposalHearingWitnessOfFact().getDate2())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(4));
        assertThat(result.getDisposalHearingWitnessOfFact().getInput3())
            .isEqualTo(DISPOSAL_WITNESS_UPLOAD);
        assertThat(result.getDisposalHearingWitnessOfFact().getInput4())
            .isEqualTo(DISPOSAL_WITNESS_CPR32_6);
        assertThat(result.getDisposalHearingWitnessOfFact().getInput5())
            .isEqualTo(DISPOSAL_WITNESS_CPR32_7_DEADLINE);
        assertThat(result.getDisposalHearingWitnessOfFact().getInput6())
            .isEqualTo(DISPOSAL_WITNESS_TRIAL_NOTE_SDO);
    }

    @Test
    void shouldPopulateJudgesRecitalUsingSharedConstant() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyJudgesRecital(builder);

        assertThat(builder.build().getDisposalHearingJudgesRecital().getInput())
            .isEqualTo(DISPOSAL_JUDGES_RECITAL_CLAIM_FORM);
    }

    @Test
    void shouldPopulateOrderWithoutHearingAndNotes() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyOrderWithoutHearing(builder);
        service.applyNotes(builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalOrderWithoutHearing().getInput())
            .startsWith(ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE);
        assertThat(result.getDisposalHearingNotes().getInput())
            .isEqualTo(ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO);
        assertThat(result.getDisposalHearingNotes().getDate())
            .isEqualTo(LocalDate.of(2025, 4, 1).plusWeeks(1));
    }

    @Test
    void shouldPopulateSchedulesAndBundleUsingLibraryText() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applySchedulesOfLoss(builder);
        service.applyBundle(builder);

        CaseData result = builder.build();
        assertThat(result.getDisposalHearingSchedulesOfLoss().getInput2())
            .isEqualTo(DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO);
        assertThat(result.getDisposalHearingSchedulesOfLoss().getInput3())
            .isEqualTo(DISPOSAL_SCHEDULE_COUNTER_SEND);
        assertThat(result.getDisposalHearingSchedulesOfLoss().getInput4())
            .isEqualTo(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO);
        assertThat(result.getDisposalHearingBundle().getInput())
            .isEqualTo(DISPOSAL_BUNDLE_REQUIREMENT);
    }

    @Test
    void shouldPopulateHearingDatesUsingCurrentClock() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.applyFinalDisposalHearing(builder);
        service.applyHearingTime(builder);

        LocalDate expected = LocalDate.now().plusWeeks(16);
        CaseData result = builder.build();
        assertThat(result.getDisposalHearingFinalDisposalHearing().getDate()).isEqualTo(expected);
        assertThat(result.getDisposalHearingHearingTime().getDateTo()).isEqualTo(expected);
    }
}
