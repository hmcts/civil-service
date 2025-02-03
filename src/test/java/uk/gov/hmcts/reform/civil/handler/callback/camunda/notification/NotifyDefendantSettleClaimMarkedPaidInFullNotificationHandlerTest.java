package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@SpringBootTest(classes = {
    NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandler.class,
    NotificationsProperties.class,
    JacksonAutoConfiguration.class
})
@Ignore
public class NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String TASK_ID_RESPONDENT1 = "NotifyDefendantSettleClaimMarkedPaidInFull1";
    private static final String TASK_ID_RESPONDENT2 = "NotifyDefendantSettleClaimMarkedPaidInFull2";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private NotifyDefendantSettleClaimMarkedPaidInFullNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifySettleClaimMarkedPaidInFullDefendantTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyDefendantSolicitor1_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "defendant-settle-claim-marked-paid-in-full-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor2_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "defendant-settle-claim-marked-paid-in-full-000DC001"
            );

        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, "Test Org Name"
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);

    }
}
