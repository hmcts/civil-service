package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BUNDLE_RESTITCH_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@SpringBootTest(classes = {
    NotifyDefendantAmendRestitchBundleHandler.class,
    JacksonAutoConfiguration.class
})
class NotifyDefendantAmendRestitchBundleHandlerTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_LR_ID = "template-LR-id";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    public static final String DATE_FORMAT = "dd-MM-yyyy";

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationsSignatureConfiguration configuration;
    @Autowired
    NotifyDefendantAmendRestitchBundleHandler handler;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
            .applicant1(Party.builder().individualFirstName("John").individualLastName("Doe")
                            .type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().individualFirstName("Jack").individualLastName("Jackson")
                             .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
            .build();
    }

    @ParameterizedTest
    @CsvSource({
        "NO, NO, NO, template-id",
        "NO, YES, NO, bilingual-template-id",
        "YES, NO, NO, 'template-LR-id'",
        "YES, NO, YES, 'template-LR-id'"
    })
    void shouldSendEmailBasedOnConditions(YesOrNo represented, YesOrNo bilingual, YesOrNo isRespondent2, String expectedTemplateId) {
        if (represented.equals(YesOrNo.NO)) {
            if (bilingual == YesOrNo.YES) {
                caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
                                                                .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                                            .respondent1ResponseLanguage(Language.BOTH.toString())
                                                                                            .build())
                                                                .build()).build();
                when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);
            } else {
                when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);
            }
        } else {
            when(notificationsProperties.getNotifyLRBundleRestitched()).thenReturn(TEMPLATE_LR_ID);
        }

        caseData = caseData.toBuilder().respondent1Represented(represented).build();
        if (YesOrNo.YES.equals(isRespondent2)) {
            caseData = caseData.toBuilder()
                .respondent2(Party.builder().individualFirstName("John").individualLastName("Johnson")
                                 .type(Party.Type.INDIVIDUAL).build())
                .respondentSolicitor2EmailAddress("solicitor2@address.com")
                .addRespondent2(isRespondent2)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .build();
        }
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(YesOrNo.YES.equals(isRespondent2)
                         ? CaseEvent.NOTIFY_DEFENDANT_TWO_AMEND_RESTITCH_BUNDLE.name()
                            : CaseEvent.NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE.name()).build();

        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(callbackRequest).build();
        handler.handle(params);

        if (represented == YesOrNo.NO) {
            verify(notificationService).sendMail(
                "respondentLip@example.com",
                expectedTemplateId,
                getNotificationDataMap(caseData, isRespondent2),
                "amend-restitch-bundle-defendant-notification-1594901956117591"
            );
        } else {
            verify(notificationService).sendMail(
                YesOrNo.YES.equals(isRespondent2) ? "solicitor2@address.com" : "respondentsolicitor@example.com",
                expectedTemplateId,
                getNotificationDataMap(caseData, isRespondent2),
                "amend-restitch-bundle-defendant-notification-1594901956117591"
            );
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData, YesOrNo isRespondent2) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, YesOrNo.YES.equals(isRespondent2) ? "John Johnson" : "Jack Jackson",
            CLAIMANT_V_DEFENDANT, YesOrNo.YES.equals(isRespondent2) ? "John Doe V Jack Jackson, John Johnson" : "John Doe V Jack Jackson",
            BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"

        );
    }
}


