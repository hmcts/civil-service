package uk.gov.hmcts.reform.civil.notification.handlers;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
public class BundleCreationNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    public static final String TASK_ID = "BundleCreationNotifier";

    @InjectMocks
    private BundleCreationNotifier bundleCreationNotifier;

    @Mock
    NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .applicant1Represented(YesOrNo.YES)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(notificationsProperties.getBundleCreationTemplate()).thenReturn("template-id");

        bundleCreationNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), TASK_ID);

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "bundle-created-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "bundle-created-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
            .applicant1Represented(YesOrNo.YES)
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(notificationsProperties.getBundleCreationTemplate()).thenReturn("template-id");

        bundleCreationNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), TASK_ID);

        Map<String, String> parameters = getNotificationDataMap();
        parameters.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234");
        parameters.put(CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader, Mr. John Rambo");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            parameters,
            "bundle-created-applicant-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            parameters,
            "bundle-created-respondent-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLiPAndRespondentLiPNotBilingual_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .applicant1Represented(YesOrNo.NO).build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");

        bundleCreationNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), TASK_ID);
        Map<String, String> parameters = getNotificationDataMapLip();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            parameters,
            "bundle-created-respondent-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "rambo@email.com",
            "template-id",
            parameters,
            "bundle-created-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLiPAndRespondentLiPBilingual_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified1v1LiP(
                CertificateOfService.builder()
                    .build()
            )
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(Language.BOTH.toString()).build()).build()
            )
            .build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("template-id");

        bundleCreationNotifier.notifyParties(caseData, NOTIFY_EVENT.toString(), TASK_ID);
        Map<String, String> parameters = getNotificationDataMapLip();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            parameters,
            "bundle-created-respondent-notification-000DC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "rambo@email.com",
            "template-id",
            parameters,
            "bundle-created-applicant-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001",
            CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
        ));
    }
}
