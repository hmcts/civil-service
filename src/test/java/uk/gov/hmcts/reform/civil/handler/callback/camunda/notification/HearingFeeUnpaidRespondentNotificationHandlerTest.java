package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidRespondentNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private HearingFeeUnpaidRespondentNotificationHandler handler;

    public static final String TEMPLATE_ID = "template-id";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            when(notificationsProperties.getRespondentHearingFeeUnpaid()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            CallbackRequest request = params.getRequest();
            request.setEventId((NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID.name()));
            params.toBuilder()
                .request(request)
                .build();

            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "hearing-fee-unpaid-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentLip_whenIs1v1() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
                .toBuilder().respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                getNotificationLipDataMap(),
                "hearing-fee-unpaid-defendantLip-notification-000DC001"
            );
        }
    }

    @Test
    void shouldNotNotifyRespondentLip_whenIs1v1AndHadNoEmail() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build()
            .toBuilder().respondent1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.SOLE_TRADER)
                             .partyName("Mr. Sole Trader").soleTraderLastName("Trader").build()).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
        //When
        handler.handle(params);
        //Then
        verifyNoInteractions(notificationService);
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001",
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

        );
    }

    @NotNull
    private Map<String, String> getNotificationLipDataMap() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CLAIMANT_V_DEFENDANT, "Rambo v Trader",
            PARTY_NAME, "Mr. Sole Trader"
        );
    }

}
