package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class CaseDismissDefendantNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseDismissDefendantNotificationHandler handler;

    private CaseDataBuilder commonCaseData() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .partyEmail("defendant@hmcts.net")
                             .type(Party.Type.INDIVIDUAL).build())
            .respondentSolicitor1EmailAddress("solicitor@example.com");
    }

    private CaseData getCaseData(boolean isRespondentLiP, boolean isRespondentBilingual) {
        RespondentLiPResponse respondentLip = RespondentLiPResponse.builder()
            .respondent1ResponseLanguage(isRespondentBilingual ? Language.BOTH.toString()
                                             : Language.ENGLISH.toString()).build();
        return commonCaseData()
            .respondent1Represented(isRespondentLiP ? YesOrNo.NO : YesOrNo.YES)

            .build().toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(respondentLip).build())
            .build();
    }

    static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(true, true, "bilingual-template"),
            Arguments.of(true, false, "default-template"),
            Arguments.of(false, false, "solicitor-template")
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void sendNotificationShouldSendEmail(boolean isRespondentLiP, boolean isRespondentBilingual, String template) {
        CaseData caseData = getCaseData(isRespondentLiP, isRespondentBilingual);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE.name())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .caseData(caseData)
            .type(ABOUT_TO_SUBMIT)
            .build();

        if (isRespondentLiP && isRespondentBilingual) {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("bilingual-template");
        } else if (isRespondentLiP) {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("default-template");
        } else {
            when(notificationsProperties.getNotifyLRCaseDismissed()).thenReturn("solicitor-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            isRespondentLiP ? "defendant@hmcts.net" : "solicitor@example.com",
            template,
            Map.of(
                "claimReferenceNumber", "1594901956117591",
                "name", "Jack Jackson",
                "claimantvdefendant", "John Doe V Jack Jackson"
            ),
            "dismiss-case-defendant-notification-1594901956117591"
        );
        assertNotNull(response);
    }
}