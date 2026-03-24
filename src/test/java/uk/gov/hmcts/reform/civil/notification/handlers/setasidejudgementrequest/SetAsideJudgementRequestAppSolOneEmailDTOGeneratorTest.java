package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON_FROM_CASEWORKER;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementRequestAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private SetAsideJudgementRequestAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getNotifySetAsideJudgmentTemplate()).thenReturn("template");
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
            .joSetAsideJudgmentErrorText("reason")
            .build();
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> utils = Mockito.mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService)).thenReturn("Org");
            utils.when(() -> NotificationUtils.getDefendantNameBasedOnCaseType(caseData)).thenReturn("Defendant");

            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertThat(result)
                .containsEntry(LEGAL_ORG_NAME, "Org")
                .containsEntry(DEFENDANT_NAME_INTERIM, "Defendant")
                .containsEntry(REASON_FROM_CASEWORKER, "reason");
        }
    }
}
