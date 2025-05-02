package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

@ExtendWith(MockitoExtension.class)
class CreateSDORespondent2NotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    private CreateSDORespondent2NotificationHandler handler;

    @BeforeEach
    void setup() {
        CreateSDORespondent2LRNotificationSender lrNotificationSender =
            new CreateSDORespondent2LRNotificationSender(notificationService, notificationsProperties,
                                                         organisationService, featureToggleService, configuration
            );
        CreateSDORespondent2LiPNotificationSender lipNotificationSender =
            new CreateSDORespondent2LiPNotificationSender(notificationService, notificationsProperties,
                                                          configuration,
                                                          featureToggleService
            );
        handler = new CreateSDORespondent2NotificationHandler(lipNotificationSender, lrNotificationSender);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 1")
                                 .partyEmail("company@email.com")
                                 .build())
                .respondent2Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(),
                "create-sdo-respondent-2-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 2")
                                 .partyEmail("company@email.com")
                                 .build())
                .respondent2Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "company@email.com",
                "template-id",
                Map.of(
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    PARTY_NAME, "Mr. Sole Trader",
                    CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
                ),
                "create-sdo-respondent-2-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                 .request(CallbackRequest.builder().eventId(
                                                         "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED")
                                                              .build()).build()))
            .isEqualTo("CreateSDONotifyRespondentSolicitor2");
    }
}



