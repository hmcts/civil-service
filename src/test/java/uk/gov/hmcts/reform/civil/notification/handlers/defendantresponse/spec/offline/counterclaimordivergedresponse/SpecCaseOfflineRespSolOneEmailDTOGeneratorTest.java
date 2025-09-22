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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_SPEC;

class SpecCaseOfflineRespSolOneEmailDTOGeneratorTest {

    private SpecCaseOfflineHelper caseOfflineHelper;
    private OrganisationService organisationService;
    private SpecCaseOfflineRespSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        caseOfflineHelper = mock(SpecCaseOfflineHelper.class);
        organisationService = mock(OrganisationService.class);
        generator = new SpecCaseOfflineRespSolOneEmailDTOGenerator(caseOfflineHelper, organisationService);
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(caseOfflineHelper.getRespTemplateForSpecClaims(caseData)).thenReturn("resp-template-id");

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo("resp-template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("defendant-response-case-handed-offline-respondent-notification-%s");
    }

    @Test
    void shouldAddRespondentOrgNameToProperties() {
        CaseData caseData = CaseData.builder().build();
        String expectedOrgName = "Defendant Org Ltd";

        Map<String, String> baseProperties = new HashMap<>();

        try (var utilities = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class)) {
            utilities.when(() -> uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent(caseData,
                                                                                                                         true, organisationService))
                .thenReturn(expectedOrgName);

            Map<String, String> result = generator.addCustomProperties(baseProperties, caseData);
            assertThat(result)
                .containsEntry(DEFENDANT_NAME_SPEC, expectedOrgName)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, expectedOrgName);
        }
    }
}
