package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GeneralApplicationsParamsBuilderTest {

    private GeneralApplicationsParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GeneralApplicationsParamsBuilder();
    }

    @Test
    void shouldAddParamsWhenFeatureToggleIsEnabled() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("djClaimantNotificationMessage",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
        assertThat(params).containsEntry("djClaimantNotificationMessageCy",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>");
        assertThat(params).containsEntry("djDefendantNotificationMessage",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");
        assertThat(params).containsEntry("djDefendantNotificationMessageCy",
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</a>");
    }
}
