package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceApplicantSolicitorOneCCSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceApplicantSolicitorOneCCUnspecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceApplicantSolicitorOneSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceApplicantSolicitorOneUnspecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceRespondentSolicitorOneCCSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceRespondentSolicitorOneCCUnspecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceRespondentSolicitorTwoCCSpecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceRespondentSolicitorTwoCCUnspecNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceSolicitorNotifierFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseApplicantNotificationHandler.class,
    FullDefenceSolicitorNotifierFactory.class,
    FullDefenceApplicantSolicitorOneSpecNotifier.class,
    FullDefenceApplicantSolicitorOneUnspecNotifier.class,
    FullDefenceApplicantSolicitorOneCCSpecNotifier.class,
    FullDefenceApplicantSolicitorOneCCUnspecNotifier.class,
    FullDefenceRespondentSolicitorOneCCSpecNotifier.class,
    FullDefenceRespondentSolicitorOneCCUnspecNotifier.class,
    FullDefenceRespondentSolicitorTwoCCSpecNotifier.class,
    FullDefenceRespondentSolicitorTwoCCUnspecNotifier.class,
    JacksonAutoConfiguration.class

})
class DefendantResponseApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private DefendantResponseApplicantNotificationHandler handler;
    @MockBean
    private OrganisationService organisationService;

    @BeforeEach
    void setup() {
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn("template-id");
        when(notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec())
            .thenReturn("spec-claimant-1v2DS-template-id");
        when(notificationsProperties.getClaimantSolicitorDefendantResponseForSpec())
            .thenReturn("spec-claimant-template-id");
        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec()).thenReturn("templateImm-id");
        when(notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction()).thenReturn("spec-respondent-template-id-action");
    }

    @Nested
    class AboutToSubmitCallback {

        @Nested
        class OneVsOneScenario {

            @Test
            void shouldNotifyApplicantSolicitorIn1v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, false),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In1v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, false),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In1v1ScenarioSecondSol_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, false),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyApplicantSolicitorSpec_whenInvoked() {

                LocalDate whenWillPay = LocalDate.now().plusMonths(1);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                    .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                            .whenWillThisAmountBePaid(whenWillPay)
                            .build()
                    )
                    .build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                    .build();

                handler.handle(params);
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("spec-claimant-template-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }

            @Test
            void shouldNotifyCitizenApplicantSpec_whenInvoked() {

                LocalDate whenWillPay = LocalDate.now().plusMonths(1);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                    .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                            .whenWillThisAmountBePaid(whenWillPay)
                            .build()
                    )
                    .build();
                caseData = caseData.toBuilder().applicant1Represented(NO).caseAccessCategory(SPEC_CLAIM).build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                    .build();

                handler.handle(params);
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("rambo@email.com"),
                    ArgumentMatchers.eq("spec-claimant-template-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpecCui();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }

            @Test
            void shouldNotifyApplicantSolicitorSpecImmediately_whenInvoked() {

                LocalDate whenWillPay = LocalDate.now().plusMonths(1);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                    .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                            .whenWillThisAmountBePaid(whenWillPay)
                            .build()
                    )
                    .build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                    .build();

                handler.handle(params);
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }

            @Test
            void shouldNotifyApplicantSolicitorSpecImmediatelyScenerio2_whenInvoked() {

                LocalDate whenWillPay = LocalDate.now().plusDays(5);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                    .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                            .whenWillThisAmountBePaid(whenWillPay)
                            .build()
                    )
                    .build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                    .build();

                handler.handle(params);
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }

            @Test
            void shouldNotifyApplicantSolicitorSpecImmediatelyScenerio3_whenInvoked() {

                LocalDate whenWillPay = LocalDate.now().plusDays(5);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build().toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                    .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                            .whenWillThisAmountBePaid(whenWillPay)
                            .build()
                    )
                    .build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM).build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build())
                    .build();

                handler.handle(params);
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }

            @Test
            void shouldNotifyRespondentSolicitorSpec_whenInvokedWithCcEvent() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged().build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
                    .respondent1DQ(Respondent1DQ.builder().build())
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                            .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "spec-respondent-template-id",
                    getNotificationDataMapSpec(),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitorSpecDef1_whenInvokedWithCcEvent() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged().build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
                    .respondent1DQ(Respondent1DQ.builder().build())
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                            .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "spec-respondent-template-id",
                    getNotificationDataMapPartAdmissionSpec(),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void sendNotificationToSolicitorSpec_shouldNotifyRespondentSolicitorSpecDef1v1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged().build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
                    .respondent1DQ(Respondent1DQ.builder().build())
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                            .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "spec-respondent-template-id",
                    getNotificationDataMapSpec(),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void sendNotificationToSolicitorSpecPart_shouldNotifyRespondentSolicitorSpecDef1v1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged().build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
                    .respondent1DQ(Respondent1DQ.builder().build())
                    .applicant1Represented(NO)
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                            .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "spec-respondent-template-id",
                    getNotificationDataMapSpec(),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitorSpecDef1SecondScenerio_whenInvokedWithCcEvent() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged().build();
                caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
                    .respondent2DQ(Respondent2DQ.builder().build())
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                            .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("respondentsolicitor2@example.com"),
                    ArgumentMatchers.eq("spec-respondent-template-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec();
                        return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                            && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
                    }),
                    ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
                );
            }
        }

        @Nested
        class OneVsTwoScenario {

            @Test
            void shouldNotifyApplicantSolicitorIn1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, true),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, true),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2In1v2Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, true),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        @Nested
        class TwoVsOneScenario {
            @Test
            void shouldNotifyApplicantSolicitorIn2v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, false),
                    "defendant-response-applicant-notification-000DC001"
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1In2v1Scenario_whenV1CallbackInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateRespondentFullDefence()
                    .build();

                CallbackParams params = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                        .build())
                    .build();

                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData, false),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData, boolean is1v2DS) {
            if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
                || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                    ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
                );
            } else {
                //if there are 2 respondents on the case, concatenate the names together for the template subject line
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    RESPONDENT_NAME,
                    getPartyNameBasedOnType(caseData.getRespondent1())
                        .concat(" and ")
                        .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                    PARTY_REFERENCES, is1v2DS ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234"
                    : "Claimant reference: 12345 - Defendant reference: 6789",
                    ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
                );
            }
        }

        private Map<String, String> getNotificationDataMapSpec() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                "defendantName", "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789"
            );
        }

        private Map<String, String> getNotificationDataMapSpecCui() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                "defendantName", "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Mr. John Rambo"
            );
        }

        private Map<String, String> getNotificationDataMapPartAdmissionSpec() {
            return Map.of(
                "defendantName", "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789"
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC_RESP2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC").build()).build())).isEqualTo(TASK_ID_CC_RESP1);
    }
}
