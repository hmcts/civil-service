package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BUNDLE_RESTITCH_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class AmendRestitchBundleNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;
    @InjectMocks
    private AmendRestitchBundleNotifier amendRestitchBundleNotifier;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantSolicitorAndRespondentSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyLRBundleRestitched()).thenReturn("template-lr-id");
        amendRestitchBundleNotifier.notifyParties(caseData);

        Map<String, String> parameters = getNotificationDataMap();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-lr-id",
            parameters,
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-lr-id",
            parameters,
            "amend-restitch-bundle-claimant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondent2Solicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(notificationsProperties.getNotifyLRBundleRestitched()).thenReturn("template-lr-id");
        amendRestitchBundleNotifier.notifyParties(caseData);

        Map<String, String> parameters = getNotificationDataMap();
        parameters.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234");
        parameters.put(CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader, Mr. John Rambo");

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-lr-id",
            parameters,
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "respondentsolicitor2@example.com",
            "template-lr-id",
            parameters,
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-lr-id",
            parameters,
            "amend-restitch-bundle-claimant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLipAndRespondentLipNotBilingual_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .claimantUserDetails(
                IdamUserDetails.builder()
                    .email("john.rambo@email.com")
                    .build()
            )
            .applicant1Represented(NO).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-lip-id");
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        amendRestitchBundleNotifier.notifyParties(caseData);

        Map<String, String> parameters = getNotificationDataMap();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-lip-id",
            parameters,
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "john.rambo@email.com",
            "template-lip-id",
            parameters,
            "amend-restitch-bundle-claimant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLipAndRespondentLipBilingual_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .claimantUserDetails(
                IdamUserDetails.builder()
                    .email("john.rambo@email.com")
                    .build()
            )
            .applicant1Represented(NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build()
            )
            .build();

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("template-lip-bilingual-id");
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        amendRestitchBundleNotifier.notifyParties(caseData);

        Map<String, String> parameters = getNotificationDataMap();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-lip-bilingual-id",
            parameters,
            "amend-restitch-bundle-defendant-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "john.rambo@email.com",
            "template-lip-bilingual-id",
            parameters,
            "amend-restitch-bundle-claimant-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader",
            BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.UK))
        ));
    }

}
