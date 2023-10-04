package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
public class BreathingSpaceEnteredLIPNotificationHandlerTest {

    @InjectMocks
    private BreathingSpaceEnteredLIPNotificationHandler handler;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;

    private static String templateId = "templateId";

    public void notifyApplicant1BreathingSpace() {

        Mockito.when(notificationsProperties.getNotifyApplicantLRMediationAgreementTemplate())
            .thenReturn(templateId);
        CaseData caseData = createCaseData();
        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_APPLICANT1_BREATHING_SPACE_ENTER,
            caseData
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq("applicant1@gmail.com"),
            eq(templateId),
            eq(createExpectedTemplateProperties()),
            eq("notify-breathing-space-lip-legacy ref")
        );
    }

    public void notifyRespondentBreathingSpace() {

        Mockito.when(notificationsProperties.getNotifyApplicantLRMediationAgreementTemplate())
            .thenReturn(templateId);
        CaseData caseData = createCaseData();
        CallbackParams params = createCallbackParams(
            CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_ENTER,
            caseData
        );

        handler.handle(params);

        Mockito.verify(notificationService).sendMail(
            eq("respondent@gmail.com"),
            eq(templateId),
            eq(createExpectedTemplateProperties()),
            eq("notify-breathing-space-lip-legacy ref")
        );
    }

    private CaseData createCaseData() {
        return CaseData.builder()
            .legacyCaseReference("legacy ref")
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("mr")
                            .individualFirstName("applicant1")
                            .individualLastName("lip")
                            .partyEmail("applicant1@gmail.com")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("mr")
                             .individualFirstName("respondent")
                             .individualLastName("lip")
                             .partyEmail("respondent@gmail.com")
                             .build())
            .build();
    }

    private CallbackParams createCallbackParams(CaseEvent caseEvent, CaseData caseData) {
        return CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .eventId(caseEvent.name())
                         .build())
            .build();
    }

    private Map<String, String> createExpectedTemplateProperties() {
        return Map.of(
            "defendantName", "mr respondent lip",
            "claimReferenceNumber", "legacy ref",
            "claimantName", "mr applicant1 lip"
        );
    }
}
