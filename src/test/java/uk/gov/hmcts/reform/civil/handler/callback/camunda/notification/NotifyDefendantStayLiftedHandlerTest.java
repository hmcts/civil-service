package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NotifyDefendantStayLiftedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private NotifyDefendantStayLiftedHandler handler;

    private CaseData caseData;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().individualFirstName("Jim").individualLastName("Jameson").type(Party.Type.INDIVIDUAL).build())
                .build();
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        static Stream<Arguments> provideCaseData() {
            return Stream.of(
                Arguments.of(false, NOTIFY_DEFENDANT_STAY_LIFTED),
                Arguments.of(true, NOTIFY_DEFENDANT2_STAY_LIFTED)
            );
        }

        @ParameterizedTest
        @MethodSource("provideCaseData")
        void sendNotificationShouldSendEmail(boolean isDefendant2, CaseEvent caseEvent) {
            caseData = caseData.toBuilder()
                .respondentSolicitor1EmailAddress("defendant@hmcts.net")
                .respondentSolicitor2EmailAddress("defendant2@hmcts.net")
                .respondent1Represented(YesOrNo.YES)
                .respondent2Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParams.builder().caseData(caseData)
                .request(CallbackRequest.builder().eventId(caseEvent.toString()).build()).build();

            when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");

            CallbackResponse response = handler.sendNotification(params);

            if (isDefendant2) {
                Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
                expectedProperties.put("claimReferenceNumber", "1594901956117591");
                expectedProperties.put("name", "Jim Jameson");
                expectedProperties.put("claimantvdefendant", "Mr. John Rambo V Jack Jackson");
                expectedProperties.put("partyReferences", buildPartiesReferencesEmailSubject(caseData));
                expectedProperties.put("casemanRef", caseData.getLegacyCaseReference());

                verify(notificationService).sendMail(
                    "defendant2@hmcts.net",
                    "solicitor-template",
                    expectedProperties,
                    "stay-lifted-defendant-notification-1594901956117591"
                );
            } else {
                Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
                expectedProperties.put("claimReferenceNumber", "1594901956117591");
                expectedProperties.put("name", "Jack Jackson");
                expectedProperties.put("claimantvdefendant", "Mr. John Rambo V Jack Jackson");
                expectedProperties.put("partyReferences", buildPartiesReferencesEmailSubject(caseData));
                expectedProperties.put("casemanRef", caseData.getLegacyCaseReference());

                verify(notificationService).sendMail(
                    "defendant@hmcts.net",
                    "solicitor-template",
                    expectedProperties,
                    "stay-lifted-defendant-notification-1594901956117591"
                );

            }
            assertNotNull(response);
        }

        private CaseDataBuilder commonCaseData() {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
                .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                                .type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                                 .partyEmail("defendant@hmcts.net")
                                 .type(Party.Type.INDIVIDUAL).build())
                .respondentSolicitor1EmailAddress("solicitor@example.com");
        }

        private CaseData getCaseData(boolean isRespondentLiP, boolean isRespondentBilingual) {
            RespondentLiPResponse respondentLip = RespondentLiPResponse.builder()
                .respondent1ResponseLanguage(isRespondentBilingual ? Language.BOTH.toString()
                                                 : Language.ENGLISH.toString()).build();
            return commonCaseData()
                .respondent1Represented(isRespondentLiP ? YesOrNo.NO : YesOrNo.YES)

                .build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(respondentLip).build())
                .build();
        }

        static Stream<Arguments> provideLipCaseData() {
            return Stream.of(
                Arguments.of(true, true, "bilingual-template"),
                Arguments.of(true, false, "default-template")
            );
        }

        @ParameterizedTest
        @MethodSource("provideLipCaseData")
        void sendNotificationShouldSendLipEmail(boolean isRespondentLiP, boolean isRespondentBilingual, String template) {
            CaseData caseData = getCaseData(isRespondentLiP, isRespondentBilingual);

            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .eventId(CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE.name())
                .build();
            CallbackParams params = CallbackParams.builder()
                .request(callbackRequest)
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .build();

            if (isRespondentLiP && isRespondentBilingual) {
                when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("bilingual-template");
            } else if (isRespondentLiP) {
                when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("default-template");
            } else {
                when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");
            }

            CallbackResponse response = handler.sendNotification(params);

            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put("claimReferenceNumber", "1594901956117591");
            expectedProperties.put("name", "Jack Jackson");
            expectedProperties.put("claimantvdefendant", "John Doe V Jack Jackson");

            verify(notificationService).sendMail(
                isRespondentLiP ? "defendant@hmcts.net" : "solicitor@example.com",
                template,
                expectedProperties,
                "stay-lifted-defendant-notification-1594901956117591"
            );
            assertNotNull(response);

            assertNotNull(response);
        }

        @NotNull
        public Map<String, String> addCommonProperties() {
            Map<String, String> expectedProperties = new HashMap<>();
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            return expectedProperties;
        }

    }

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().individualFirstName("Jim").individualLastName("Jameson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    @Test
    void checkCamundaActivityDefendantTest() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_STAY_LIFTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendantStayLifted", response);
    }

    @Test
    void checkCamundaActivityDefendant2Test() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT2_STAY_LIFTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendant2StayLifted", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(
            List.of(
                NOTIFY_DEFENDANT_STAY_LIFTED,
                NOTIFY_DEFENDANT2_STAY_LIFTED
            ), response
        );
    }
}
