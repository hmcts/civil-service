package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoJourneyToggleServiceTest {

    @Mock
    private SdoFeatureToggleService featureToggleService;

    private SdoJourneyToggleService service;

    @BeforeEach
    void setUp() {
        service = new SdoJourneyToggleService(featureToggleService);
    }

    @Test
    void shouldApplyJourneyFlagsBasedOnToggles() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);
        when(featureToggleService.isWelshJourneyEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyJourneyFlags(caseData);

        assertThat(caseData.getShowCarmFields()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getBilingualHint()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldNotApplyChecklistWhenCarmDisabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        List<OrderDetailsPagesSectionsToggle> checklist = List.of(OrderDetailsPagesSectionsToggle.SHOW);

        service.applySmallClaimsChecklistToggle(caseData, checklist);

        assertThat(caseData.getSmallClaimsMediationSectionToggle()).isNull();
    }

    @Test
    void shouldPopulateR2MediationFieldsWhenCarmEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        service.applyR2SmallClaimsMediation(caseData, List.of(IncludeInOrderToggle.INCLUDE));

        assertThat(caseData.getSdoR2SmallClaimsMediationSectionToggle()).containsExactly(IncludeInOrderToggle.INCLUDE);
        assertThat(caseData.getSdoR2SmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldPopulateSmallClaimsMediationStatementWhenCarmEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        service.applySmallClaimsMediationStatement(caseData);

        assertThat(caseData.getSmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldReturnNullForNonSpecCasesWhenResolvingEaCourt() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isNull();
    }

    @Test
    void shouldReturnYesWhenWelshJourneyEnabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnYesForLipCaseWhenLocationNotWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnYesForLipCaseWhenLocationWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnYesForLipvLROneVOneEvenWhenNoCEnabledAndWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnYesWhenBaseLocationMissingForLipCase() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnYesForLipvLROneVOneWhenAllowedAndNoCEnabledAndWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isEqualTo(YesOrNo.YES);
    }
}
