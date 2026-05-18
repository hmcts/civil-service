package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON_FROM_CASEWORKER;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementRequestRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private SetAsideJudgementRequestRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getNotifySetAsideJudgmentTemplate()).thenReturn("template");
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template");
    }

    @Test
    void shouldAddAdditionalProperties() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setJoSetAsideJudgmentErrorText("reason");
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> utils = Mockito.mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn("Org2");
            utils.when(() -> NotificationUtils.getDefendantNameBasedOnCaseType(caseData)).thenReturn("Defendant");

            Map<String, String> updated = generator.addCustomProperties(properties, caseData);

            assertThat(updated)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Org2")
                .containsEntry(LEGAL_ORG_NAME, "Org2")
                .containsEntry(DEFENDANT_NAME_INTERIM, "Defendant")
                .containsEntry(REASON_FROM_CASEWORKER, "reason");
        }
    }
}
