package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;

@ExtendWith(MockitoExtension.class)
public class NotifyDefendantsClaimantSettleTheClaimTest extends BaseCallbackHandlerTest {

    @Mock
    private OrganisationService organisationService;
    @InjectMocks
    private NotifyDefendantsClaimantSettleTheClaim notificationHandler;
    @Mock
    private NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;
    @Captor
    private ArgumentCaptor<String> targetEmail;
    @Captor
    private ArgumentCaptor<String> emailTemplate;
    @Captor
    private ArgumentCaptor<Map<String, String>> notificationDataMap;
    @Captor
    private ArgumentCaptor<String> reference;

    @Nested
    class AboutToSubmitCallback {

        private static final String DEFENDANT_EMAIL_ADDRESS = "defendantmail@hmcts.net";
        private static final String DEFENDANT_PARTY_NAME = "ABC ABC";
        private static final String REFERENCE_NUMBER = "8372942374";
        private static final String EMAIL_TEMPLATE = "test-notification-id";
        private static final String EMAIL_TEMPLATE_LR = "test-notification-lr-id";
        private static final String CLAIMANT_ORG_NAME = "Org Name";

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
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM.name()).build()).build();
            //When
            given(notificationsProperties.getNotifyDefendantLIPClaimantSettleTheClaimTemplate()).willReturn(EMAIL_TEMPLATE);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(),
                                                           reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo(DEFENDANT_EMAIL_ADDRESS);
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE);
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(RESPONDENT_NAME, "ABC ABC");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_REFERENCE_NUMBER, "8372942374");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIMANT_NAME, "Org Name");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldSendNotificationToDefendantLR_whenEventIsCalledAndDefendantHasEmail(boolean referenceWasProvided) {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().type(Party.Type.COMPANY).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName(CLAIMANT_ORG_NAME).build())
                .respondent1Represented(YesOrNo.YES)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("ORG_ID").build())
                                                   .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                                                   .build())
                .respondentSolicitor1EmailAddress("test@test.com")
                .solicitorReferences(SolicitorReferences.builder().respondentSolicitor1Reference(referenceWasProvided ? "Def Ref Num" : null).build())
                .legacyCaseReference(REFERENCE_NUMBER)
                .ccdCaseReference(1234567891234567L)
                .addApplicant2(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM.name()).build()).build();
            //When
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("Legal Rep Name").build()));
            given(notificationsProperties.getNotifyDefendantLRClaimantSettleTheClaimTemplate()).willReturn(EMAIL_TEMPLATE_LR);
            notificationHandler.handle(params);
            //Then
            verify(notificationService, times(1)).sendMail(targetEmail.capture(),
                                                           emailTemplate.capture(),
                                                           notificationDataMap.capture(),
                                                           reference.capture()
            );
            assertThat(targetEmail.getAllValues().get(0)).isEqualTo("test@test.com");
            assertThat(emailTemplate.getAllValues().get(0)).isEqualTo(EMAIL_TEMPLATE_LR);
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_REFERENCE_NUMBER, "8372942374");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIMANT_NAME, "Org Name");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567891234567");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(DEFENDANT_REFERENCE_NUMBER,
                                                                                referenceWasProvided ? "Def Ref Num" : "Not provided");
            assertThat(notificationDataMap.getAllValues().get(0)).containsEntry(LEGAL_REP_NAME, "Legal Rep Name");
        }

    }
}
