package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NAME_SPEC;

class SpecCaseOfflineAppSolOneEmailDTOGeneratorTest {

    private SpecCaseOfflineHelper caseOfflineHelper;
    private OrganisationService organisationService;
    private SpecCaseOfflineAppSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        caseOfflineHelper = mock(SpecCaseOfflineHelper.class);
        organisationService = mock(OrganisationService.class);
        generator = new SpecCaseOfflineAppSolOneEmailDTOGenerator(caseOfflineHelper, organisationService);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(caseOfflineHelper.getApplicantTemplateForSpecClaims(caseData)).thenReturn("template-id");

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo("template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("defendant-response-case-handed-offline-applicant-notification-%s");
    }

    @Test
    void shouldAddCustomPropertiesIncludingOrgNameAndOfflineProps() {
        CaseData caseData = CaseData.builder().build();

        String orgName = "Test Org Ltd";
        Map<String, String> baseProperties = new HashMap<>();
        Map<String, String> caseOfflineProps = Map.of("reason", "Full admission");

        when(caseOfflineHelper.getApplicantTemplateForSpecClaims(caseData)).thenReturn("template-id");

        try (var utilities = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class)) {
            utilities.when(() -> uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn(orgName);

            try (var helper = mockStatic(SpecCaseOfflineHelper.class)) {
                helper.when(() -> SpecCaseOfflineHelper.caseOfflineNotificationProperties(caseData)).thenReturn(caseOfflineProps);

                Map<String, String> result = generator.addCustomProperties(baseProperties, caseData);

                assertThat(result)
                    .containsEntry(CLAIM_NAME_SPEC, orgName)
                    .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, orgName)
                    .containsEntry("reason", "Full admission");
            }
        }
    }
}
