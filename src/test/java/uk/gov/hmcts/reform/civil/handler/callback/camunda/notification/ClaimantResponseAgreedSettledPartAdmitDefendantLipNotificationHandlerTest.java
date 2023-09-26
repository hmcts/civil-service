package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    ClaimantResponseAgreedSettledPartAdmitDefendantLipNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimantResponseAgreedSettledPartAdmitDefendantLipNotificationHandlerTest extends BaseCallbackHandlerTest {

    public static final String targetEmail = "sole.trader@email.com";
    public static final String template = "spec-lip-template-id";
    public static final String templateEmail_lr = "respondent1email@hmcts.net";
    public static final String template_id_lr = "spec-lr-template-id";
    public static final String bilingualTemplate = "spec-lip-template-bilingual-id";
    public static final String englishLangResponse = "ENGLISH";
    public static final String bothLangResponse = "BOTH";

    public static final String reference = "claimant-part-admit-settle-respondent-notification-000DC001";
    public static final String EVENT_ID = "NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED";

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @Autowired
    private ClaimantResponseAgreedSettledPartAdmitDefendantLipNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentLipPartAdmitSettleClaimTemplate()).thenReturn(template);
            when(notificationsProperties.getRespondentLipPartAdmitSettleClaimBilingualTemplate()).thenReturn(bilingualTemplate);
            when(notificationsProperties.getRespondentLrPartAdmitSettleClaimTemplate()).thenReturn(template_id_lr);
        }

        @Test
        void shouldNotifyRespondent_whenInvoked_spec_lip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                targetEmail,
                template,
                getNotificationDataMap(caseData),
                reference
            );
        }

        @Test
        void shouldNotNotify_whenInvoked_spec_lip_withLr() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1OrgRegistered(null)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void shouldNotNotify_whenInvoked_spec_lip_noEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .respondent1(PartyBuilder.builder().soleTrader().partyEmail(null).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void shouldNotNotify_whenInvoked_spec_lip_bilingual() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(bothLangResponse).build()).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                targetEmail,
                bilingualTemplate,
                getNotificationDataMap(caseData),
                reference
            );
        }

        @Test
        void shouldNotNotify_whenInvoked_spec_lip_english() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .specClaim1v1LrVsLip()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(englishLangResponse).build()).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                targetEmail,
                template,
                getNotificationDataMap(caseData),
                reference
            );
        }

        @Test
        void shouldNotifyRespondent_whenInvoked_spec_lr() {
            when(organisationService.findOrganisationById(
                anyString())).thenReturn(Optional.of(Organisation.builder().name("defendant solicitor org").build()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondentSolicitor1EmailAddress("respondent1email@hmcts.net")
                .respondent1OrgRegistered(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(EVENT_ID)
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                templateEmail_lr,
                template_id_lr,
                getNotificationDataMap(caseData),
                reference
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            if (caseData.isRespondent1NotRepresented()) {
                return Map.of(
                    RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
                );
            } else {
                return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData)
                );

            }
        }

        private String getRespondentLegalOrganizationName(CaseData caseData) {
            Optional<Organisation> organisation = organisationService.findOrganisationById(caseData.getRespondent1OrganisationId());
            String respondentLegalOrganizationName = null;
            if (organisation.isPresent()) {
                respondentLegalOrganizationName = organisation.get().getName();
            }
            return respondentLegalOrganizationName;
        }
    }
}
