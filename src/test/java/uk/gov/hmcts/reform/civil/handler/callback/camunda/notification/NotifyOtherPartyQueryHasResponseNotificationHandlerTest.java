package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_PARTY_QUERY_HAS_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotifyOtherPartyQueryHasResponseNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private QueryManagementCamundaService runtimeService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Captor
    private ArgumentCaptor<String> targetEmail;

    @Captor
    private ArgumentCaptor<String> emailTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;

    @InjectMocks
    private NotifyOtherPartyQueryHasResponseNotificationHandler handler;

    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_PUBLIC_QUERY_ID = "template-public-query-id";
    private static final String TEMPLATE_ID_LIP = "template-public-query-id-lip";
    private static final String TEMPLATE_ID_LIP_WELSH = "template-public-query-id-welsh";

    @Nested
    class LrNotifications {

        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(any()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getNotifyOtherPartyQueryResponseReceived()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getQueryLrPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_QUERY_ID);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
        }

        @ParameterizedTest
        @CsvSource({
            "APPLICANTSOLICITORONE, respondent1@email.com, false",
            "RESPONDENTSOLICITORONE, applicant@email.com, false",
            "APPLICANTSOLICITORONE, respondent1@email.com, true",
            "RESPONDENTSOLICITORONE, applicant@email.com, true"
        })
        void shouldNotifyOtherParty_whenQueryResponseOnCase_OneRespondentRepresentative(String caseRole, String email, String toggle) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(Boolean.valueOf(toggle));
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("7")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries1v1();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                email,
                Boolean.parseBoolean(toggle) ? TEMPLATE_PUBLIC_QUERY_ID : TEMPLATE_ID,
                getNotificationDataMap(caseData, false),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @CsvSource({
            "RESPONDENTSOLICITORTWO, applicant@email.com, false",
            "RESPONDENTSOLICITORONE, applicant@email.com, false",
            "RESPONDENTSOLICITORTWO, applicant@email.com, true",
            "RESPONDENTSOLICITORONE, applicant@email.com, true",
        })
        void shouldNotifyOtherParty_whenQueryResponseOnCase_OneRespondentRepresentative_applicantIsOtherParty(String caseRole, String email, String toggle) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(Boolean.valueOf(toggle));
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("11")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries1v2SameSol();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                email,
                Boolean.parseBoolean(toggle) ? TEMPLATE_PUBLIC_QUERY_ID : TEMPLATE_ID,
                getNotificationDataMap(caseData, false),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @CsvSource({
            "RESPONDENTSOLICITORTWO, applicant@email.com, respondent1@email.com, false",
            "RESPONDENTSOLICITORONE, applicant@email.com, respondent2@email.com, false",
            "APPLICANTSOLICITORONE, respondent1@email.com, respondent2@email.com, false",
            "RESPONDENTSOLICITORTWO, applicant@email.com, respondent1@email.com, true",
            "RESPONDENTSOLICITORONE, applicant@email.com, respondent2@email.com, true",
            "APPLICANTSOLICITORONE, respondent1@email.com, respondent2@email.com, true",
        })
        void shouldNotifyOtherParty_whenQueryResponseOnCase_TwoRespondentRepresentative(String caseRole, String email, String emailDef2, String toggle) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(Boolean.valueOf(toggle));
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("11")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries1v2DiffSol();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            if (emailDef2 == null) {
                verify(notificationService).sendMail(
                    email,
                    Boolean.parseBoolean(toggle) ? TEMPLATE_PUBLIC_QUERY_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, false),
                    "other-party-response-to-query-notification-000DC001"
                );
            }

            if (nonNull(emailDef2)) {
                verify(notificationService, times(2)).sendMail(
                    targetEmail.capture(),
                    emailTemplate.capture(),
                    notificationDataMap.capture(),
                    reference.capture()
                );

                assertThat(targetEmail.getAllValues().get(0)).isEqualTo(email);
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(Boolean.parseBoolean(toggle) ? TEMPLATE_PUBLIC_QUERY_ID : TEMPLATE_ID);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData, false));
                assertThat(reference.getAllValues().get(0)).isEqualTo(
                    "other-party-response-to-query-notification-000DC001");

                assertThat(targetEmail.getAllValues().get(1)).isEqualTo(emailDef2);
                assertThat(emailTemplate.getAllValues().get(1)).isEqualTo(Boolean.parseBoolean(toggle) ? TEMPLATE_PUBLIC_QUERY_ID : TEMPLATE_ID);
                assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMap(caseData, false));
                assertThat(reference.getAllValues().get(1)).isEqualTo(
                    "other-party-response-to-query-notification-000DC001");
            }
        }

        private CaseData createCaseDataWithMultipleFollowUpQueries1v2SameSol() {
            CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("1")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("5")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(3))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("6")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("2")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("9")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("10")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("11")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORTWO.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("3")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("13")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("14")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("15")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();
            return CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build()
                .toBuilder()
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .email("applicant@email.com")
                                                    .build())
                .respondentSolicitor1EmailAddress("respondent1@email.com")
                .respondentSolicitor2EmailAddress("respondent2@email.com")
                .qmApplicantSolicitorQueries(applicantQuery)
                .qmRespondentSolicitor1Queries(respondent1Query)
                .qmRespondentSolicitor2Queries(respondent2Query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
        }

        private CaseData createCaseDataWithMultipleFollowUpQueries1v1() {
            CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("1")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("5")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(3))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("6")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("2")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("9")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("10")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("11")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            return CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build()
                .toBuilder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .email("applicant@email.com")
                                                    .build())
                .respondentSolicitor1EmailAddress("respondent1@email.com")
                .qmApplicantSolicitorQueries(applicantQuery)
                .qmRespondentSolicitor1Queries(respondent1Query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
        }

        private CaseData createCaseDataWithMultipleFollowUpQueries1v2DiffSol() {
            CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("1")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("5")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(3))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("6")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("7")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("1")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("2")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("9")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("10")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("11")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("2")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();

            CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORTWO.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder()
                        .id("3")
                        .createdBy("LR")
                        .build(),
                    CaseMessage.builder()
                        .id("13")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now().minusHours(2))
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("14")
                        .createdBy("LR")
                        .createdOn(OffsetDateTime.now().minusHours(1))
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("15")
                        .createdBy("admin")
                        .createdOn(OffsetDateTime.now())
                        .parentId("3")
                        .build(),
                    CaseMessage.builder()
                        .id("8")
                        .createdBy("LR")
                        .parentId("80")
                        .createdOn(OffsetDateTime.now().plusDays(1))
                        .build()
                ))
                .build();
            return CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build()
                .toBuilder()
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .email("applicant@email.com")
                                                    .build())
                .respondentSolicitor1EmailAddress("respondent1@email.com")
                .respondentSolicitor2EmailAddress("respondent2@email.com")
                .qmApplicantSolicitorQueries(applicantQuery)
                .qmRespondentSolicitor1Queries(respondent1Query)
                .qmRespondentSolicitor2Queries(respondent2Query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
        }

    }

    @Nested
    class LipOnCaseNotifications {

        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(any()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getNotifyOtherPartyQueryResponseReceived()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getQueryLrPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_QUERY_ID);
            when(notificationsProperties.getQueryLipPublicResponseReceived()).thenReturn(TEMPLATE_ID_LIP);
            when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn(TEMPLATE_ID_LIP_WELSH);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            when(configuration.getRaiseQueryLip()).thenReturn((String) configMap.get("raiseQueryLip"));
            when(configuration.getRaiseQueryLipWelsh()).thenReturn((String) configMap.get("raiseQueryLipWelsh"));
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_OtherPartyLipApplicant(boolean isWelsh) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LR").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();
            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

            CaseData caseData;
            if (isWelsh) {
                caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference("WELSH")
                    .respondentSolicitor1EmailAddress("respondent1@email.com")
                    .queries(query)
                    .businessProcess(BusinessProcess.builder()
                                         .processInstanceId("123")
                                         .build())
                    .build();
            } else {
                caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference("ENGLISH")
                    .respondentSolicitor1EmailAddress("respondent1@email.com")
                    .queries(query)
                    .businessProcess(BusinessProcess.builder()
                                         .processInstanceId("123")
                                         .build())
                    .build();
            }

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                isWelsh ? TEMPLATE_ID_LIP_WELSH : TEMPLATE_ID_LIP,
                getNotificationDataMapLip(true),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_OtherPartyLipRespondent(boolean isWelsh) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LR").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();
            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            CaseData caseData;
            if (isWelsh) {
                caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                    .caseDataLiP(CaseDataLiP.builder()
                                     .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                 .respondent1ResponseLanguage("WELSH").build())
                                     .build())
                    .respondent1Represented(YesOrNo.NO)
                    .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                    .queries(query)
                    .businessProcess(BusinessProcess.builder().processInstanceId("123").build())
                    .build();
            } else {
                caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                    .caseDataLiP(CaseDataLiP.builder()
                                     .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                 .respondent1ResponseLanguage("ENGLSH").build())
                                     .build())
                    .respondent1Represented(YesOrNo.NO)
                    .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                    .queries(query)
                    .businessProcess(BusinessProcess.builder().processInstanceId("123").build())
                    .build();
            }

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                isWelsh ? TEMPLATE_ID_LIP_WELSH : TEMPLATE_ID_LIP,
                getNotificationDataMapLip(false),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_OtherPartyLrApplicant() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.DEFENDANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("DEFENDANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_PUBLIC_QUERY_ID,
                getNotificationDataMap(caseData, true),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_OtherPartyLrApplicant_atStateClaimIssued() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.DEFENDANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("DEFENDANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService, times(0)).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_PUBLIC_QUERY_ID,
                getNotificationDataMap(caseData, true),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_OtherPartyLrRespondent() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.CLAIMANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("CLAIMANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_PUBLIC_QUERY_ID,
                getNotificationDataMap(caseData, true),
                "other-party-response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyOtherParty_whenQueryResponseOnLipCase_TwoRespondentRepresentative() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.CLAIMANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("CLAIMANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseFullDefenceReceived().build().toBuilder()
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                targetEmail.capture(),
                emailTemplate.capture(),
                notificationDataMap.capture(),
                reference.capture()
            );

            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("respondentsolicitor@example.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(TEMPLATE_PUBLIC_QUERY_ID);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData, true));
            assertThat(reference.getAllValues().get(0)).isEqualTo("other-party-response-to-query-notification-000DC001");

            assertThat(targetEmail.getAllValues().get(1)).isEqualTo("respondentsolicitor2@example.com");
            assertThat(emailTemplate.getAllValues().get(1)).isEqualTo(TEMPLATE_PUBLIC_QUERY_ID);
            assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMap(caseData, true));
            assertThat(reference.getAllValues().get(1)).isEqualTo("other-party-response-to-query-notification-000DC001");
        }

        @Test
        void shouldNotNotifyOtherParty_whenIsUnpecClaim_and_CaseIssued() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.CLAIMANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("CLAIMANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            verify(notificationService, times(0)).sendMail(
                targetEmail.capture(),
                emailTemplate.capture(),
                notificationDataMap.capture(),
                reference.capture()
            );

        }

        @Test
        void shouldNotNotifyOtherParty_whenIsUnpecClaim_and_awaiting_case_details_notification() {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
            CaseQueriesCollection query = CaseQueriesCollection.builder()
                .roleOnCase(CaseRole.CLAIMANT.toString())
                .caseMessages(wrapElements(
                    CaseMessage.builder().id("3").createdBy("LIP").build(),
                    CaseMessage.builder().id("13").createdBy("admin").createdOn(OffsetDateTime.now().minusHours(2)).parentId("3").build()))
                .build();

            when(runtimeService.getProcessVariables(any())).thenReturn(QueryManagementVariables.builder().queryId("13").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("CLAIMANT"));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build().toBuilder()
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .queries(query)
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            verify(notificationService, times(0)).sendMail(
                targetEmail.capture(),
                emailTemplate.capture(),
                notificationDataMap.capture(),
                reference.capture()
            );

        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData, boolean isLipCase) {
        Map<String, String> properties = new HashMap<>(addCommonProperties(isLipCase));
        properties.put(CLAIM_REFERENCE_NUMBER, "1594901956117591");
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
        properties.put(CASEMAN_REF, "000DC001");
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        return properties;
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip(boolean applicant) {
        Map<String, String> properties = new HashMap<>(addCommonProperties(true));
        properties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, "1594901956117591",
            PARTY_NAME, applicant ? "Mr. John Rambo" : "Mr. Sole Trader"
        ));
        return properties;
    }

    @NotNull
    public Map<String, String> addCommonProperties(boolean isLipCase) {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        if (isLipCase) {
            expectedProperties.put(LIP_CONTACT, configuration.getRaiseQueryLip());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getRaiseQueryLipWelsh());
        } else {
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        }
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(NOTIFY_OTHER_PARTY_QUERY_HAS_RESPONSE);
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId(params)).isEqualTo("NotifyOtherPartyQueryHasResponse");
    }
}
