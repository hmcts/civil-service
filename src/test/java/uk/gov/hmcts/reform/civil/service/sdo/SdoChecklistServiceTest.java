package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SdoChecklistServiceTest {

    @Mock
    private SdoJourneyToggleService journeyToggleService;

    private SdoChecklistService service;

    @BeforeEach
    void setUp() {
        service = new SdoChecklistService(journeyToggleService);
    }

    @Test
    void shouldPopulateOrderChecklistsAndDelegateSmallClaimsToggle() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        List<OrderDetailsPagesSectionsToggle> showList = List.of(OrderDetailsPagesSectionsToggle.SHOW);

        service.applyOrderChecklists(caseData, builder, showList);

        CaseData result = builder.build();
        assertThat(result.getFastTrackAltDisputeResolutionToggle()).isEqualTo(showList);
        assertThat(result.getDisposalHearingDisclosureOfDocumentsToggle()).isEqualTo(showList);
        assertThat(result.getSmallClaimsHearingToggle()).isEqualTo(showList);
        verify(journeyToggleService).applySmallClaimsChecklistToggle(caseData, builder, showList);
    }

    @Test
    void shouldPopulateR2ChecklistsAndMediationToggle() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        List<IncludeInOrderToggle> includeList = List.of(IncludeInOrderToggle.INCLUDE);

        service.applyR2Checklists(caseData, builder, includeList);

        CaseData result = builder.build();
        assertThat(result.getSdoAltDisputeResolution().getIncludeInOrderToggle()).containsExactly(
            IncludeInOrderToggle.INCLUDE);
        assertThat(result.getSdoR2TrialToggle()).isEqualTo(includeList);
        verify(journeyToggleService).applyR2SmallClaimsMediation(caseData, builder, includeList);
    }
}
