package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@SpringBootTest(classes = {
    ChangeOfRepresentationNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class ChangeOfRepresentationNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE = "notice-of-change-000DC001";
    private static final String PREVIOUS_SOL = "Previous solicitor";
    private static final String PREVIOUS_SOL_TEMPLATE = "former-sol-template-id";
    private static final String CASE_TITLE = "Mr. John Rambo v Mr. Sole Trader";
    private static final String NEW_SOLICITOR = "New solicitor";
    private static final String OTHER_SOLICITOR = "Other solicitor";
    private static final String OTHER_SOLICITOR_2 = "Other solicitor2";
    private static final String OTHER_SOL_TEMPLATE = "other-sol-template-id";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private ChangeOfRepresentationNotificationHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(organisationService.findOrganisationById("Previous-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
            when(organisationService.findOrganisationById("New-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY R"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY R2"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR_2).build()));
            when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(PREVIOUS_SOL_TEMPLATE);
            when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(OTHER_SOL_TEMPLATE);
        }

        @Nested
        class NotifyPreviousSolicitorEvent {

            @Test
            void shouldNotifyFormerSolicitor_whenInvoked() {
                CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_FORMER_SOLICITOR.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "previous-solicitor@example.com",
                    PREVIOUS_SOL_TEMPLATE,
                    expectedProperties,
                    REFERENCE
                );
            }

            @Test
            void shouldRemoveFormerSolicitorEmail_whenInvoked() {
                CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_FORMER_SOLICITOR.name()).build()).build();

                var expected = objectMapper.convertValue(caseData.getChangeOfRepresentation().toBuilder()
                                                             .formerRepresentationEmailAddress(null).build(), new TypeReference<>() {});

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getData())
                    .extracting("changeOfRepresentation")
                    .isEqualTo(expected);
            }
        }

        @Nested
        class NotifyOtherSolicitor1Event {

            @Test
            void shouldNotifyOtherPartiesWhenInvoked_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    OTHER_SOL_TEMPLATE,
                    expectedProperties,
                    REFERENCE
                );
            }
        }

        @Nested
        class NotifyOtherSolicitor2Event {

            @Test
            void shouldNotifyOtherPartiesWhenInvoked_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_2.name()).build()).build();

                Map<String, String> expectedProperties = Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR_2
                );

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    OTHER_SOL_TEMPLATE,
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
                           NOTIFY_OTHER_SOLICITOR_1.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_OTHER_SOLICITOR_1);

        assertThat(handler
                       .camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                           NOTIFY_OTHER_SOLICITOR_2.name()).build()).build()))
            .isEqualTo(TASK_ID_NOTIFY_OTHER_SOLICITOR_2);
    }

    @Nested
    class SkipNotifyParties {

        @Test
        void shouldNotSendEmailWhenNotifyingFormerSolForLip() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeLip().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_FORMER_SOLICITOR.name()).build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotSendEmailWhenNotifyingOtherSol1ForLip() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol1Lip().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotSendEmailWhenNotifyingOtherSol2ForLip() {
            CaseData caseData =
                CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol2Lip().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_OTHER_SOLICITOR_2.name()).build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }
    }
}
