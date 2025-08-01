package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_NEW_DEFENDANT_SOLICITOR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_FORMER_SOLICITOR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ChangeOfRepresentationNotificationHandler.TASK_ID_NOTIFY_OTHER_SOLICITOR_2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ChangeOfRepresentationNotificationHandlerTest extends BaseCallbackHandlerTest {

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

    private ChangeOfRepresentationNotificationHandler handler;

    private ObjectMapper objectMapper;

    public static final String REFERENCE = "notice-of-change-000DC001";
    private static final String PREVIOUS_SOL = "Previous solicitor";
    private static final String PREVIOUS_SOL_TEMPLATE = "former-sol-template-id";
    private static final String CASE_TITLE = "Mr. John Rambo v Mr. Sole Trader";
    private static final String NEW_SOLICITOR = "New solicitor";
    private static final String OTHER_SOLICITOR = "Other solicitor";
    private static final String OTHER_SOLICITOR_2 = "Other solicitor2";
    private static final String OTHER_SOL_TEMPLATE = "other-sol-template-id";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ChangeOfRepresentationNotificationHandler(notificationService, notificationsProperties,
                                                               organisationService, objectMapper, configuration, featureToggleService);
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(organisationService.findOrganisationById("Previous-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
            when(organisationService.findOrganisationById("New-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Nested
        class NotifyPreviousSolicitorEvent {

            @Test
            void shouldNotifyFormerSolicitor_whenInvoked() {
                when(organisationService.findOrganisationById("QWERTY A"))
                    .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));
                when(organisationService.findOrganisationById("Previous-sol-id"))
                    .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
                when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(PREVIOUS_SOL_TEMPLATE);
                Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
                when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));

                CaseData caseData =
                    CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1().build();

                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR,
                    PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                    CASEMAN_REF, "000DC001",
                    LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
                    "reference", "1594901956117591"
                ));

                expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
                expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
                expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
                expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
                expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
                expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_FORMER_SOLICITOR.name()).build()).build();
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
                when(organisationService.findOrganisationById("QWERTY A"))
                    .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));

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
                when(organisationService.findOrganisationById("QWERTY A"))
                    .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));

                when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(OTHER_SOL_TEMPLATE);

                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .build();

                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR,
                    PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                    CASEMAN_REF, "000DC001",
                    LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
                    "reference", "1594901956117591"
                ));

                expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
                expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
                expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
                expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
                expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
                expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());

                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();
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
        class NotifyNewDefendantLrEvent {
            @Test
            void newDefendantLrForLipVsLRScenario() {
                when(notificationsProperties.getNotifyNewDefendantSolicitorNOC()).thenReturn(
                    OTHER_SOL_TEMPLATE);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .applicant1Represented(YesOrNo.NO)
                    .build();

                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, "LiP",
                    PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                    CASEMAN_REF, "000DC001",
                    LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
                    "reference", "1594901956117591"
                ));

                expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
                expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
                expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
                expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
                expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
                expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
                expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
                expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_NEW_DEFENDANT_SOLICITOR.name()).build()).build();
                handler.handle(params);

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
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
                when(organisationService.findOrganisationById("QWERTY R2"))
                    .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR_2).build()));

                when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(OTHER_SOL_TEMPLATE);
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                    .build();
                Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    CASE_NAME, CASE_TITLE,
                    ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
                    CCD_REF, caseData.getCcdCaseReference().toString(),
                    FORMER_SOL, PREVIOUS_SOL,
                    NEW_SOL, NEW_SOLICITOR,
                    OTHER_SOL_NAME, OTHER_SOLICITOR_2,
                    PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                    CASEMAN_REF, "000DC001",
                    LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
                    "reference", "1594901956117591"
                ));

                expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
                expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
                expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
                expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
                expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
                expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());

                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                    .request(CallbackRequest.builder()
                                 .eventId(NOTIFY_OTHER_SOLICITOR_2.name()).build()).build();

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

    @Nested
    class AboutToSubmitCallbackLip {

        @BeforeEach
        void setup() {
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
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
        }

        @Test
        void shouldNotifyForOtherPartiesLipBilingualForRespondentSolicitorChange() {
            when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()).thenReturn(
                OTHER_SOL_TEMPLATE);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.WELSH.toString())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();
            handler.handle(params);

            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                "Defendant Name",
                "Mr. Sole Trader",
                "claim16DigitNumber",
                "1594901956117591",
                "claimantName",
                "Mr. John Rambo",
                "claimnumber",
                "000DC001"
            ));
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());

            verify(notificationService).sendMail(
                "rambo@email.com",
                OTHER_SOL_TEMPLATE,
                expectedProperties,
                REFERENCE
            );
        }

        @Test
        void shouldNotifyForOtherPartiesLipForRespondentSolicitorChange() {
            when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate()).thenReturn(
                OTHER_SOL_TEMPLATE);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_OTHER_SOLICITOR_1.name()).build()).build();
            handler.handle(params);

            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                "Defendant Name",
                "Mr. Sole Trader",
                "claim16DigitNumber",
                "1594901956117591",
                "claimantName",
                "Mr. John Rambo",
                "claimnumber",
                "000DC001"
            ));
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());

            verify(notificationService).sendMail(
                "rambo@email.com",
                OTHER_SOL_TEMPLATE,
                expectedProperties,
                REFERENCE
            );
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
