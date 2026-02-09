package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoSmallClaimsOrderDefaultsServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;
    @Mock
    private SdoFeatureToggleService featureToggleService;

    private SdoSmallClaimsOrderDefaultsService service;

    @BeforeEach
    void setUp() {
        SdoJourneyToggleService journeyToggleService = new SdoJourneyToggleService(featureToggleService);
        SdoSmallClaimsNarrativeService narrativeService = new SdoSmallClaimsNarrativeService(deadlineService);
        service = new SdoSmallClaimsOrderDefaultsService(narrativeService, journeyToggleService);

        lenient().when(deadlineService.nextWorkingDayFromNowWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 6, 1).plusWeeks(invocation.getArgument(0, Integer.class)));
        lenient().when(deadlineService.workingDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 7, 1).plusDays(invocation.getArgument(0, Integer.class)));
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);
    }

    @Test
    void shouldPopulateSmallClaimsFieldsAndMediationStatement() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateSmallClaimsOrderDetails(
            caseData,
            List.of(OrderDetailsPagesSectionsToggle.SHOW)
        );

        assertThat(caseData.getSmallClaimsDocuments()).isNotNull();
        assertThat(caseData.getSmallClaimsMediationSectionStatement()).isNotNull();
        assertThat(caseData.getSmallClaimsCreditHire()).isNotNull();
        assertThat(caseData.getSmallClaimsFlightDelay()).isNotNull();
        assertThat(caseData.getSmallClaimsNotes()).isNotNull();
    }
}
