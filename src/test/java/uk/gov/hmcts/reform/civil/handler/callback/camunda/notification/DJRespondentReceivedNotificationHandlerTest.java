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
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DJRespondentReceivedNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DJRespondentReceivedNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class DJRespondentReceivedNotificationHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private DJRespondentReceivedNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived())
                .thenReturn("test-template-received-id");
            when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested())
                .thenReturn("test-template-requested-id");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedAnd1v1() {
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
                getNotificationDataMap1v2fail(caseData),
                "default-judgment-respondent-requested-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap1v1(CaseData caseData) {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, LEGACY_CASE_REFERENCE,
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }

        private Map<String, String> getNotificationDataMap1v2(CaseData caseData) {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, LEGACY_CASE_REFERENCE,
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_EMAIL, "Test Org Name"

            );
        }

        private Map<String, String> getNotificationDataMap1v2fail(CaseData caseData) {
            return Map.of(
                DEFENDANT_EMAIL, "Test Org Name",
                CLAIM_NUMBER, LEGACY_CASE_REFERENCE,
                DEFENDANT_NAME, "steve",
                CLAIMANT_EMAIL, "Test Org Name"

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
