package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@SpringBootTest(classes = {
    NotifyClaimantClaimSubmitted.class,
    JacksonAutoConfiguration.class
})
public class NotifyClaimantClaimSubmittedTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService toggleService;
    @MockBean
    private PinInPostConfiguration pinInPostConfiguration;
    @Autowired
    private NotifyClaimantClaimSubmitted handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String EMAIL_TEMPLATE_HWF = "test-notification-id";
        private static final String EMAIL_TEMPLATE_NO_HWF = "test-notification-no-hwf-id";
        private static final String CLAIMANT_EMAIL_ID = "testorg@email.com";
        private static final String CLAIMANT_EMAIL_ID_INDIVIDUAL = "rambo@email.com";
        private static final String REFERENCE_NUMBER = "claim-submitted-notification-000DC001";
        private static final String CLAIMANT = "Mr. John Rambo";
        private static final String RESPONDENT_NAME = "Mr. Sole Trader";
        public static final String FRONTEND_CUI_URL = "dummy_cui_front_end_url";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_NO_HWF);
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_HWF);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldNotifyApplicant1_ClaimIsSubmittedButNotIssued() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(CLAIMANT_EMAIL_ID)
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotSendEmail_whenEventIsCalledAndApplicantHasNoEmail() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(0)).sendMail(
                CLAIMANT_EMAIL_ID,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHFWReferenceNumberPresent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("1111").build()).build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHFWReferanceNumberNotPresent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIMANT_NAME, CLAIMANT,
                DEFENDANT_NAME, RESPONDENT_NAME,
                FRONTEND_URL, FRONTEND_CUI_URL
            );
        }

    }
}
