package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_NEW_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_NEW_SOLICITOR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_LINK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;


@SpringBootTest(classes = {
    ChangeOfRepresentationNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class ChangeOfRepresentationNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE = "notice-of-change-000DC001";
    public static final String templateId = "3d8f3754-c317-4175-9ca9-a75b78419d68";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @Autowired
    private ChangeOfRepresentationNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNoticeOfChangeNewSolicitor())
                .thenReturn(templateId);

            when(NocNotificationUtils.getNewSolicitorEmail(any())).thenReturn("new-solicitor@example.com");
            when(NocNotificationUtils.getPreviousSolicitorEmail(any())).thenReturn("previous-solicitor@example.com");
        }

        @Nested
        class notifyNewSolicitorEvent {

            @Test
            void shouldNotifyNewSolicitorWhenInvoked_whenInvoked() {
                System.out.println("NEW EVENT TRIGGERED CORRECTLY!!");
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_NEW_SOLICITOR.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, "Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getLegacyCaseReference(),
                    PARTY_NAME, "Previous Solicitor",
                    CASE_LINK, "Case link"
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateId,
                    expectedProperties,
                    REFERENCE
                );
            }
        }

        @Nested
        class notifyPreviousSolicitorEvent {

            @Test
            void shouldNotifyNewSolicitorWhenInvoked_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_FORMER_SOLICITOR.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, "Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getLegacyCaseReference(),
                    PARTY_NAME, "Previous Solicitor",
                    CASE_LINK, "Case link"
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "new-solicitor@example.com",
                    templateId,
                    expectedProperties,
                    REFERENCE
                );
            }
        }

        @Nested
        class notifyOtherSolicitor1Event {

            @Test
            void shouldNotifyOtherPartiesWhenInvoked_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, "Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getLegacyCaseReference(),
                    PARTY_NAME, "Previous Solicitor",
                    CASE_LINK, "Case link"
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateId,
                    expectedProperties,
                    REFERENCE
                );
            }
        }

        @Nested
        class notifyOtherSolicitor2Event {

            @Test
            void shouldNotifyOtherPartiesWhenInvoked_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_2.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, "Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co",
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getLegacyCaseReference(),
                    PARTY_NAME, "Previous Solicitor",
                    CASE_LINK, "Case link"
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedProperties,
                    REFERENCE
                );
            }
        }

    }


    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                           NOTIFY_FORMER_SOLICITOR.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_FORMER_SOLICITOR);

        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                           NOTIFY_NEW_SOLICITOR.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_NEW_SOLICITOR);

        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                           NOTIFY_OTHER_SOLICITOR_1.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_OTHER_SOLICITOR_1);

        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                           NOTIFY_OTHER_SOLICITOR_2.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_OTHER_SOLICITOR_2);
    }
}
