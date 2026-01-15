package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
        CaseData caseData = CaseDataBuilder.builder().build();
        List<OrderDetailsPagesSectionsToggle> showList = List.of(OrderDetailsPagesSectionsToggle.SHOW);

        service.applyOrderChecklists(caseData, showList);

        assertThat(caseData.getFastTrackAltDisputeResolutionToggle()).isEqualTo(showList);
        assertThat(caseData.getDisposalHearingDisclosureOfDocumentsToggle()).isEqualTo(showList);
        assertThat(caseData.getSmallClaimsHearingToggle()).isEqualTo(showList);
        verify(journeyToggleService).applySmallClaimsChecklistToggle(caseData, showList);
    }

    @Test
    void shouldPopulateR2ChecklistsAndMediationToggle() {
        CaseData caseData = CaseDataBuilder.builder().build();
        List<IncludeInOrderToggle> includeList = List.of(IncludeInOrderToggle.INCLUDE);

        service.applyR2Checklists(caseData, includeList);

        assertThat(caseData.getSdoAltDisputeResolution().getIncludeInOrderToggle()).containsExactly(
            IncludeInOrderToggle.INCLUDE);
        assertThat(caseData.getSdoR2TrialToggle()).isEqualTo(includeList);
        verify(journeyToggleService).applyR2SmallClaimsMediation(caseData, includeList);
    }
}
