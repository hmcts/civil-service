package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_BUNDLE_REQUIREMENT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_DOCUMENTS_EXCHANGE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_SEND;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_SCHEDULE_FUTURE_LOSS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_6;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_CPR32_7_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_TRIAL_NOTE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_WITNESS_UPLOAD;

@ExtendWith(MockitoExtension.class)
class DjDisposalNarrativeServiceTest {

    private static final String JUDGE = "Judge Smith";

    @Mock
    private DjDeadlineService deadlineService;

    private DjDisposalNarrativeService service;

    @BeforeEach
    void setUp() {
        service = new DjDisposalNarrativeService(deadlineService);
        lenient().when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.weeksFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.now().plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildJudgesRecitalWithName() {
        var recital = service.buildJudgesRecital(JUDGE);

        assertThat(recital.getJudgeNameTitle()).isEqualTo(JUDGE);
        assertThat(recital.getInput()).isEqualTo(JUDGE + ",");
    }

    @Test
    void shouldBuildDisclosureWithFourWeekDeadline() {
        var disclosure = service.buildDisclosureOfDocuments();

        assertThat(disclosure.getInput()).isEqualTo(DISPOSAL_DOCUMENTS_EXCHANGE);
        assertThat(disclosure.getDate()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(4));
    }

    @Test
    void shouldBuildSchedulesOfLossWithSequentialDeadlines() {
        var schedules = service.buildSchedulesOfLoss();

        assertThat(schedules.getDate1()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(10));
        assertThat(schedules.getDate2()).isEqualTo(LocalDate.of(2025, 1, 1).plusWeeks(12));
        assertThat(schedules.getInput1()).isEqualTo(DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ);
        assertThat(schedules.getInput2()).isEqualTo(DISPOSAL_SCHEDULE_COUNTER_SEND);
        assertThat(schedules.getInput3()).isEqualTo(DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ);
        assertThat(schedules.getInputText4()).isEqualTo(DISPOSAL_SCHEDULE_FUTURE_LOSS);
    }

    @Test
    void shouldBuildWitnessOfFactAndBundleUsingSharedText() {
        var witness = service.buildWitnessOfFact();
        var bundle = service.buildBundle();

        assertThat(witness.getInput1()).isEqualTo(DISPOSAL_WITNESS_UPLOAD + " ");
        assertThat(witness.getInput2()).isEqualTo(DISPOSAL_WITNESS_CPR32_6);
        assertThat(witness.getInput3()).isEqualTo(DISPOSAL_WITNESS_CPR32_7_DEADLINE);
        assertThat(witness.getInput4()).isEqualTo(DISPOSAL_WITNESS_TRIAL_NOTE_DJ);
        assertThat(bundle.getInput()).isEqualTo(DISPOSAL_BUNDLE_REQUIREMENT);
    }
}
