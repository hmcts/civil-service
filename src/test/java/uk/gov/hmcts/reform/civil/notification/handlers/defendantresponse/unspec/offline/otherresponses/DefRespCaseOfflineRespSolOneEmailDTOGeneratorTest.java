package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses.DefRespCaseOfflineHelper.caseOfflineNotificationProperties;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

class DefRespCaseOfflineRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_1V1 = "template-1v1";
    private static final String TEMPLATE_MULTIPARTY = "template-multiparty";
    private static final String LEGAL_ORG_NAME = "Resp1 Legal Org";

    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;
    private DefRespCaseOfflineRespSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new DefRespCaseOfflineRespSolOneEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnTemplateIdFor1v1Or2v1Case() {
        CaseData caseData = CaseData.builder()
            .addApplicant2(YesOrNo.YES)
            .build();

        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()).thenReturn(TEMPLATE_1V1);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_1V1);
    }

    @Test
    void shouldReturnTemplateIdForMultipartyCase() {
        CaseData caseData = CaseData.builder()
            .respondent2(Party.builder().build())
            .build();
        when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty()).thenReturn(TEMPLATE_MULTIPARTY);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_MULTIPARTY);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();
        assertThat(referenceTemplate).isEqualTo("defendant-response-case-handed-offline-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> inputProps = new HashMap<>();
        Map<String, String> offlineProps = Map.of("caseKey", "offlineVal");

        try (var utilsMock = mockStatic(NotificationUtils.class);
             var helperMock = mockStatic(DefRespCaseOfflineHelper.class)) {

            utilsMock.when(() -> getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn(LEGAL_ORG_NAME);
            helperMock.when(() -> caseOfflineNotificationProperties(caseData)).thenReturn(offlineProps);

            Map<String, String> result = generator.addCustomProperties(inputProps, caseData);

            assertThat(result).containsEntry(NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC, LEGAL_ORG_NAME)
                .containsEntry("caseKey", "offlineVal");
        }
    }
}
