package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    InterimJudgmentClaimantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class InterimJudgmentClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private InterimJudgmentClaimantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(notificationsProperties.getInterimJudgmentRequestedClaimant()).thenReturn("template-id-req");
            when(notificationsProperties.getInterimJudgmentApprovalClaimant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-app",
                getNotificationDataMap(),
                "interim-judgment-approval-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantSolicitorWith2Defendants_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-000DC001"));
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                "Legal Rep Claimant", "Test Org Name",
                "Claim number", "000DC001",
                "Defendant Name", "Mr. Sole Trader"
            );
        }
    }
}
