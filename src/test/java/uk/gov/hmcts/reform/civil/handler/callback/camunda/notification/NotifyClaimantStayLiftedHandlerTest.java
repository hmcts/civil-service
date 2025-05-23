package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_LIFTED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantStayLiftedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private NotifyClaimantStayLiftedHandler handler;

    private CaseData caseData;

    private CaseDataBuilder commonCaseData() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@example.com").build());
    }

    private CaseData getCaseData(boolean isClaimantLiP, boolean isClaimantBilingual) {
        String claimantBilingualLanguagePreference = isClaimantBilingual ? Language.BOTH.toString()
            : Language.ENGLISH.toString();
        RespondentLiPResponse respondentLip = RespondentLiPResponse.builder()
            .respondent1ResponseLanguage(claimantBilingualLanguagePreference).build();
        return commonCaseData()
            .applicant1Represented(isClaimantLiP ? YesOrNo.NO : YesOrNo.YES)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@example.com").build())
            .claimantBilingualLanguagePreference(claimantBilingualLanguagePreference)
            .build().toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(respondentLip).build())
            .build();
    }

    static Stream<Arguments> provideCaseDataLip() {
        return Stream.of(
            Arguments.of(true, true, "bilingual-template"),
            Arguments.of(true, false, "default-template")
        );
    }

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    @Test
    void checkCamundaActivityTest() {
        caseData = caseData.toBuilder().build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyClaimantStayLifted", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(List.of(NOTIFY_CLAIMANT_STAY_LIFTED), response);
    }

    @Test
    void sendNotificationShouldSendEmail() {
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();

        when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            "respondentSolicitor@hmcts.net",
            "solicitor-template",
            Map.of(
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe",
                "claimantvdefendant", "John Doe V Jack Jackson",
                "partyReferences", buildPartiesReferencesEmailSubject(caseData),
                "casemanRef", caseData.getLegacyCaseReference(),
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            ),
            "stay-lifted-claimant-notification-1594901956117591"
        );
        assertNotNull(response);
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataLip")
    void sendNotificationShouldSendEmail(boolean isRespondentLiP, boolean isRespondentBilingual, String template) {
        CaseData caseData = getCaseData(isRespondentLiP, isRespondentBilingual);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE.name())
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
            when(notificationsProperties.getNotifyLRStayLifted()).thenReturn("solicitor-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            isRespondentLiP ? "claimant@hmcts.net" : "solicitor@example.com",
            template,
            Map.of(
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe",
                "claimantvdefendant", "John Doe V Jack Jackson"
            ),
            "stay-lifted-claimant-notification-1594901956117591"
        );
        assertNotNull(response);
    }
}
