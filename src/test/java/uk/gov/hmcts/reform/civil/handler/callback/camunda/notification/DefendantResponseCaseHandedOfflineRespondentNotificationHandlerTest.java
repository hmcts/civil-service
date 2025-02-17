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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.CaseHandledOfflineRecipient;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent.CaseHandledOffLineRespondentSolicitorNotifierFactory;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent.CaseHandledOfflineRespondentSolicitorSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent.CaseHandledOfflineRespondentSolicitorUnspecNotifier;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseCaseHandedOfflineRespondentNotificationHandler.TASK_ID_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseCaseHandedOfflineRespondentNotificationHandler.TASK_ID_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseCaseHandedOfflineRespondentNotificationHandler.class,
    CaseHandledOffLineRespondentSolicitorNotifierFactory.class,
    CaseHandledOfflineRespondentSolicitorUnspecNotifier.class,
    CaseHandledOfflineRespondentSolicitorSpecNotifier.class,
    JacksonAutoConfiguration.class
})
class DefendantResponseCaseHandedOfflineRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private DefendantResponseCaseHandedOfflineRespondentNotificationHandler handler;
    @Autowired
    private CaseHandledOfflineRespondentSolicitorSpecNotifier caseHandledOfflineRespondentSolicitorSpecNotifier;

    @Nested
    class AboutToSubmitCallback {

        @Nested
        class OneVsOneScenario {

            @Test
            void shouldNotifyDefendantSolicitor1_when1v1Case() {
                when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                    .thenReturn("template-id");

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentCounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }
        }

        @Nested
        class OneVsTwoScenario {

            @BeforeEach
            void setup() {
                when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                    .thenReturn("template-id-multiparty");
            }

            @Test
            void shouldNotifyDefendantSolicitor1_when1v2Case() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
                    .thenReturn("template-id-multiparty");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor1_when1v2CaseUnspec() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
                    .respondent2ClaimResponseType(RespondentResponseType.COUNTER_CLAIM)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                    .thenReturn("template-id-multiparty");

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2_when1v2Case() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
                    .thenReturn("template-id-multiparty");

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2_when1v2CaseUnspec() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                    .respondent2ClaimResponseType(RespondentResponseType.COUNTER_CLAIM)
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                    .thenReturn("template-id-multiparty");

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor1_when1v2SameSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimSameDefendantSolicitor()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2_when1v2SameSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondentSolicitor2EmailAddress(null)
                    .respondent2SameLegalRepresentative(YesOrNo.YES)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .respondentSolicitor2EmailAddress(null)
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();
                when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
                    .thenReturn("template-id-multiparty");
                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    null,
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor1_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondentSolicitor1EmailAddress(null)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    null,
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor1FirstScenerio_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitorOneFirstScenerio_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2FirstScenerio_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2FirstScenerio_when1v2DifferentSolicitorCaseNoCounter() {
                CaseData caseData = CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();
                when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
                    .thenReturn("template-id-multiparty");
                Organisation r2Org = Organisation.builder()
                    .name("org name")
                    .build();
                when(organisationService.findOrganisationById(
                    caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()
                )).thenReturn(Optional.of(r2Org));
                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id-multiparty",
                    Map.of("legalOrgName", r2Org.getName(),
                           CASEMAN_REF, "000DC001",
                           "partyReferences", "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                        "claimReferenceNumber", caseData.getCcdCaseReference().toString()),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitorOne2FirstScenerio_when1v2DifferentSolicitorCase() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .multiPartyClaimSameDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YesOrNo.NO)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id-multiparty",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

        }

        @Nested
        class TwoVsOneScenario {

            @Test
            void shouldNotifyDefendantSolicitor1_when2v1Case() {
                when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                    .thenReturn("template-id");
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentCounterClaim()
                    .respondent1ClaimResponseTypeToApplicant2(PART_ADMISSION)
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }
        }

        @Nested
        class SpecCounterClaimScenario {

            @Test
            void shouldNotifyDefendantSolicitor1_when1v1CounterClaimCase() {
                when(notificationsProperties.getRespondentSolicitorCounterClaimForSpec())
                    .thenReturn("template-id");

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .build();
                caseData = caseData
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId(
                            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE").build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMapSpec(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyDefendantSolicitor2_when1v2CounterClaimCase() {
                when(notificationsProperties.getRespondentSolicitorCounterClaimForSpec())
                    .thenReturn("template-id");
                when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.empty());

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    .build();
                caseData = caseData
                    .toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId(
                            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE").build())
                    .build();

                when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.empty());

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMapSpec(caseData),
                    "defendant-response-case-handed-offline-respondent-notification-000DC001"
                );
            }
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            );
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue()
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + caseData.getRespondent1ClaimResponseTypeToApplicant2())
                    .concat(" against " + caseData.getApplicant2().getPartyName()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            );
        } else {
            //1v2 template is used and expects different data
            return Map.of(
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseType().getDisplayedValue(),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                CASEMAN_REF, "000DC001"
            );
        }
    }

    private Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
        return Map.of(
            "defendantLR", "Signer Name",
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE").build()).build())).isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE").build()).build())).isEqualTo(TASK_ID_RESPONDENT2);

    }

    @Test
    void shouldReturnPropertiesSpec1v2DiffSol2_whenInvoked() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build();

        assertThat(caseHandledOfflineRespondentSolicitorSpecNotifier.addPropertiesSpec1v2DiffSol(caseData,
            CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR2))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber", "1594901956117591");
    }
}
