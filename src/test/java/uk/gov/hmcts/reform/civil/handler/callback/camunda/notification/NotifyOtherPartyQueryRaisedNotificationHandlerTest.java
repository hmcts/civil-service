package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotifyOtherPartyQueryRaisedNotificationHandlerTest extends BaseCallbackHandlerTest {

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

    @Captor
    private ArgumentCaptor<String> targetEmail;

    @Captor
    private ArgumentCaptor<String> emailTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;

    @Captor
    private ArgumentCaptor<String> reference;

    @InjectMocks
    private NotifyOtherPartyQueryRaisedNotificationHandler handler;

    private static final String TEMPLATE_ID = "template-id";

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById(any()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(notificationsProperties.getNotifyOtherPartyQueryRaised()).thenReturn(TEMPLATE_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @CsvSource({
            "APPLICANTSOLICITORONE, respondent1@email.com",
            "RESPONDENTSOLICITORONE, applicant@email.com"
        })
        void shouldNotifyOtherParty_whenQueryRaisedOnCase_OneRespondentRepresentative(String caseRole, String email) {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("1")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseDataWithQueries1v1();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                email,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "a-query-has-been-raised-notification-000DC001"
            );
        }

        @ParameterizedTest
        @CsvSource({
            "RESPONDENTSOLICITORTWO, applicant@email.com",
            "RESPONDENTSOLICITORONE, applicant@email.com",
        })
        void shouldNotifyOtherParty_whenQueryRaisedOnCase_TwoRespondentRepresentative_applicantIsOtherParty(String caseRole, String email) {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("2")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseDataWithQueries1v2SameSol();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(notificationService).sendMail(
                email,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "a-query-has-been-raised-notification-000DC001"
            );
        }

        @ParameterizedTest
        @CsvSource({
            "RESPONDENTSOLICITORTWO, applicant@email.com,",
            "RESPONDENTSOLICITORONE, applicant@email.com,",
            "APPLICANTSOLICITORONE, respondent1@email.com, respondent2@email.com",
        })
        void shouldNotifyOtherParty_whenQueryRaisedOnCase_TwoRespondentRepresentative(String caseRole, String email, String emailDef2) {
            when(runtimeService.getProcessVariables(any()))
                .thenReturn(QueryManagementVariables.builder()
                                .queryId("3")
                                .build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(caseRole));
            CaseData caseData = createCaseData1v2DifferentSolCaseWithQueries();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            handler.handle(params);

            if (emailDef2 == null) {
                verify(notificationService).sendMail(
                    email,
                    TEMPLATE_ID,
                    getNotificationDataMap(caseData),
                    "a-query-has-been-raised-notification-000DC001"
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
                assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(TEMPLATE_ID);
                assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(getNotificationDataMap(caseData));
                assertThat(reference.getAllValues().get(0)).isEqualTo("a-query-has-been-raised-notification-000DC001");

                assertThat(targetEmail.getAllValues().get(1)).isEqualTo(emailDef2);
                assertThat(emailTemplate.getAllValues().get(1)).isEqualTo(TEMPLATE_ID);
                assertThat(notificationDataMap.getAllValues().get(1)).isEqualTo(getNotificationDataMap(caseData));
                assertThat(reference.getAllValues().get(1)).isEqualTo("a-query-has-been-raised-notification-000DC001");
            }
        }

        private CaseData createCaseDataWithQueries1v1() {
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

            return CaseDataBuilder.builder().atStateClaimIssued().build()
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

        private CaseData createCaseDataWithQueries1v2SameSol() {
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
                .respondent1(PartyBuilder.builder().build())
                .respondent2(PartyBuilder.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
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

        private CaseData createCaseData1v2DifferentSolCaseWithQueries() {
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

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, "1594901956117591",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CASEMAN_REF, "000DC001",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
            );
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvents() {
            assertThat(handler.handledEvents()).containsOnly(NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY);
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            assertThat(handler.camundaActivityId(params)).isEqualTo("NotifyOtherPartyQueryRaised");
        }
    }
}
