package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantCaseStayedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantCaseStayedHandler handler;

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
            Arguments.of(true, true, "bilingual-template", "claimant@hmcts.net"),
            Arguments.of(true, false, "default-template", "claimant@hmcts.net"),
            Arguments.of(false, false, "solicitor-template", "solicitor@example.com")
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void sendNotificationShouldSendEmail(boolean isApplicantLiP, boolean isClaimantBilingual, String template, String email) {
        caseData = caseData.toBuilder()
            .applicant1Represented(isApplicantLiP ? YesOrNo.NO : YesOrNo.YES)
            .claimantBilingualLanguagePreference(isClaimantBilingual ? Language.BOTH.toString() : Language.ENGLISH.toString())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(email).build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();

        if (isApplicantLiP && isClaimantBilingual) {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("bilingual-template");
        } else if (isApplicantLiP) {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("default-template");
        } else {
            when(notificationsProperties.getNotifyLRCaseStayed()).thenReturn("solicitor-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            email,
            template,
            Map.of(
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe",
                "claimantvdefendant", "John Doe V Jack Jackson"
            ),
            "case-stayed-claimant-notification-1594901956117591"
        );
        assertNotNull(response);
    }
}
