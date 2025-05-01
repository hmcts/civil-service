package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

class FullDefenceApplicantSolicitorOneCCUnspecNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private FullDefenceApplicantSolicitorOneCCUnspecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn("template-id");
    }

    @Test
    void shouldNotifyRespondentSolicitor1In1v1Scenario_whenV1CallbackInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(caseData),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor1In2v1Scenario_whenV1CallbackInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateRespondentFullDefence()
            .build();

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "template-id",
            getNotificationDataMap(caseData),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()),
                CASEMAN_REF, "000DC001"
            );
        } else {
            //if there are 2 respondents on the case, concatenate the names together for the template subject line
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1())
                    .concat(" and ")
                    .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()),
                CASEMAN_REF, "000DC001"
            );
        }
    }
}
