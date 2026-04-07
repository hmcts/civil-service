package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPPI;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjDirectionsToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsHousingDisrepair;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsPPI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjPrePopulateTrialOtherRemedyServiceTest {

    @Mock
    private DjSpecialistNarrativeService narrativeService;

    @Mock
    private FeatureToggleService featureToggleService;

    private DjPrePopulateTrialOtherRemedyService service;

    @BeforeEach
    void setUp() {
        service = new DjPrePopulateTrialOtherRemedyService(
            narrativeService,
            featureToggleService,
            new DjDirectionsToggleService()
        );
    }

    @Test
    void shouldClearPpiAndHousingWhenOtherRemedyDisabled() {
        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(false);
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applyOtherRemedyTrialDefaults(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getTrialPPI()).isNull();
        assertThat(result.getTrialHousingDisrepair()).isNull();
        verifyNoInteractions(narrativeService);
    }

    @Test
    void shouldPopulatePpiAndOtherRemedyHousingWhenToggleEnabledAndBothSelected() {
        TrialPPI ppi = new TrialPPI();
        TrialHousingDisrepair housing = new TrialHousingDisrepair();
        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(true);
        when(narrativeService.buildTrialPPI()).thenReturn(ppi);
        when(narrativeService.buildTrialHousingDisrepairOtherRemedy()).thenReturn(housing);

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .caseManagementOrderAdditional(List.of(
                OrderTypeTrialAdditionalDirectionsPPI,
                OrderTypeTrialAdditionalDirectionsHousingDisrepair
            ))
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applyOtherRemedyTrialDefaults(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getTrialPPI()).isSameAs(ppi);
        assertThat(result.getTrialHousingDisrepair()).isSameAs(housing);
        verify(narrativeService).buildTrialPPI();
        verify(narrativeService).buildTrialHousingDisrepairOtherRemedy();
        verify(narrativeService, never()).buildTrialHousingDisrepair();
    }

    @Test
    void shouldClearPpiAndHousingWhenToggleEnabledButNoneSelected() {
        when(featureToggleService.isOtherRemedyEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .caseManagementOrderAdditional(List.of())
            .build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applyOtherRemedyTrialDefaults(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getTrialPPI()).isNull();
        assertThat(result.getTrialHousingDisrepair()).isNull();
        verifyNoInteractions(narrativeService);
    }
}
