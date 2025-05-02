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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NotifyDefendantStayUpdateRequestedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private NotifyDefendantStayUpdateRequestedHandler handler;

    private CaseData caseData;

    private static final String ENGLISH = "ENGLISH";
    private static final String BILINGUAL = "BOTH";

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("lipEmail@email.com").build())
            .respondent2(Party.builder().individualFirstName("Jim").individualLastName("Jameson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(false, NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED),
            Arguments.of(true, NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED)
        );
    }

    @Test
    void checkCamundaActivityDefendantTest() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendantStayUpdateRequested", response);
    }

    @Test
    void checkCamundaActivityDefendant2Test() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED.toString()).build()).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyDefendant2StayUpdateRequested", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(List.of(NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED,
                             NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED), response);
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void sendNotificationShouldSendEmail(boolean isDefendant2, CaseEvent caseEvent) {
        caseData = caseData.toBuilder()
            .respondentSolicitor1EmailAddress("defendant@hmcts.net")
            .respondentSolicitor2EmailAddress("defendant2@hmcts.net")
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(caseEvent.toString()).build()).build();

        when(notificationsProperties.getNotifyLRStayUpdateRequested()).thenReturn("solicitor-template");
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        CallbackResponse response = handler.sendNotification(params);

        if (isDefendant2) {
            verify(notificationService).sendMail(
                "defendant2@hmcts.net",
                "solicitor-template",
                Map.of(
                    "claimantvdefendant", "Mr. John Rambo V Jack Jackson",
                    "claimReferenceNumber", "1594901956117591",
                    "name", "Jim Jameson",
                    "partyReferences", buildPartiesReferencesEmailSubject(caseData),
                    "casemanRef", caseData.getLegacyCaseReference(),
                    PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                    OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                    SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                    HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

                ),
                "stay-update-requested-defendant-notification-1594901956117591"
            );
        } else {
            verify(notificationService).sendMail(
                "defendant@hmcts.net",
                "solicitor-template",
                Map.of(
                    "claimantvdefendant", "Mr. John Rambo V Jack Jackson",
                    "claimReferenceNumber", "1594901956117591",
                    "name", "Jack Jackson",
                    "partyReferences", buildPartiesReferencesEmailSubject(caseData),
                    "casemanRef", caseData.getLegacyCaseReference(),
                    PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                    OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                    SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                    HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

                ),
                "stay-update-requested-defendant-notification-1594901956117591"
            );
        }
        assertNotNull(response);
    }

    static Stream<Arguments> provideCaseDataLip() {
        return Stream.of(
            Arguments.of(BILINGUAL, "bilingual-template"),
            Arguments.of(ENGLISH, "default-template")
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataLip")
    void sendNotificationShouldSendEmailToLip(String language, String template) {

        RespondentLiPResponse respondentLip = RespondentLiPResponse.builder()
            .respondent1ResponseLanguage(language).build();
        caseData = caseData.toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(respondentLip).build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED.toString()).build()).build();

        if (ENGLISH.equals(language)) {
            when(notificationsProperties.getNotifyLipStayUpdateRequested()).thenReturn("default-template");
        } else {
            when(notificationsProperties.getNotifyLipBilingualStayUpdateRequested()).thenReturn("bilingual-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            "lipEmail@email.com",
            template,
            Map.of(
                "claimantvdefendant", "Mr. John Rambo V Jack Jackson",
                "claimReferenceNumber", "1594901956117591",
                "name", "Jack Jackson"
            ),
            "stay-update-requested-defendant-notification-1594901956117591"
        );
        assertNotNull(response);
    }

}
