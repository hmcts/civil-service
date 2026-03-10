package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@ExtendWith(MockitoExtension.class)
class SettleClaimPaidInFullNotificationRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "defendant-settle-claim-marked-paid-in-full-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private SettleClaimPaidInFullNotificationRespSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getNotifySettleClaimMarkedPaidInFullDefendantTemplate()).thenReturn(TEMPLATE_ID);

        String actualTemplate = generator.getEmailTemplateId(caseData);

        assertThat(actualTemplate).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddLegalOrganisationNameToCustomProperties() {
        uk.gov.hmcts.reform.ccd.model.OrganisationPolicy organisationPolicy =
            new uk.gov.hmcts.reform.ccd.model.OrganisationPolicy()
                .setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("org-id"));
        CaseData caseData = CaseData.builder()
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        Organisation organisation = new Organisation().setName("Test Org Name");
        when(organisationService.findOrganisationById("org-id")).thenReturn(Optional.of(organisation));

        Map<String, String> properties = new HashMap<>();
        Map<String, String> actual = generator.addCustomProperties(properties, caseData);

        assertThat(actual)
            .containsEntry(LEGAL_ORG_NAME, "Test Org Name")
            .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Test Org Name");
    }
}
