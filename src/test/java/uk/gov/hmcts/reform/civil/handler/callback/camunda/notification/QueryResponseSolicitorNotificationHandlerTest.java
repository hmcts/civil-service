package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
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
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QueryResponseSolicitorNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TASK_ID = "QueryResponseNotify";
    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_LIP = "template-id-lip";
    private static final String TEMPLATE_ID_LIP_WELSH = "template-id-lip-welsh";
    private static final String TEMPLATE_PUBLIC_LIP_ID = "template-public-lip-id";
    private static final String TEMPLATE_PUBLIC_WELSH_LIP_ID = "template-public-welsh-lip-id";
    private static final String TEMPLATE_PUBLIC_LR_ID = "template-public-lr-id";
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
    @InjectMocks
    private QueryResponseSolicitorNotificationHandler handler;

    private CaseData createCaseDataWithMultipleFollowUpQueries(OffsetDateTime now) {
        CaseQueriesCollection caseQueriesCollection = CaseQueriesCollection.builder()
                                                                           .caseMessages(wrapElements(
                                                                                   CaseMessage.builder()
                                                                                              .id("1")
                                                                                              .createdBy("LR")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("5")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now.minusHours(3))
                                                                                              .parentId("1")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("6")
                                                                                              .createdBy("LR")
                                                                                              .createdOn(now.minusHours(2))
                                                                                              .parentId("1")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("7")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now.minusHours(1))
                                                                                              .parentId("1")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("7")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now)
                                                                                              .parentId("1")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("8")
                                                                                              .createdBy("LR")
                                                                                              .parentId("80")
                                                                                              .createdOn(now.plusDays(1))
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("2")
                                                                                              .createdBy("LR")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("9")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now.minusHours(2))
                                                                                              .parentId("2")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("10")
                                                                                              .createdBy("LR")
                                                                                              .createdOn(now.minusHours(1))
                                                                                              .parentId("2")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("11")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now)
                                                                                              .parentId("2")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("8")
                                                                                              .createdBy("LR")
                                                                                              .parentId("80")
                                                                                              .createdOn(now.plusDays(1))
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("3")
                                                                                              .createdBy("LR")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("13")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now.minusHours(2))
                                                                                              .parentId("3")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("14")
                                                                                              .createdBy("LR")
                                                                                              .createdOn(now.minusHours(1))
                                                                                              .parentId("3")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("15")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now)
                                                                                              .parentId("3")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("8")
                                                                                              .createdBy("LR")
                                                                                              .parentId("80")
                                                                                              .createdOn(now.plusDays(1))
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("5")
                                                                                              .createdBy("Lip")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("13")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now.minusHours(2))
                                                                                              .parentId("5")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("14")
                                                                                              .createdBy("Lip")
                                                                                              .createdOn(now.minusHours(1))
                                                                                              .parentId("5")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("15")
                                                                                              .createdBy("admin")
                                                                                              .createdOn(now)
                                                                                              .parentId("5")
                                                                                              .build(),
                                                                                   CaseMessage.builder()
                                                                                              .id("8")
                                                                                              .createdBy("LR")
                                                                                              .parentId("80")
                                                                                              .createdOn(now.plusDays(1))
                                                                                              .build()
                                                                           ))
                                                                           .build();

        return CaseDataBuilder.builder().atStateClaimIssued().build()
                              .toBuilder()
                              .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                                             .email("applicant@email.com")
                                                                             .build())
                              .respondentSolicitor1EmailAddress("respondent1@email.com")
                              .respondentSolicitor2EmailAddress("respondent2@email.com")
                              .queries(caseQueriesCollection)
                              .businessProcess(BusinessProcess.builder()
                                                              .processInstanceId("123")
                                                              .build())
                              .build();
    }

    private CaseData createCaseDataWithQueries(OffsetDateTime now) {
        CaseQueriesCollection caseQueries = CaseQueriesCollection.builder()
                                                                 .caseMessages(wrapElements(
                                                                         CaseMessage.builder()
                                                                                    .id("1")
                                                                                    .createdBy("LR")
                                                                                    .createdOn(now)
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("5")
                                                                                    .createdBy("admin")
                                                                                    .createdOn(now)
                                                                                    .parentId("1")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("8")
                                                                                    .createdBy("LR")
                                                                                    .parentId("80")
                                                                                    .createdOn(now.plusDays(1))
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("2")
                                                                                    .createdBy("LR")
                                                                                    .createdOn(now)
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("9")
                                                                                    .createdBy("admin")
                                                                                    .createdOn(now)
                                                                                    .parentId("2")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("8")
                                                                                    .createdBy("LR")
                                                                                    .parentId("80")
                                                                                    .createdOn(now.plusDays(1))
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("3")
                                                                                    .createdBy("LR")
                                                                                    .createdOn(now)
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("13")
                                                                                    .createdBy("admin")
                                                                                    .createdOn(now)
                                                                                    .parentId("3")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("8")
                                                                                    .createdBy("LR")
                                                                                    .parentId("80")
                                                                                    .createdOn(now.plusDays(1))
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("144")
                                                                                    .createdBy("Lip")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("13")
                                                                                    .createdBy("admin")
                                                                                    .createdOn(now.minusHours(2))
                                                                                    .parentId("144")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("14")
                                                                                    .createdBy("Lip")
                                                                                    .createdOn(now.minusHours(1))
                                                                                    .parentId("144")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("15")
                                                                                    .createdBy("admin")
                                                                                    .createdOn(now)
                                                                                    .parentId("144")
                                                                                    .build(),
                                                                         CaseMessage.builder()
                                                                                    .id("8")
                                                                                    .createdBy("Lip")
                                                                                    .parentId("80")
                                                                                    .createdOn(now.plusDays(1))
                                                                                    .build()
                                                                 ))
                                                                 .build();

        return CaseDataBuilder.builder().atStateClaimIssued().build()
                              .toBuilder()
                              .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                                             .email("applicant@email.com")
                                                                             .build())
                              .respondentSolicitor1EmailAddress("respondent1@email.com")
                              .respondentSolicitor2EmailAddress("respondent2@email.com")
                              .queries(caseQueries)
                              .businessProcess(BusinessProcess.builder()
                                                              .processInstanceId("123")
                                                              .build())
                              .build();
    }

    @NotNull
    private Map<String, String> getNotificationDataForLip(LocalDateTime now, boolean publicQmEnabled) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
                "partyReferences",
                "Claimant reference: 12345 - Defendant reference: 6789",
                "name",
                "Mr. John Rambo",
                "claimReferenceNumber",
                "1594901956117591",
                "casemanRef",
                "000DC001",
                QUERY_DATE,
                formatLocalDate(now.toLocalDate(), DATE)
        ));
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        if (publicQmEnabled) {
            expectedProperties.put(LIP_CONTACT, configuration.getRaiseQueryLip());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getRaiseQueryLipWelsh());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
            expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        } else {
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
        }
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData, LocalDateTime now) {
        Map<String, String> expectedProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, "1594901956117591",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CASEMAN_REF, "000DC001",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                QUERY_DATE, formatLocalDate(now.toLocalDate(), DATE)
        ));
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(any()))
                    .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getQueryResponseReceived()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getQueryLrPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_LR_ID);
            when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_WELSH_LIP_ID);
            when(notificationsProperties.getQueryLipPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_LIP_ID);
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
        @ValueSource(booleans = {true, false})
        void shouldNotifyApplicantLR_whenResponseToQueryReceivedMultipleFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("7")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "applicant@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.minusHours(1).toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyRespondent1LR_whenResponseToQueryReceivedMultipleFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("11")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "respondent1@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.minusHours(1).toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyRespondent2LR_whenResponseToQueryReceivedMultipleFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("15")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "respondent2@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.minusHours(1).toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyApplicantLR_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("5")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "applicant@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyRespondent1LR_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("9")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "respondent1@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyRespondent2LR_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("5")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "respondent2@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LR_ID : TEMPLATE_ID,
                    getNotificationDataMap(caseData, now.toLocalDateTime()),
                    "response-to-query-notification-000DC001"
            );
        }
    }

    @Nested
    class EmailNotificationsForLip {

        @BeforeEach
        void setUp() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            when(notificationsProperties.getQueryLipWelshPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_WELSH_LIP_ID);
            when(notificationsProperties.getQueryLipPublicResponseReceived()).thenReturn(TEMPLATE_PUBLIC_LIP_ID);
            when(notificationsProperties.getQueryLipResponseReceivedEnglish()).thenReturn(TEMPLATE_ID_LIP);
            when(notificationsProperties.getQueryLipResponseReceivedWelsh()).thenReturn(TEMPLATE_ID_LIP_WELSH);
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
            when(configuration.getRaiseQueryLip()).thenReturn((String) configMap.get("raiseQueryLip"));
            when(configuration.getRaiseQueryLipWelsh()).thenReturn((String) configMap.get("raiseQueryLipWelsh"));
        }

        @NotNull
        private Map<String, String> getNotificationDataMapLipRes(LocalDateTime now, boolean publicQmEnabled) {
            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                    "partyReferences",
                    "Claimant reference: 12345 - Defendant reference: 6789",
                    "name",
                    "Mr. Sole Trader",
                    "claimReferenceNumber",
                    "1594901956117591",
                    "casemanRef",
                    "000DC001",
                    QUERY_DATE,
                    formatLocalDate(now.toLocalDate(), DATE)
            ));
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            if (publicQmEnabled) {
                expectedProperties.put(LIP_CONTACT, configuration.getRaiseQueryLip());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getRaiseQueryLipWelsh());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
                expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
            } else {
                expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
                expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
                expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
                expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            }
            return expectedProperties;
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyClaimantLip_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("5")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now).toBuilder()
                                                              .applicant1Represented(YesOrNo.NO)
                                                              .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "rambo@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LIP_ID : TEMPLATE_ID_LIP,
                    getNotificationDataForLip(now.toLocalDateTime(), publicQuery),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyClaimantLipForBilingual_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("5")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder()
                               .applicant1Represented(YesOrNo.NO)
                               .claimantBilingualLanguagePreference(Language.BOTH.toString()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "rambo@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_WELSH_LIP_ID : TEMPLATE_ID_LIP_WELSH,
                    getNotificationDataForLip(now.toLocalDateTime(), publicQuery),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyDefendantLip_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("15")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.DEFENDANT.toString()));

            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder()
                               .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                               .respondent1Represented(YesOrNo.NO)
                               .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "sole.trader@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_LIP_ID : TEMPLATE_ID_LIP,
                    getNotificationDataMapLipRes(now.toLocalDateTime(), publicQuery),
                    "response-to-query-notification-000DC001"
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotifyDefendantLipBilingual_whenResponseToQueryReceivedNoFollowUpQueries(boolean publicQuery) {
            when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(publicQuery);
            when(runtimeService.getProcessVariables(any()))
                    .thenReturn(QueryManagementVariables.builder()
                                                        .queryId("15")
                                                        .build());
            when(coreCaseUserService.getUserCaseRoles(
                    any(),
                    any()
            )).thenReturn(List.of(CaseRole.DEFENDANT.toString()));
            OffsetDateTime now = OffsetDateTime.of(2025, 8, 20, 10, 0, 0, 0, ZoneOffset.UTC);
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder()
                               .respondent1Represented(YesOrNo.NO)
                               .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                               .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
                                       RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build())
                               .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                    "sole.trader@email.com",
                    publicQuery ? TEMPLATE_PUBLIC_WELSH_LIP_ID : TEMPLATE_ID_LIP_WELSH,
                    getNotificationDataMapLipRes(now.toLocalDateTime(), publicQuery),
                    "response-to-query-notification-000DC001"
            );
        }
    }
}
