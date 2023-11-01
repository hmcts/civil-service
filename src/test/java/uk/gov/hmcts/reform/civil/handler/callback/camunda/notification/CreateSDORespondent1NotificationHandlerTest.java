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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    CreateSDORespondent1NotificationHandler.class,
    JacksonAutoConfiguration.class,
    CreateSDORespondent1LRNotificationSender.class,
    CreateSDORespondent1LiPNotificationSender.class
})

public class CreateSDORespondent1NotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private CreateSDORespondent1NotificationHandler handler;
    private static final String DEFENDANT_EMAIL = "respondent@example.com";
    private static final String LEGACY_REFERENCE = "create-sdo-respondent-1-notification-000DC001";
    private static final String DEFENDANT_NAME = "respondent";
    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_EA = "template-id-EA";
    private static final String ORG_NAME = "Signer Name";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSdoOrdered()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getSdoOrderedEA()).thenReturn(TEMPLATE_ID_EA);
            when(notificationsProperties.getSdoOrderedSpecBilingual()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getSdoOrderedSpec()).thenReturn(TEMPLATE_ID);
            when(notificationsProperties.getSdoOrderedSpecEA()).thenReturn(TEMPLATE_ID_EA);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
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
                getNotificationDataMap(caseData),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedEA() {
            when(featureToggleService.isEarlyAdoptersEnabled()).thenReturn(true);
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
                TEMPLATE_ID_EA,
                getNotificationDataMap(caseData),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
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
            when(featureToggleService.isEarlyAdoptersEnabled()).thenReturn(true);
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
                TEMPLATE_ID_EA,
                getNotificationDataLipMap1(caseData),
                LEGACY_REFERENCE
            );
        }

        @Test
        void shouldNotifyRespondentLiPWithBilingual_whenDefendantResponseIsBilingual() {
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
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataLipMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONDENT_NAME, caseData.getRespondent1().getPartyName()
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataLipMap1(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getRespondent1().getPartyName()
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


