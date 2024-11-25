package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class NotifyDefendantCaseStayedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyDefendantCaseStayedHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(true, true, true, "bilingual-template", "claimant@hmcts.net"),
            Arguments.of(true, false, true, "default-template", "claimant@hmcts.net"),
            Arguments.of(false, false, true, "solicitor-template", "solicitor@example.com"),
            Arguments.of(false, false, false, "solicitor-template", "solicitor@example.com")
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void sendNotificationShouldSendEmail(boolean isRespondentLiP, boolean isRespondentBilingual, boolean isRespondent1, String template, String email) {
        caseData = caseData.toBuilder()
            .respondent1Represented(isRespondentLiP ? YesOrNo.NO : YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                isRespondentBilingual ? Language.BOTH.toString() : Language.ENGLISH.toString()).build()).build())
            .respondentSolicitor1EmailAddress(email)
            .respondent2(!isRespondent1
                             ? Party.builder().individualFirstName("John").individualLastName("Johnson").type(Party.Type.INDIVIDUAL).build()
                             : null)
            .addRespondent2(!isRespondent1
                                ? YesOrNo.YES
                                : null)
            .respondent2SameLegalRepresentative(!isRespondent1
                                                    ? YesOrNo.NO
                                                    : null)
            .respondentSolicitor2EmailAddress(!isRespondent1
                                                  ? "solicitor2@gmail.com"
                                                  : null)
            .build();
        CallbackRequest request = CallbackRequest.builder()
            .eventId(isRespondent1 ? CaseEvent.NOTIFY_DEFENDANT_STAY_CASE.name() : CaseEvent.NOTIFY_DEFENDANT_TWO_STAY_CASE.name()).build();
        CallbackParams params = CallbackParams.builder().request(request).caseData(caseData).build();

        if (isRespondentLiP && isRespondentBilingual) {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("bilingual-template");
        } else if (isRespondentLiP) {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("default-template");
        } else {
            when(notificationsProperties.getNotifyLRCaseStayed()).thenReturn("solicitor-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            isRespondent1 ? email : "solicitor2@gmail.com",
            template,
            Map.of(
                "claimReferenceNumber", "1594901956117591",
                "name", isRespondent1 ? "Jack Jackson" : "John Johnson",
                "claimantvdefendant", isRespondent1 ? "John Doe V Jack Jackson" : "John Doe V Jack Jackson, John Johnson"
            ),
            "case-stayed-defendant-notification-1594901956117591"
        );
        assertNotNull(response);
    }

}
