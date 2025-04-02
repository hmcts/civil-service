package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimContinuingOnlineApplicantPartyForSpecNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private Time time;

    @InjectMocks
    private ClaimContinuingOnlineApplicantPartyForSpecNotificationHandler handler;

    private static final String CLAIMANT_EMAIL_ADDRESS = "testorg@email.com";
    private static final String TASK_ID_Applicant1 = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";
    private static final String TEMPLATE_ID = "template-id";
    private static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    private static final String REFERENCE = "claim-continuing-online-notification-000DC001";

    @BeforeEach
    void setup() {
        LocalDateTime responseDeadline = LocalDateTime.now().plusDays(14);
        when(notificationsProperties.getClaimantClaimContinuingOnlineForSpec())
            .thenReturn(TEMPLATE_ID);
        when(deadlinesCalculator.plus14DaysDeadline(any())).thenReturn(responseDeadline);
    }

    @Test
    void shouldNotifyApplicant1PartyEmail_whenInvoked() {
        // Given
        CaseData caseData = getCaseData(CLAIMANT_EMAIL_ADDRESS, null);
        CallbackParams params = getCallbackParams(caseData);

        // When
        handler.handle(params);

        // Then
        verify(notificationService).sendMail(
            CLAIMANT_EMAIL_ADDRESS,
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            REFERENCE
        );
    }

    @Test
    void shouldNotifyApplicant1_UserDetailsEmail_whenInvoked() {
        // Given
        CaseData caseData = getCaseData(null, CLAIMANT_EMAIL_ADDRESS);
        CallbackParams params = getCallbackParams(caseData);

        // When
        handler.handle(params);

        // Then
        verify(notificationService).sendMail(
            CLAIMANT_EMAIL_ADDRESS,
            TEMPLATE_ID,
            getNotificationDataMap(caseData),
            REFERENCE
        );
    }

    @Test
    void shouldNotifyApplicant1WithBilingualEmailTemplateWhenClaimantIsBilingual() {
        // Given
        CaseData caseData = getCaseData(CLAIMANT_EMAIL_ADDRESS, null);
        caseData = caseData.toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();
        CallbackParams params = getCallbackParams(caseData);
        when(notificationsProperties.getBilingualClaimantClaimContinuingOnlineForSpec())
            .thenReturn(BILINGUAL_TEMPLATE_ID);

        // When
        handler.handle(params);

        // Then
        verify(notificationService).sendMail(
            CLAIMANT_EMAIL_ADDRESS,
            BILINGUAL_TEMPLATE_ID,
            getNotificationDataMap(caseData),
            REFERENCE
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC").build())
                                                 .build())).isEqualTo(TASK_ID_Applicant1);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    }

    private CallbackParams getCallbackParams(CaseData caseData) {
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC")
                .build()).build();
        return params;
    }

    private CaseData getCaseData(String partyEmail, String claimantUserEmail) {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build().toBuilder()
            .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                            .partyEmail(partyEmail)
                            .build())
            .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                             .build())
            .claimDetailsNotificationDate(LocalDateTime.now())
            .respondent1ResponseDeadline(LocalDateTime.now())
            .addRespondent2(YesOrNo.NO)
            .claimantUserDetails(IdamUserDetails.builder().email(claimantUserEmail).build())
            .build();

        return caseData;
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, "Mr. Sole Trader",
            CLAIMANT_NAME, "Mr. John Rambo",
            ISSUED_ON, formatLocalDate(LocalDate.now(), DATE),
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            RESPONSE_DEADLINE, formatLocalDate(
                caseData.getRespondent1ResponseDeadline()
                    .toLocalDate(), DATE)
        );
    }
}

