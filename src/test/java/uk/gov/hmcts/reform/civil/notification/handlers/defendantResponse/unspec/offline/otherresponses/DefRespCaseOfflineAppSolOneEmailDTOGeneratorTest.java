package uk.gov.hmcts.reform.civil.notification.handlers.defendantResponse.unspec.offline.otherresponses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

class DefRespCaseOfflineAppSolOneEmailDTOGeneratorTest {

    private DefRespCaseOfflineAppSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new DefRespCaseOfflineAppSolOneEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnOfflineTemplateFor1v1Or2v1() {
        String expectedTemplate = "offline-template-1v1";
        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()).thenReturn(expectedTemplate);

        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondent2()).thenReturn(null); // to simulate 1v1
        when(caseData.getApplicant1()).thenReturn(null);  // not checked here, but ensures mock is complete
        when(caseData.getRespondent1()).thenReturn(null); // not used

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldReturnOfflineMultipartyTemplateForOtherScenarios() {
        String expectedTemplate = "offline-template-multiparty";
        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty()).thenReturn(expectedTemplate);

        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondent2()).thenReturn(mock(uk.gov.hmcts.reform.civil.model.Party.class)); // simulate 1v2
        when(caseData.getApplicant1()).thenReturn(null);
        when(caseData.getRespondent1()).thenReturn(null);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("defendant-response-case-handed-offline-applicant-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> mockProperties = new HashMap<>();
        mockProperties.put("someKey", "someValue");

        try (var mockedHelper = mockStatic(DefRespCaseOfflineHelper.class);
             var mockedUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class)) {

            mockedUtils.when(() ->
                                 uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService)
            ).thenReturn("OrgName Ltd");

            mockedHelper.when(() ->
                                  DefRespCaseOfflineHelper.caseOfflineNotificationProperties(caseData)
            ).thenReturn(mockProperties);

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "OrgName Ltd");
            assertThat(result).containsEntry("someKey", "someValue");
        }
    }
}
