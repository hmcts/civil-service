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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applyJourneyFlags(caseData, builder);

        CaseData result = builder.build();
        assertThat(result.getShowCarmFields()).isEqualTo(YesOrNo.YES);
        assertThat(result.getBilingualHint()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldNotApplyChecklistWhenCarmDisabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(false);

        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        List<OrderDetailsPagesSectionsToggle> checklist = List.of(OrderDetailsPagesSectionsToggle.SHOW);

        service.applySmallClaimsChecklistToggle(caseData, builder, checklist);

        assertThat(builder.build().getSmallClaimsMediationSectionToggle()).isNull();
    }

    @Test
    void shouldPopulateR2MediationFieldsWhenCarmEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applyR2SmallClaimsMediation(caseData, builder, List.of(IncludeInOrderToggle.INCLUDE));

        CaseData result = builder.build();
        assertThat(result.getSdoR2SmallClaimsMediationSectionToggle()).containsExactly(IncludeInOrderToggle.INCLUDE);
        assertThat(result.getSdoR2SmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldPopulateSmallClaimsMediationStatementWhenCarmEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.applySmallClaimsMediationStatement(caseData, builder);

        assertThat(builder.build().getSmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldReturnNullForNonSpecCasesWhenResolvingEaCourt() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .build();

        assertThat(service.resolveEaCourtLocation(caseData, true)).isNull();
    }

    @Test
    void shouldReturnYesWhenWelshJourneyEnabled() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnNoForLipCaseWhenLocationNotWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getCaseManagementLocation()).thenReturn(CaseLocationCivil.builder().baseLocation("123").build());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed("123")).thenReturn(false);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldReturnYesForLipCaseWhenLocationWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getCaseManagementLocation()).thenReturn(CaseLocationCivil.builder().baseLocation("123").build());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed("123")).thenReturn(true);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnNoForLipvLROneVOneEvenWhenNoCEnabledAndWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        when(caseData.isLRvLipOneVOne()).thenReturn(false);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        assertThat(service.resolveEaCourtLocation(caseData, false)).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldThrowWhenBaseLocationMissingForLipCase() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        assertThatThrownBy(() -> service.resolveEaCourtLocation(caseData, true))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnYesForLipvLROneVOneWhenAllowedAndNoCEnabledAndWhitelisted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.isApplicantLiP()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        when(caseData.isLRvLipOneVOne()).thenReturn(false);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(caseData.getCaseManagementLocation()).thenReturn(CaseLocationCivil.builder().baseLocation("321").build());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed("321")).thenReturn(true);

        assertThat(service.resolveEaCourtLocation(caseData, true)).isEqualTo(YesOrNo.YES);
    }
}
