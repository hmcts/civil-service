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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDateTime;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class QueryResponseSolicitorNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String TASK_ID = "QueryResponseNotify";
    private static final String TEMPLATE_ID = "template-id";
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
    @InjectMocks
    private QueryResponseSolicitorNotificationHandler handler;

    private CaseData createCaseDataWithMultipleFollowUpQueries(LocalDateTime now) {
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
                    .build()
            ))
            .build();
        CaseQueriesCollection applicantLipQuery = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.CLAIMANT.toString())
            .caseMessages(wrapElements(
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
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query)
            .qmApplicantCitizenQueries(applicantLipQuery)
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("123")
                                 .build())
            .build();
    }

    private CaseData createCaseDataWithQueries(LocalDateTime now) {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.APPLICANTSOLICITORONE.toString())
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
                    .build()
            ))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.RESPONDENTSOLICITORONE.toString())
            .caseMessages(wrapElements(
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
                    .build()
            ))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.RESPONDENTSOLICITORTWO.toString())
            .caseMessages(wrapElements(
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
                    .build()
            ))
            .build();
        CaseQueriesCollection defendantLipQuery = CaseQueriesCollection.builder()
            .roleOnCase(CaseRole.DEFENDANT.toString())
            .caseMessages(wrapElements(
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
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query)
            .qmRespondentCitizenQueries(defendantLipQuery)
            .businessProcess(BusinessProcess.builder()
                                 .processInstanceId("123")
                                 .build())
            .build();
    }

    @NotNull
    private Map<String, String> getNotificationDataForLip() {
        return Map.of(
            "partyReferences",
            "Claimant reference: 12345 - Defendant reference: 6789",
            "name",
            "Mr. John Rambo",
            "claimReferenceNumber",
            "1594901956117591",
            "casemanRef",
            "000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData, LocalDateTime now) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, "1594901956117591",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CASEMAN_REF, "000DC001",
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            QUERY_DATE, formatLocalDate(now.toLocalDate(), DATE)
        );
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setUp() {
            when(organisationService.findOrganisationById(any()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(notificationsProperties.getQueryResponseReceived()).thenReturn(TEMPLATE_ID);
        }

        @Test
        void shouldNotifyApplicantLR_whenResponseToQueryReceivedMultipleFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("7")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now.minusHours(1)),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1LR_whenResponseToQueryReceivedMultipleFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("11")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now.minusHours(1)),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2LR_whenResponseToQueryReceivedMultipleFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("15")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithMultipleFollowUpQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent2@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now.minusHours(1)),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLR_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("5")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicant@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1LR_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("9")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2LR_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("5")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent2@email.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData, now),
                "response-to-query-notification-000DC001"
            );
        }
    }

    @Nested
    class EmailNotificationsForLip {
        @Test
        void shouldNotifyClaimantLip_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("5")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            when(notificationsProperties.getQueryLipResponseReceivedEnglish()).thenReturn(TEMPLATE_ID);
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE_ID,
                getNotificationDataForLip(),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantLipForBilingual_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("5")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.CLAIMANT.toString()));
            when(notificationsProperties.getQueryLipResponseReceivedWelsh()).thenReturn(TEMPLATE_ID);
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference(Language.BOTH.toString()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE_ID,
                getNotificationDataForLip(),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantLip_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("15")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.DEFENDANT.toString()));
            when(notificationsProperties.getQueryLipResponseReceivedEnglish()).thenReturn(TEMPLATE_ID);
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder()
                .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                Map.of(
                    "partyReferences",
                    "Claimant reference: 12345 - Defendant reference: 6789",
                    "name",
                    "Mr. Sole Trader",
                    "claimReferenceNumber",
                    "1594901956117591",
                    "casemanRef",
                    "000DC001"
                ),
                "response-to-query-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantLipBilingual_whenResponseToQueryReceivedNoFollowUpQueries() {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("15")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(
                any(),
                any()
            )).thenReturn(List.of(CaseRole.DEFENDANT.toString()));
            when(notificationsProperties.getQueryLipResponseReceivedWelsh()).thenReturn(TEMPLATE_ID);
            LocalDateTime now = LocalDateTime.now();
            CaseData caseData = createCaseDataWithQueries(now);
            caseData = caseData.toBuilder()
                .defendantUserDetails(IdamUserDetails.builder().email("sole.trader@email.com").build())
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(
                    RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                Map.of(
                    "partyReferences",
                    "Claimant reference: 12345 - Defendant reference: 6789",
                    "name",
                    "Mr. Sole Trader",
                    "claimReferenceNumber",
                    "1594901956117591",
                    "casemanRef",
                    "000DC001"
                ),
                "response-to-query-notification-000DC001"
            );
        }
    }
}
