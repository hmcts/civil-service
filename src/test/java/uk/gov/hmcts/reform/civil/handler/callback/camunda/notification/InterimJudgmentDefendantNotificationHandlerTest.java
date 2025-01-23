package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_DEF;

@ExtendWith(MockitoExtension.class)
public class InterimJudgmentDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InterimJudgmentDefendantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-app",
                getNotificationDataMap(),
                "interim-judgment-approval-notification-def-000DC001"
            );
        }

        @Test
        void shouldReturnPartyNameIfRespondentIsLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(null)
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null).build().toBuilder()
                .ccdCaseReference(1594901956117591L).build();

            Map<String, String> propertyMap = handler.addProperties(caseData);
            assertEquals("Mr. Don Smith", propertyMap.get(LEGAL_ORG_DEF));
        }

        @Test
        void shouldReturnPartyNameIfOrgnisationPolicyIsSetButOrgIdMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .orgPolicyCaseAssignedRole("[RESPSOLICITORONE]")
                                                   .organisation(uk.gov.hmcts.reform.ccd.model
                                                                     .Organisation.builder().build()).build())
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null).build().toBuilder()
                .ccdCaseReference(1594901956117591L).build();

            Map<String, String> propertyMap = handler.addProperties(caseData);
            assertEquals("Mr. Don Smith", propertyMap.get(LEGAL_ORG_DEF));
        }

        @Test
        void shouldNotifyClaimantSolicitor2Defendants_whenInvoked() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(2)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-def-000DC001"));
        }

        @Test
        void shouldNotNotify_whenLipDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(null)
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null).build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .addRespondent2(YesOrNo.NO)
                .ccdCaseReference(1594901956117591L).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(notificationService);
            assertThat(response.getState()).isEqualTo("JUDICIAL_REFERRAL");
        }

        @Test
        public void shouldNotNotifyRespondent2WhenLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mr.")
                                 .individualFirstName("Don")
                                 .individualLastName("Smith")
                                 .build())
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("hmcts")
                                 .individualTitle("Mrs.")
                                 .individualFirstName("Donna")
                                 .individualLastName("Smith")
                                 .build())
                .respondent1OrganisationPolicy(null)
                .legacyCaseReference("12DC910")
                .respondent2OrganisationPolicy(null)
                .respondent1Represented(YesOrNo.NO)
                .addRespondent2(YesOrNo.YES)
                .respondent2Represented(YesOrNo.NO)
                .ccdCaseReference(1594901956117591L).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(notificationService);
            assertThat(response.getState()).isEqualTo("JUDICIAL_REFERRAL");
        }

        @Test
        public void shouldNotifyRespondent2SolWhenRespondent1Lip() {
            when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn("template-id-app");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                anyString(),
                eq("template-id-app"), anyMap(),
                eq("interim-judgment-approval-notification-def-000DC001"));
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                "Defendant LegalOrg Name", "Test Org Name",
                "Claim number", "1594901956117591",
                "Defendant Name", "Mr. Sole Trader",
                "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }
    }
}
