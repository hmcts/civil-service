package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

@ExtendWith(MockitoExtension.class)
class CreateSDORespondent1NotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    private CreateSDORespondent1NotificationHandler handler;

    @BeforeEach
    void setup() {
        CreateSDORespondent1LRNotificationSender lrNotificationSender =
            new CreateSDORespondent1LRNotificationSender(notificationService, notificationsProperties,
                                                         organisationService, featureToggleService
        );
        CreateSDORespondent1LiPNotificationSender lipNotificationSender =
            new CreateSDORespondent1LiPNotificationSender(notificationService, notificationsProperties,
                                                          featureToggleService
        );
        handler = new CreateSDORespondent1NotificationHandler(lipNotificationSender, lrNotificationSender);
    }

    private static final String DEFENDANT_EMAIL = "respondent@example.com";
    private static final String LEGACY_REFERENCE = "create-sdo-respondent-1-notification-000DC001";
    private static final String DEFENDANT_NAME = "respondent";
    private static final String TEMPLATE_ID = "template-id";
    private static final String ORG_NAME = "Signer Name";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            when(notificationsProperties.getSdoOrdered()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                TEMPLATE_ID,
                getNotificationDataMap(),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondent1().getPartyEmail(),
                TEMPLATE_ID,
                getNotificationDataLipMap1(caseData),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvokedEA() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondent1().getPartyEmail(),
                TEMPLATE_ID,
                getNotificationDataLipMap1(caseData),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentLiPWithBilingual_whenDefendantResponseIsBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(TEMPLATE_ID);

            Party party = PartyBuilder.builder()
                .individual(DEFENDANT_NAME)
                .partyEmail(DEFENDANT_EMAIL)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("BOTH").build()).build())
                .respondent1(party)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondent1().getPartyEmail(),
                TEMPLATE_ID,
                getNotificationDataLipMap(caseData),
                LEGACY_REFERENCE
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataLipMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataLipMap1(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                PARTY_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED").build()).build()))
            .isEqualTo("CreateSDONotifyRespondentSolicitor1");
    }
}
