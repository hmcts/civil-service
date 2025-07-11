package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneralApplicationsParamsBuilderTest {

    private GeneralApplicationsParamsBuilder builder;
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = mock(FeatureToggleService.class);
        builder = new GeneralApplicationsParamsBuilder(featureToggleService);
    }

    @Test
    void shouldAddParamsWhenFeatureToggleIsEnabled() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("djClaimantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
        assertThat(params).containsEntry("djClaimantNotificationMessageCy", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>");
        assertThat(params).containsEntry("djDefendantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");
        assertThat(params).containsEntry("djDefendantNotificationMessageCy", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</a>");
    }

    @Test
    void shouldAddParamsWhenFeatureToggleIsDisabled() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(false);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("djClaimantNotificationMessage", "<u>make an application to vary the judgment</u>");
        assertThat(params).containsEntry("djClaimantNotificationMessageCy", "<u>wneud cais i amrywio’r dyfarniad</u>");
        assertThat(params).containsEntry("djDefendantNotificationMessage", "<u>make an application to set aside (remove) or vary the judgment</u>");
        assertThat(params).containsEntry("djDefendantNotificationMessageCy", "<u>wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</u>");
    }
}
