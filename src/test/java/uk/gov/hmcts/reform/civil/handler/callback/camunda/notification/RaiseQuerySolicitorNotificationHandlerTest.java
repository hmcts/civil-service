package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class RaiseQuerySolicitorNotificationHandlerTest extends BaseCallbackHandlerTest {

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
    private RaiseQuerySolicitorNotificationHandler handler;

    public static final String TASK_ID = "QueryRaisedNotify";
    private static final String TEMPLATE_ID = "template-id";

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(any()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getQueryRaised()).thenReturn(TEMPLATE_ID);
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

        @Test
        void shouldNotifyApplicantLR_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("1")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
            CaseData caseData = createCaseDataWithQueries();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "query-raised-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1LR_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("2")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
            CaseData caseData = createCaseDataWithQueries();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "query-raised-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2LR_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("3")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
            CaseData caseData = createCaseDataWithQueries();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent2@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "query-raised-notification-000DC001"
            );
        }
    }

    @Nested
    class AboutToSubmitForLip {
        @BeforeEach
        void setup() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
        }

        @Test
        void shouldNotifyClaimantLip_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId(null)
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            when(notificationsProperties.getQueryRaisedLip()).thenReturn(TEMPLATE_ID);
            CaseData caseData =
                createCaseDataWithQueries().toBuilder()
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("a")
                                    .individualLastName("b").partyEmail("applicant@email.com").build())
                    .applicant1Represented(YesOrNo.NO)
                    .qmLatestQuery(LatestQuery.builder().queryId("4").build())
                    .build();

            CallbackParams params = callbackParamsOf(
                caseData,
                ABOUT_TO_SUBMIT
            );

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMapLip(caseData),
                "query-raised-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantLipBilingual_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId(null)
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            when(notificationsProperties.getQueryRaisedLipBilingual()).thenReturn(TEMPLATE_ID);
            CaseData caseData =
                createCaseDataWithQueries().toBuilder()
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("a")
                                    .individualLastName("b").partyEmail("applicant@email.com").build())
                    .qmLatestQuery(LatestQuery.builder().queryId("4").build())
                    .claimantBilingualLanguagePreference(Language.WELSH.toString())
                    .applicant1Represented(YesOrNo.NO)
                    .build();

            CallbackParams params = callbackParamsOf(
                caseData,
                ABOUT_TO_SUBMIT
            );

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMapLip(caseData),
                "query-raised-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantLipLip_whenApplicantRaisedLatestQuery() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId(null)
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.DEFENDANT.toString()));
            when(notificationsProperties.getQueryRaisedLip()).thenReturn(TEMPLATE_ID);
            CaseData caseData =
                createCaseDataWithQueries().toBuilder()
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("a")
                                     .individualLastName("b").partyEmail("applicant@email.com").build())
                    .qmLatestQuery(LatestQuery.builder().queryId("5").build())
                    .defendantUserDetails(IdamUserDetails.builder().email("applicant@email.com").build())
                    .respondent1Represented(YesOrNo.NO)
                    .build();

            CallbackParams params = callbackParamsOf(
                caseData,
                ABOUT_TO_SUBMIT
            );

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMapLip(caseData),
                "query-raised-notification-000DC001"
            );
        }

    }

    private CaseData createCaseDataWithQueries() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.RESPONDENTSOLICITORTWO.toString())
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();

        CaseQueriesCollection publicQueries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("3")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("2")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("4")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("5")
                                           .build()))
            .build();

        return CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .email("applicant@email.com")
                                                .build())
            .respondentSolicitor1EmailAddress("respondent1@email.com")
            .respondentSolicitor2EmailAddress("respondent2@email.com")
            .qmApplicantSolicitorQueries(applicantQuery)
            .queries(publicQueries)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query)
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("123")
                                 .build())
            .build();
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
        expectedProperties.putAll(Map.of(
            "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
            "name", "a b",
            "claimReferenceNumber", "1594901956117591",
            "casemanRef", "000DC001"
        ));
        return expectedProperties;
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, "1594901956117591",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CASEMAN_REF, "000DC001",
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
        return expectedProperties;
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
