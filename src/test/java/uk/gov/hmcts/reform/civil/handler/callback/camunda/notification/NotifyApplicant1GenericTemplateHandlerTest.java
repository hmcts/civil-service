package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@ExtendWith(MockitoExtension.class)
class NotifyApplicant1GenericTemplateHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    NotifyApplicant1GenericTemplateHandler handler;

    @Test
    void shouldSendGenericEmailWhenAllDataIsCorrectAndNotBilingual() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1Represented(YesOrNo.NO)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "claimant@hmcts.net",
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "generic-notification-lip-000DC001"
        );

    }

    @Test
    void shouldSendGenericEmailWhenAllDataIsCorrectAndNotBilingual_SolicitorAfterNoC() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@claimant.net").build())
            .applicant1Represented(YesOrNo.YES)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(
            TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "solicitor@claimant.net",
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "generic-notification-lip-000DC001"
        );

    }

    @Test
    void shouldSendGenericEmailWhenAllDataIsCorrectAndBilingual() {

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1Represented(YesOrNo.NO)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build()).build();
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(
            BILINGUAL_TEMPLATE_ID);

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        handler.handle(params);

        verify(notificationService).sendMail(
            "claimant@hmcts.net",
            BILINGUAL_TEMPLATE_ID,
            getNotificationDataMap(caseData),
            "generic-notification-lip-000DC001"
        );

    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, "John Doe",
            CLAIMANT_V_DEFENDANT, "John Doe V Jack Jackson"
        );
    }

}
