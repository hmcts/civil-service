package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready.RespondentTwoNotifyOthersTrialReadyNotifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

@ExtendWith(MockitoExtension.class)
public class Respondent2NotifyOthersTrialReadyNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @InjectMocks
    private RespondentTwoNotifyOthersTrialReadyNotifier respondent2NotifyOthersTrialReadyNotifier;

    @Test
    void shouldNotifyApplicantAndRespondent1_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateHearingDateScheduled().build();

        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        when(notificationsProperties.getOtherPartyTrialReady()).thenReturn("template-id");

        respondent2NotifyOthersTrialReadyNotifier.notifyParties(caseData, "NOTIFY_EVENT", "Respondent2NotifyOthersTrialReadyNotifier");

        verify(notificationService).sendMail(
            "applicantsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "other-party-trial-ready-notification-000DC001"
        );

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(),
            "other-party-trial-ready-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyApplicantLipAndRespondentLipNotBilingual_whenInvoked() {
        final CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheckLiP(true).build();

        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id");

        respondent2NotifyOthersTrialReadyNotifier.notifyParties(caseData, "NOTIFY_EVENT", "Respondent2NotifyOthersTrialReadyNotifier");
        Map<String, String> parameters = getNotificationDataMapLip();

        parameters.put(PARTY_NAME, "Mr. Sole Trader");
        verify(notificationService).sendMail(
            "sole.trader@email.com",
            "template-id",
            parameters,
            "other-party-trial-ready-notification-000MC001"
        );

        parameters.put(PARTY_NAME, "Mr. John Rambo");
        verify(notificationService).sendMail(
            "rambo@email.com",
            "template-id",
            parameters,
            "other-party-trial-ready-notification-000MC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001",
            HEARING_DATE, LocalDate.now().plusWeeks(3).plusDays(1).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))
        ));
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip() {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            CASEMAN_REF, "000MC001",
            CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
        ));
    }
}
