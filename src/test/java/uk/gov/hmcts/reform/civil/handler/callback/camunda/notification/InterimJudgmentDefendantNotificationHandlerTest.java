package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_DEF;

@SpringBootTest(classes = {
    InterimJudgmentDefendantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class InterimJudgmentDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private InterimJudgmentDefendantNotificationHandler handler;
    @Captor
    ArgumentCaptor<Map<String, String>> captor;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(notificationsProperties.getInterimJudgmentRequestedDefendant()).thenReturn("template-id-req");
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-app",
                getNotificationDataMap(),
                "interim-judgment-approval-notification-def-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor2Defendants_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-def-000DC001"));
        }

        @Test
        void shouldUseDefendantNameIfOrganisationNameNull() {
            final CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor();

            caseDataBuilder.respondent1OrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                .build());

            final CaseData caseData = caseDataBuilder.build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                anyString(),
                eq("template-id-app"),
                captor.capture(),
                eq("interim-judgment-approval-notification-def-000DC001"));

            final Map<String, String> defendant1Map = captor.getAllValues().get(0);
            assertEquals(defendant1Map.get(LEGAL_ORG_DEF), defendant1Map.get(DEFENDANT_NAME_INTERIM));
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                "Defendant LegalOrg Name", "Test Org Name",
                "Claim number", "000DC001",
                "Defendant Name", "Mr. Sole Trader"
            );
        }
    }
}
