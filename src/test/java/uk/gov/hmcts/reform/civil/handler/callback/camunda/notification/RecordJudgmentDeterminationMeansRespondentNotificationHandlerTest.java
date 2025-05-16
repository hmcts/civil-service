package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class RecordJudgmentDeterminationMeansRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private RecordJudgmentDeterminationMeansRespondentNotificationHandler handler;
    @Mock
    private OrganisationService organisationService;
    private static final String ORG_NAME_RESPONDENT1 = "Org1";
    private static final String ORG_NAME_RESPONDENT2 = "Org2";
    public static final String TASK_ID_RESPONDENT1 = "RecordJudgmentNotifyRespondent1";
    public static final String TASK_ID_RESPONDENT2 = "RecordJudgmentNotifyRespondent2";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvoked() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .respondentSolicitor1EmailAddress("respondent1@example.com")
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .legacyCaseReference("000DC001")
                .ccdCaseReference(12345L)
                .addRespondent2(YesOrNo.YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test")
                                 .individualLastName("Test Lastname")
                                 .individualFirstName("Test Firstname").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test2")
                                 .individualLastName("Test2 Lastname")
                                 .individualFirstName("Test2 Firstname").build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                            .organisationID(ORG_NAME_RESPONDENT1).build()).build()).build();

            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name(ORG_NAME_RESPONDENT1).build()));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name()).build()
            ).build();

            when(notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate()).thenReturn("template-id");
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent1@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .respondentSolicitor1EmailAddress("respondent1@example.com")
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .legacyCaseReference("000DC001")
                .ccdCaseReference(12345L)
                .addRespondent2(YesOrNo.YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test")
                                 .individualLastName("Test Lastname")
                                 .individualFirstName("Test Firstname").build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                                 .partyName("Test2")
                                 .individualLastName("Test2 Lastname")
                                 .individualFirstName("Test2 Firstname").build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                             .organisationID(ORG_NAME_RESPONDENT2).build()).build()).build();
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name(ORG_NAME_RESPONDENT2).build()));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT.name()).build()
            ).build();

            when(notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate()).thenReturn("template-id");
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondent2@example.com",
                "template-id",
                getNotificationDataMapRespondent2(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
            caseData = caseData.toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1").individualLastName("ApplicantLastName").partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1").individualFirstName("Respondent1").individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .respondent1Represented(null)
                .legacyCaseReference("000DC001")
                .specRespondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name()).build()
            ).build();

            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentLip@example.com",
                "template-id",
                addPropertiesLip(caseData),
                "record-judgment-determination-means-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                LEGAL_ORG_NAME, ORG_NAME_RESPONDENT1,
                DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference(),
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapRespondent2(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                LEGAL_ORG_NAME, ORG_NAME_RESPONDENT2,
                DEFENDANT_NAME, NotificationUtils.getDefendantNameBasedOnCaseType(caseData),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference(),
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        private Map<String, String> addPropertiesLip(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);
    }
}
