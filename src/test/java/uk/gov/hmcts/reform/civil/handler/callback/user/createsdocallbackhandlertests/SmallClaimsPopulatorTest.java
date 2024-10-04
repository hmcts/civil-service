package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SmallClaimsPopulator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SmallClaimsPopulatorTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SmallClaimsPopulator smallClaimsPopulator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(10000))
            .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
            .build();
    }

    @Test
    void shouldSetSmallClaimsFields_whenSdoR2Enabled() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), any(Integer.class))).thenReturn(LocalDate.now().plusDays(5));

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        smallClaimsPopulator.setSmallClaimsFields(updatedData, caseData);

        CaseData result = updatedData.build();
        assertThat(result.getSmallClaimsJudgesRecital()).isNotNull();
        assertThat(result.getSmallClaimsDocuments()).isNotNull();
        assertThat(result.getSdoR2SmallClaimsWitnessStatementOther()).isNotNull();
        assertThat(result.getSmallClaimsHearing()).isNotNull();
        assertThat(result.getSmallClaimsNotes()).isNotNull();
        assertThat(result.getSmallClaimsCreditHire()).isNotNull();
        assertThat(result.getSmallClaimsRoadTrafficAccident()).isNotNull();
    }

    @Test
    void shouldSetSmallClaimsFields_whenSdoR2Disabled() {
        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), any(Integer.class))).thenReturn(LocalDate.now().plusDays(5));

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        smallClaimsPopulator.setSmallClaimsFields(updatedData, caseData);

        CaseData result = updatedData.build();
        assertThat(result.getSmallClaimsJudgesRecital()).isNotNull();
        assertThat(result.getSmallClaimsDocuments()).isNotNull();
        assertThat(result.getSmallClaimsWitnessStatement()).isNotNull();
        assertThat(result.getSmallClaimsHearing()).isNotNull();
        assertThat(result.getSmallClaimsNotes()).isNotNull();
        assertThat(result.getSmallClaimsCreditHire()).isNotNull();
        assertThat(result.getSmallClaimsRoadTrafficAccident()).isNotNull();
    }

    @Test
    void shouldSetSmallClaimsFields_whenCarmEnabledForCase() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(1));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), any(Integer.class))).thenReturn(LocalDate.now().plusDays(5));

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        smallClaimsPopulator.setSmallClaimsFields(updatedData, caseData);

        CaseData result = updatedData.build();
        assertThat(result.getSmallClaimsMediationSectionStatement()).isNotNull();
    }
}
