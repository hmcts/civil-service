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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseApplicantNotificationHandler.TASK_ID_CC_RESP2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseApplicantNotificationHandler.class,
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
    @MockBean
    private FeatureToggleService featureToggleService;

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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
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
                final CaseData finalCaseData = caseData;
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("spec-claimant-template-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec(finalCaseData);
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
                final CaseData finalCaseData = caseData;
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec(finalCaseData);
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
                final CaseData finalCaseData = caseData;
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec(finalCaseData);
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
                final CaseData finalCaseData = caseData;
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("applicantsolicitor@example.com"),
                    ArgumentMatchers.eq("templateImm-id"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec(finalCaseData);
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
                    getNotificationDataMapSpec(caseData),
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
                    "spec-1v1-template-id",
                    getNotificationDataMapSpec(caseData),
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
                    "spec-respondent-template-id-action",
                    getNotificationDataMapSpec(caseData),
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

                final CaseData finalCaseData = caseData;
                verify(notificationService).sendMail(
                    ArgumentMatchers.eq("respondentsolicitor2@example.com"),
                    ArgumentMatchers.eq("spec-respondent-template-id-action"),
                    ArgumentMatchers.argThat(map -> {
                        Map<String, String> expected = getNotificationDataMapSpec(finalCaseData);
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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
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
                    getNotificationDataMap(caseData),
                    "defendant-response-applicant-notification-000DC001"
                );
            }
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
                || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    PARTY_REFERENCES, buildPartiesReferences(caseData),
                    ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
                );
            } else {
                //if there are 2 respondents on the case, concatenate the names together for the template subject line
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    RESPONDENT_NAME,
                    getPartyNameBasedOnType(caseData.getRespondent1())
                        .concat(" and ")
                        .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                    PARTY_REFERENCES, buildPartiesReferences(caseData),
                    ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
                );
            }
        }

        private Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "defendantName", "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
            );
        }

        private Map<String, String> getNotificationDataMapImmediatelySpec(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "defendantName", "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                "payImmediately", "12 FEBRUARY 2023;"

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

    @Test
    void shoulldReturnPartyInformation_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationSecondScenerio_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationThirdScenerio_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationScenerio1_whenCaseEventIsInvoked() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        String formattedDate = formatLocalDate(whenWillPay, DATE);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Mr. John Rambo").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "Mr. John Rambo")
            .containsEntry("payImmediately", formattedDate.toUpperCase());

    }

    @Test
    void shoulldReturnPartyInformationScenerioOne_whenCaseEventIsInvoked() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        String formattedDate = formatLocalDate(whenWillPay, DATE);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Mr. John Rambo").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "Mr. John Rambo")
            .containsEntry("payImmediately", formattedDate.toUpperCase());

    }

    @Test
    void shoulldReturnPartyInformationScenerioTwo_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder().build()
            )
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Mr. John Rambo").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "Mr. John Rambo");
    }

    @Test
    void shoulldReturnPartyInformationScenerioThird_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationScenerioFourth_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationScenerioFifth_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationScenerioSix_whenCaseEventIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "my company");
    }

    @Test
    void shoulldReturnPartyInformationScenerio2_whenCaseEventIsInvoked() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        String formattedDate = formatLocalDate(whenWillPay, DATE);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Mr. John Rambo").build())
            .build();

        assertThat(handler.addPropertiesSpec(caseData,
                                             CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE))
            .containsEntry("legalOrgName", "Signer Name")
            .containsEntry("claimReferenceNumber",  "000DC001")
            .containsEntry("defendantName", "Mr. John Rambo")
            .containsEntry("payImmediately", formattedDate.toUpperCase());;

    }
}
