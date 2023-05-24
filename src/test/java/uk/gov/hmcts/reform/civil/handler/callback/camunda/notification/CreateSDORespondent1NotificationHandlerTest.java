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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
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
    @Autowired
    private CreateSDORespondent1NotificationHandler handler;
    private final String defendantEmail = "respondent@example.com";
    private final String legacyCaseReference = "create-sdo-respondent-1-notification-000DC001";
    private final String defendantName = "respondent";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
            when(notificationsProperties.getSdoOrderedSpecBilingual()).thenReturn("template-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
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
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "create-sdo-respondent-1-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLiP_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
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
                "template-id",
                Map.of(
                    CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                    CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getRespondent1().getPartyName()
                ),
                legacyCaseReference
            );
        }

        @Test
        void shouldNotifyRespondentLiPWithBilingual_whenInvoked() {
            Party party = PartyBuilder.builder()
                .individual(defendantName )
                .partyEmail(defendantEmail)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder().
                                 respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
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
                "template-id",
                Map.of(
                    CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                    RESPONDENT_NAME, caseData.getRespondent1().getPartyName()
                ),
                "create-sdo-respondent-1-notification-000DC001"
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
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED").build()).build()))
            .isEqualTo("CreateSDONotifyRespondentSolicitor1");
    }
}


