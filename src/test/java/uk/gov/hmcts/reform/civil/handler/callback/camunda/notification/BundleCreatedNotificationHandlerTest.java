package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BundleCreatedNotificationHandler.TASK_ID_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    BundleCreatedNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class BundleCreatedNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TEMPLATE_ID = "template-id";

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private BundleCreatedNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getBundleCreationTemplate()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            //Given: Case data at hearing scheduled state and callback param with Notify applicant event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to applicant
            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorOne_whenInvoked() {
            //Given: Case data at hearing scheduled state and callback param with Notify respondent1 event
            CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent1
            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvokedWithDiffSol() {
            //Given: Case data at hearing scheduled state and callback param with Notify respondent2 event and
            // different solicitor for respondent2
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent2
            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondentSolicitorTwo_whenInvokedWithSameSol() {
            //Given: Case data at hearing scheduled state and callback param with Notify respondent2 event and
            // same solicitor for respondent2
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: Email should not be sent to respondent2
            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotifyRespondentLip_whenIsNotRepresented() {
            //Given: Case data at hearing scheduled state and callback param with Notify respondent1 Lip
            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingDateScheduled().build().toBuilder()
                .respondent1Represented(YesOrNo.NO).respondent1(
                    Party.builder().partyName("John Doe").partyEmail("doe@doe.com").individualFirstName("John")
                        .individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()
            ).build();

            //When: handler is called
            handler.handle(params);

            //Then: verify email is sent to respondent1 lipy
            verify(notificationService).sendMail(
                "doe@doe.com",
                "template-id",
                getNotificationLipDataMap(caseData, "John Doe"),
                "bundle-created-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()).build()))
                .isEqualTo(TASK_ID_APPLICANT);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED.name()).build()).build()))
                .isEqualTo(TASK_ID_DEFENDANT1);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED.name()).build()).build()))
                .isEqualTo(TASK_ID_DEFENDANT2);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATED);
            assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATED);
            assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATED);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
            );
        }

        @NotNull
        private Map<String, String> getNotificationLipDataMap(CaseData caseData, String name) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
                PARTY_NAME, name
            );
        }
    }
}
