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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantStayUpdateRequestedHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantStayUpdateRequestedHandler handler;

    private CaseData caseData;

    private static final String ENGLISH = "ENGLISH";
    private static final String BILINGUAL = "BOTH";

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .partyEmail("lipEmail@mail.com").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson").type(Party.Type.INDIVIDUAL).build())
            .build();
    }

    @Test
    void checkCamundaActivityTest() {
        caseData = caseData.toBuilder().build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();
        var response = handler.camundaActivityId(params);
        assertEquals("NotifyClaimantStayUpdateRequested", response);
    }

    @Test
    void checkHandleEventTest() {
        var response = handler.handledEvents();
        assertEquals(List.of(NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED), response);
    }

    @Test
    void sendNotificationShouldSendEmail() {
        caseData = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("respondentSolicitor@hmcts.net").build())
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData).build();

        when(notificationsProperties.getNotifyLRStayUpdateRequested()).thenReturn("solicitor-template");

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            "respondentSolicitor@hmcts.net",
            "solicitor-template",
            Map.of(
                "claimantvdefendant", "John Doe V Jack Jackson",
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe",
                "partyReferences", buildPartiesReferencesEmailSubject(caseData),
                "casemanRef", caseData.getLegacyCaseReference()
            ),
            "stay-update-requested-claimant-notification-1594901956117591"
        );
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

        caseData = caseData.toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(language)
            .build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .request(CallbackRequest.builder().eventId(NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED.toString()).build()).build();

        if (ENGLISH.equals(language)) {
            when(notificationsProperties.getNotifyLipStayUpdateRequested()).thenReturn("default-template");
        } else {
            when(notificationsProperties.getNotifyLipBilingualStayUpdateRequested()).thenReturn("bilingual-template");
        }

        CallbackResponse response = handler.sendNotification(params);

        verify(notificationService).sendMail(
            "claimant@hmcts.net",
            template,
            Map.of(
                "claimantvdefendant", "John Doe V Jack Jackson",
                "claimReferenceNumber", "1594901956117591",
                "name", "John Doe"
            ),
            "stay-update-requested-claimant-notification-1594901956117591"
        );
        assertNotNull(response);
    }
}
