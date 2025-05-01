package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.EXTERNAL_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class DefendantChangeOfAddressApplicantForCuiNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DefendantChangeOfAddressApplicantForCuiNotificationHandler handler;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Nested
    class AboutToSubmitCallback {

        public static final String FRONTEND_CUI_URL = "dummy_cui_front_end_url";
        public static final String EXTERNAL_CASE_ID = "1594901956117591";
        public static final String APPLICANT_NAME = "Mr. John Rambo";
        public static final String REFERENCE_NUMBER = "000DC001";
        public static final String DEFENDANT_NAME = "Mr. Sole Trader";
        public static final String PARTY_EMAIL = "rambo@email.com";
        public static final String TEMPLATE_ID = "template-id";
        public static final String REFERENCE = "defendant-contact-details-change-applicant-notification-000DC001";

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getRespondentChangeOfAddressNotificationTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyLiPClaimant_whenInvoked() {
            when(notificationsProperties.getNotifyLiPClaimantDefendantChangedContactDetails()).thenReturn(TEMPLATE_ID);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn(FRONTEND_CUI_URL);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                PARTY_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMapForLiPClaimant(),
                REFERENCE
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapForLiPClaimant() {
            return Map.of(
                CLAIMANT_NAME, APPLICANT_NAME,
                CLAIM_REFERENCE_NUMBER, REFERENCE_NUMBER,
                RESPONDENT_NAME, DEFENDANT_NAME,
                FRONTEND_URL, FRONTEND_CUI_URL,
                EXTERNAL_ID, EXTERNAL_CASE_ID
            );
        }
    }
}
