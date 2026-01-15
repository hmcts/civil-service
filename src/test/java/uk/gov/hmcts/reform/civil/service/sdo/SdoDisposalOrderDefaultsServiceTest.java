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

@ExtendWith(MockitoExtension.class)
class SdoDisposalOrderDefaultsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoDisposalOrderDefaultsService service;

    @BeforeEach
    void setUp() {
        SdoDisposalNarrativeService sdoDisposalNarrativeService = new SdoDisposalNarrativeService(deadlineService);
        service = new SdoDisposalOrderDefaultsService(sdoDisposalNarrativeService);
        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1).plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 2, 1).plusDays(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateCoreDisposalFields() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateDisposalOrderDetails(caseData);

        assertThat(caseData.getDisposalHearingJudgesRecital()).isNotNull();
        assertThat(caseData.getDisposalOrderWithoutHearing()).isNotNull();
        assertThat(caseData.getDisposalHearingNotes().getDate()).isNotNull();
    }
}
