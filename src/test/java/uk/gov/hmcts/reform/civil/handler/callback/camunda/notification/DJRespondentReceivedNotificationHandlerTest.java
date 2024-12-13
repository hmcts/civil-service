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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DJRespondentReceivedNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_EMAIL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_EMAIL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class DJRespondentReceivedNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DJRespondentReceivedNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedAnd1v1() {
            when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived())
                .thenReturn("test-template-received-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "test-template-received-id",
                getNotificationDataMap1v1(caseData),
                "default-judgment-respondent-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedAnd1v2AndBothSelected() {
            when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived())
                .thenReturn("test-template-received-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            //send Received email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YES)
                .defendantDetailsSpec(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build())
                                      .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "test-template-received-id",
                getNotificationDataMap1v2(caseData),
                "default-judgment-respondent-received-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedAnd1v2AndNotBothNotSelected() {
            when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested())
                .thenReturn("test-template-requested-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            //send Requested email
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YES)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("steve")
                                                     .build())
                                          .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "test-template-requested-id",
                getNotificationDataMap1v2fail(),
                "default-judgment-respondent-requested-notification-000DC001"
            );
        }

        @Test
        void shouldReturn_whenInvokedAnd1v1AndLRvLiP_NoEmail() {
            //send Received email
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1Represented(null)
                .respondent1(PartyBuilder.builder().company().partyEmail(null).build())
                .addRespondent2(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).version(V_1).build();

            handler.handle(params);

            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void shouldGenerateEmail_whenInvokedAnd1v1AndLRvLiP() {
            when(notificationsProperties.getRespondent1DefaultJudgmentRequestedTemplate())
                .thenReturn("test-template-requested-id");

            //send Received email
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .respondent1Represented(null)
                .respondent1(PartyBuilder.builder().company().build())
                .addRespondent2(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).version(V_1).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "company@email.com",
                "test-template-requested-id",
                getNotificationDataMapLRvLip(),
                "default-judgment-respondent-requested-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapLRvLip() {
            return Map.of(
                CLAIM_NUMBER_INTERIM, CASE_ID.toString(),
                DEFENDANT_NAME_INTERIM, "Company ltd",
                APPLICANT_ONE_NAME, "Mr. John Rambo",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap1v1(CaseData caseData) {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, CASE_ID.toString(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        private Map<String, String> getNotificationDataMap1v2(CaseData caseData) {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, CASE_ID.toString(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_EMAIL, "Test Org Name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        private Map<String, String> getNotificationDataMap1v2fail() {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, CASE_ID.toString(),
                DEFENDANT_NAME, "steve",
                CLAIMANT_EMAIL, "Test Org Name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest
                                                                                         .builder().eventId(
                "NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED").build()).build())).isEqualTo(TASK_ID);
    }
}
