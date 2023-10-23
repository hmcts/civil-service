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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    CreateSDORespondent2NotificationHandler.class,
    JacksonAutoConfiguration.class,
    CreateSDORespondent2LRNotificationSender.class,
    CreateSDORespondent2LiPNotificationSender.class
})
public class CreateSDORespondent2NotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private CreateSDORespondent2NotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
            when(notificationsProperties.getSdoOrderedEA()).thenReturn("template-id-ea");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 1")
                                 .partyEmail("company@email.com")
                                 .build())
                .respondent2Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "create-sdo-respondent-2-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedEA() {
            when(featureToggleService.isEarlyAdoptersEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 1")
                                 .partyEmail("company@email.com")
                                 .build())
                .respondent2Represented(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id-ea",
                getNotificationDataMap(caseData),
                "create-sdo-respondent-2-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("Company 2")
                                 .partyEmail("company@email.com")
                                 .build())
                .respondent2Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "company@email.com",
                "template-id",
                Map.of(
                    CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                    CLAIM_LEGAL_ORG_NAME_SPEC, "Company 2"
                ),
                "create-sdo-respondent-2-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                 .request(CallbackRequest.builder().eventId(
                                                         "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED")
                                                              .build()).build()))
            .isEqualTo("CreateSDONotifyRespondentSolicitor2");
    }
}



