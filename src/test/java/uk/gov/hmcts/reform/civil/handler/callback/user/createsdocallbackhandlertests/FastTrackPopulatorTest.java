package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackPopulatorTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private FastTrackPopulator fastTrackPopulator;

    @Test
    void shouldSetFastTrackFields() {
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenReturn(LocalDate.from(LocalDateTime.now().plusDays(1)));
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        fastTrackPopulator.setFastTrackFields(caseDataBuilder);

        verify(workingDayIndicator, atLeastOnce()).getNextWorkingDay(any(LocalDate.class));
        verify(deadlinesCalculator, atLeastOnce()).getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class));
        verify(featureToggleService, atLeastOnce()).isSdoR2Enabled();
    }

    @Test
    void shouldSetFastTrackFieldsWhenSdoR2Disabled() {
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenReturn(LocalDate.from(LocalDateTime.now().plusDays(1)));
        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        fastTrackPopulator.setFastTrackFields(caseDataBuilder);

        verify(workingDayIndicator, atLeastOnce()).getNextWorkingDay(any(LocalDate.class));
        verify(deadlinesCalculator, atLeastOnce()).getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class));
        verify(featureToggleService, atLeastOnce()).isSdoR2Enabled();
    }
}
