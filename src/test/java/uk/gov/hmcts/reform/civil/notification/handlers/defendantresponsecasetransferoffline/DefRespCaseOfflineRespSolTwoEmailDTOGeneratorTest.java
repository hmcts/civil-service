package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecasetransferoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

class DefRespCaseOfflineRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_1V1 = "template-1v1";
    private static final String TEMPLATE_MULTIPARTY = "template-multiparty";
    private static final String LEGAL_ORG_NAME = "Test Organisation";

    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;

    private DefRespCaseOfflineRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new DefRespCaseOfflineRespSolTwoEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnTemplateIdFor1v1Or2v1Case() {
        CaseData caseData = mock(CaseData.class);
        when(is1v1Or2v1Case(caseData)).thenReturn(true);
        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()).thenReturn(TEMPLATE_1V1);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_1V1);
    }

    @Test
    void shouldReturnTemplateIdForMultipartyCase() {
        CaseData caseData = mock(CaseData.class);
        when(is1v1Or2v1Case(caseData)).thenReturn(false);
        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty()).thenReturn(TEMPLATE_MULTIPARTY);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_MULTIPARTY);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("defendant-response-case-handed-offline-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> baseProps = Map.of("existing", "value");

        Map<String, String> offlineProps = Map.of("offlineKey", "offlineValue");
        Map<String, String> expected = Map.of(
            "existing", "value",
            "claimLegalOrgNameSpec", LEGAL_ORG_NAME,
            "offlineKey", "offlineValue"
        );

        mockStatic(NotificationUtils.class);
        mockStatic(DefRespCaseOfflineHelper.class);

        try (
            var utilsMock = mockStatic(NotificationUtils.class);
            var helperMock = mockStatic(DefRespCaseOfflineHelper.class)
        ) {
            utilsMock.when(() -> getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn(LEGAL_ORG_NAME);
            helperMock.when(() -> DefRespCaseOfflineHelper.caseOfflineNotificationProperties(caseData))
                .thenReturn(offlineProps);

            Map<String, String> result = generator.addCustomProperties(baseProps, caseData);

            assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
        }
    }
}
