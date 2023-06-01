package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@SpringBootTest(classes = {
    NotifyMediationUnsuccessfulDefendantLiPHandler.class,
    JacksonAutoConfiguration.class,
})
class NotifyMediationUnsuccessfulDefendantLiPHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    NotificationsProperties notificationsProperties;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Autowired
    private NotifyMediationUnsuccessfulDefendantLiPHandler notificationHandler;

    @Nested
    class AboutToSubmitCallback {

        private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
        private static final String CLAIMANT_ORG_NAME = "Org Name";
        private static final String DEFENDANT_PARTY_NAME = "Lets party";
        private static final String REFERENCE_NUMBER = "8372942374";
        private static final String EMAIL_TEMPLATE = "test-notification-id";
        private static final String BILINGUAL_EMAIL_TEMPLATE = "test-notification-bilingual-id";
        private static final String BILINGUAL_SELECTION = "BOTH";
        private static final String ENGLISH_SELECTION = "ENGLISH";

        private static final Map<String, String> PROPERTY_MAP = Map.of(
            DEFENDANT_NAME, DEFENDANT_PARTY_NAME,
            CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER,
            CLAIMANT_NAME, CLAIMANT_ORG_NAME
        );

        @BeforeEach
        void setUp() {
            given(notificationsProperties.getMediationUnsuccessfulDefendantLIPTemplate()).willReturn(EMAIL_TEMPLATE);
            given(notificationsProperties.getMediationUnsuccessfulDefendantLIPBilingualTemplate()).willReturn(BILINGUAL_EMAIL_TEMPLATE);
        }

        @Test
        void shouldSendNotificationToDefendantLip_whenEventIsCalledAndDefendantHasEmail() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP.name()).build()).build();
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_MAP);
        }

        @Test
        void shouldSendNotificationToDefendantLip_whenEventIsCalledAndDefendantHasEmailAndBilingual() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(BILINGUAL_SELECTION).build()).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP.name()).build()).build();
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(BILINGUAL_EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_MAP);
        }

        @Test
        void shouldSendNotificationToDefendantLip_whenEventIsCalledAndDefendantHasEmailAndEnglish() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).partyEmail(
                    DEFENDANT_EMAIL_ADDRESS).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(ENGLISH_SELECTION).build()).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP.name()).build()).build();
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).isEqualTo(PROPERTY_MAP);
        }

        @Test
        void shouldNotSendEmail_whenEventIsCalledAndDefendantHasNoEmail() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName(DEFENDANT_PARTY_NAME).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_NAME).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP.name()).build()).build();
            //When
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(0)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(), reference.capture()
            );
        }
    }
}
