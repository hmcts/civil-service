package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjDisposalDirectionsServiceTest {

    private static final String JUDGE_NAME = "District Judge Red";

    @Mock
    private WorkingDayIndicator workingDayIndicator;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private DjDisposalDirectionsService service;
    private DjDisposalNarrativeService disposalNarrativeService;

    @BeforeEach
    void setUp() {
        DjWelshLanguageService welshLanguageService = new DjWelshLanguageService();
        DjDeadlineService deadlineService = new DjDeadlineService(workingDayIndicator, deadlinesCalculator);
        disposalNarrativeService = new DjDisposalNarrativeService(deadlineService);
        service = new DjDisposalDirectionsService(deadlineService, welshLanguageService, disposalNarrativeService);

        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDate.class));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt()))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0, LocalDate.class);
                int days = invocation.getArgument(1, Integer.class);
                return date.plusDays(days);
            });
    }

    @Test
    void shouldPopulateDisposalDirections() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();

        service.populateDisposalDirections(builder, JUDGE_NAME);

        CaseData result = builder.build();

        DisposalHearingJudgesRecitalDJ recital = result.getDisposalHearingJudgesRecitalDJ();
        assertThat(recital).isNotNull();
        assertThat(recital.getJudgeNameTitle()).isEqualTo(JUDGE_NAME);

        assertThat(result.getDisposalHearingFinalDisposalHearingDJ().getDate())
            .isEqualTo(LocalDate.now().plusWeeks(16));

        DisposalHearingOrderMadeWithoutHearingDJ orderWithoutHearing =
            result.getDisposalHearingOrderMadeWithoutHearingDJ();
        String expectedDeadline = LocalDate.now().plusDays(5)
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH));
        assertThat(orderWithoutHearing.getInput()).contains(expectedDeadline);

        SdoR2WelshLanguageUsage welshUsage = result.getSdoR2DisposalHearingWelshLanguageDJ();
        assertThat(welshUsage).isNotNull();
        assertThat(welshUsage.getDescription()).isEqualTo(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
    }
}
