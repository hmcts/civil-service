package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantForSpecNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantForSpecNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimApplicantForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private AcknowledgeClaimApplicantForSpecNotificationHandler handler;
    @Mock
    private OrganisationService organisationService;
    private final String legalOrgName = "legalOrgName";
    private final String partyReferences = "partyReferences";
    @Mock
    private FeatureToggleService toggleService;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder()
                    .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                partyReferences, "Claimant reference: 12345 - Defendant reference: 6789",
                legalOrgName, getApplicantLegalOrganizationName(
                    caseData.getApplicant1OrganisationPolicy()
                        .getOrganisation().getOrganisationID(),
                    caseData
                ),
                RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE),
                CASEMAN_REF, "000DC001"
            );
        }
    }

    public String getApplicantLegalOrganizationName(String id, CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC").build()).build())).isEqualTo(TASK_ID_CC);
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC);
    }
}
