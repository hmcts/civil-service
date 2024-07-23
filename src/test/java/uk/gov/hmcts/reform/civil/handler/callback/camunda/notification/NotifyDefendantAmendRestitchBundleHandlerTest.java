package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@SpringBootTest(classes = {
    NotifyDefendantAmendRestitchBundleHandler.class,
    JacksonAutoConfiguration.class
})
class NotifyDefendantAmendRestitchBundleHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    NotifyDefendantAmendRestitchBundleHandler handler;

    @Test
    void shouldSendEmailWhenAllDataIsCorrectAndNotBilingual() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build()).respondent1Represented(YesOrNo.NO).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "respondentLip@example.com",
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

    }

    @Test
    void shouldSendEmailWhenAllDataIsCorrectAndBilingual() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)

            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build()).respondent1Represented(YesOrNo.NO).build();
        caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
                                                         .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                                     .respondent1ResponseLanguage(Language.BOTH.toString())
                                                                                     .build())
                                                         .build()).build();

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(
            BILINGUAL_TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "respondentLip@example.com",
            BILINGUAL_TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

    }

    @Test
    void shouldNotSendEmailWhenAllDataIsCorrectAndNotLIP() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build()).respondent1Represented(YesOrNo.YES).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService, never()).sendMail(any(), any(), any(), any());

    }

    @Test
    void shouldNotSendEmailWhenDefendantEmailIsNull() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail(null).build()).respondent1Represented(YesOrNo.NO).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService, never()).sendMail(any(), any(), any(), any());

    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, "Jack Jackson",
            CLAIMANT_V_DEFENDANT, "John Doe V Jack Jackson"
        );
    }

}

