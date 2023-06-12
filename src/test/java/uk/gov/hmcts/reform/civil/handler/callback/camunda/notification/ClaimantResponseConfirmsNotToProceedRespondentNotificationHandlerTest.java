package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.Task_ID_RESPONDENT_SOL2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler handler;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToSubmitCallback {

        public static final String LEGACY_CASE_REFERENCE = "000DC001";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");
            when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec()).thenReturn("spec-template-id");
            when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn("spec-template-id");
            when(notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec()).thenReturn("spec-template-part-admit-id");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked_spec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(caseData,
                                           CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked_spec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(caseData,
                                           CaseEvent
                                               .NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked_spec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(caseData,
                                           CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithCcEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked_spec_partadmit() {
            when(organisationService.findOrganisationById(
                anyString())).thenReturn(Optional.of(Organisation.builder().name("respondent solicitor org").build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
                .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "spec-template-part-admit-id",
                getNotificationDataMapSpecPartAdmit(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }
    }

    @NotNull
    public Map<String, String> getNotificationDataMapSpec(CaseData caseData, CaseEvent caseEvent) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    @NotNull
    public Map<String, String> getNotificationDataMapSpecPartAdmit(CaseData caseData) {
        return Map.of(
            CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData,  CaseEvent caseEvent) {
        String organisationID;
        organisationID = caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC)
            ? caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getRespondentLegalOrganizationName(CaseData caseData) {
        String id = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);

        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED").build())
                                                 .build())).isEqualTo(Task_ID_RESPONDENT_SOL2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC").build()).build())).isEqualTo(
            TASK_ID_CC);
    }
}
