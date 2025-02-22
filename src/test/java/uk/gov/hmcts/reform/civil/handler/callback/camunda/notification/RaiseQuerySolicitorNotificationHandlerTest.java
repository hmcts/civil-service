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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
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

    @InjectMocks
    private RaiseQuerySolicitorNotificationHandler handler;

    public static final String TASK_ID = "QueryRaisedNotify";
    private static final String TEMPLATE_ID = "template-id";

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById(any()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(notificationsProperties.getQueryRaised()).thenReturn(TEMPLATE_ID);
    }

    @Nested
    class AboutToSubmitCallback {

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
                .businessProcess(BusinessProcess.builder()
                                     .processInstanceId("123")
                                     .build())
                .build();
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, "1594901956117591",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CASEMAN_REF, "000DC001",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
            );
        }
    }
}
