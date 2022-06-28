package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.prd.model.SuperUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_APP_SOL_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_FIRST_CAA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_FIRST_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_SECOND_CAA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_SECOND_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@SpringBootTest(classes = {
    CreateClaimRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class CreateClaimRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateClaimRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithCcEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvolked_InOneVsTwoCaseSameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked_InOneVsTwoCaseDifferentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithMultipartyEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Nested
        class TriggerNotifyCaaEvents {

            CaseData caseData;
            String respondent1OrgId;
            String respondent2OrgId;
            List<String> mockRespondent1CaaEmails;
            List<String> mockRespondent2CaaEmails;

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceRespondent2().build();
                respondent1OrgId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
                respondent2OrgId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
                mockRespondent1CaaEmails = Arrays.asList(
                    "org-1-caa-1@example.com",
                    "org-1-caa-2@example.com",
                    "org-1-caa-3@example.com"
                );
                mockRespondent2CaaEmails = Arrays.asList(
                    "org-2-caa-1@example.com",
                    "org-2-caa-2@example.com",
                    "org-2-caa-3@example.com"
                );
            }

            @Test
            void shouldNotifyRespondentOneOrganisationCaas_whenUsersExist_whenInvoked() {
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent1OrgId))
                    .thenReturn(Optional.of(mockProfessionalUsersEntityResponse(mockRespondent1CaaEmails)));

                handler.handle(params);

                verifyAllNotificationsSent(mockRespondent1CaaEmails, caseData);
            }

            @Test
            void shouldNotifyRespondentTwoOrganisationCaas_whenUsersExist_whenInvoked_withDefendantTwoNotifyOptions() {
                var caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceRespondent2()
                    .defendantSolicitorNotifyClaimOptions("Defendant Two:").build();

                when(organisationService.findUsersInOrganisation(respondent2OrgId))
                    .thenReturn(Optional.of(mockProfessionalUsersEntityResponse(mockRespondent2CaaEmails)));

                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG").build()).build();

                handler.handle(params);

                verifyAllNotificationsSent(mockRespondent2CaaEmails, caseData);
            }

            @Test
            void shouldNotifyRespondentOneOrganisationSuperUser_whenNoUsersExist_whenInvoked() {
                var mockOrganisation = Organisation.builder()
                    .superUser(SuperUser.builder().email("mock-super-user@email.com").build()).build();
                var mockOrganisationsResponse = ProfessionalUsersEntityResponse.builder()
                    .users(new ArrayList<>()).build();
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent1OrgId)).thenReturn(
                    Optional.of(mockOrganisationsResponse));
                when(organisationService.findOrganisationById(respondent1OrgId))
                    .thenReturn(Optional.of(mockOrganisation));

                handler.handle(params);

                verifyAllNotificationsSent(Arrays.asList(mockOrganisation.getSuperUser().getEmail()), caseData);
            }

            @Test
            void shouldThrowException_whenOrganisationObjectNotPopulated_whenInvoked() {
                var mockOrganisationsResponse = ProfessionalUsersEntityResponse.builder()
                    .users(new ArrayList<>()).build();
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent1OrgId)).thenReturn(
                    Optional.of(mockOrganisationsResponse));
                when(organisationService.findOrganisationById(respondent1OrgId))
                    .thenReturn(Optional.empty());

                assertThrows(CallbackException.class, () -> handler.handle(params));
            }

            @Test
            void shouldNotifyRespondentTwoOrganisationCaas_whenUsersExist_whenInvoked() {
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent2OrgId))
                    .thenReturn(Optional.of(mockProfessionalUsersEntityResponse(mockRespondent2CaaEmails)));

                handler.handle(params);

                verifyAllNotificationsSent(mockRespondent2CaaEmails, caseData);
            }

            @Test
            void shouldNotifyRespondentTwoOrganisationSuperUser_whenNoUsersExist_whenInvoked() {
                var mockOrganisation = Organisation.builder()
                    .superUser(SuperUser.builder().email("mock-super-user@email.com").build())
                    .build();
                var mockOrganisationsResponse = ProfessionalUsersEntityResponse.builder()
                    .users(new ArrayList<>()).build();
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent2OrgId))
                    .thenReturn(Optional.of(mockOrganisationsResponse));
                when(organisationService.findOrganisationById(respondent2OrgId))
                    .thenReturn(Optional.of(mockOrganisation));

                handler.handle(params);

                verifyAllNotificationsSent(Arrays.asList(mockOrganisation.getSuperUser().getEmail()), caseData);
            }

            @Test
            void shouldThrowException_whenOrganisationObjectNotPopulated_whenInvokedWithCaaRespondentTwoEvent() {
                var mockOrganisationsResponse = ProfessionalUsersEntityResponse.builder()
                    .users(new ArrayList<>()).build();
                var params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG").build()).build();

                when(organisationService.findUsersInOrganisation(respondent2OrgId)).thenReturn(
                    Optional.of(mockOrganisationsResponse));
                when(organisationService.findOrganisationById(respondent2OrgId))
                    .thenReturn(Optional.empty());

                assertThrows(CallbackException.class, () -> handler.handle(params));
            }
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build())).isEqualTo(TASK_ID_EMAIL_FIRST_SOL);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build())).isEqualTo(TASK_ID_EMAIL_APP_SOL_CC);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE").build()).build())).isEqualTo(TASK_ID_EMAIL_SECOND_SOL);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG").build()).build())).isEqualTo(TASK_ID_EMAIL_FIRST_CAA);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG").build()).build())).isEqualTo(TASK_ID_EMAIL_SECOND_CAA);
    }

    private void verifyAllNotificationsSent(List<String> emailAddresses, CaseData caseData) {
        if (emailAddresses.isEmpty()) {
            verify(false);
        }
        emailAddresses.forEach(
            email -> verify(notificationService).sendMail(
                email,
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            ));
    }

    private ProfessionalUsersEntityResponse mockProfessionalUsersEntityResponse(List<String> emails) {
        var mockRoles = Arrays.asList("pui-caa");
        var mockCaaUsers = emails.stream()
            .map(email -> ProfessionalUsersResponse.builder().email(email).roles(mockRoles).build())
            .collect(Collectors.toList());

        return ProfessionalUsersEntityResponse.builder().users(mockCaaUsers).build();
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            "defendantName", "Mr. Sole Trader",
            "claimDetailsNotificationDeadline", formatLocalDate(NOTIFICATION_DEADLINE.toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
