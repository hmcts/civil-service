package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
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
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class FullDefenceRespondentSolicitorTwoCCSpecNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private FullDefenceRespondentSolicitorTwoCCSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn("template-id");
    }

    @Test
    void shouldNotifyRespondentSolicitor1In1v1ScenarioSecondSol_whenV1CallbackInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();


        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor2@example.com",
            "template-id",
            getNotificationDataMap(caseData),
            "defendant-response-applicant-notification-000DC001"
        );
    }


    @Test
    void shouldNotifyRespondentSolicitorSpecDef1SecondScenerio_whenInvokedWithCcEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                    .build())
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("respondentsolicitor2@example.com"),
            ArgumentMatchers.eq("spec-respondent-template-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    private Map<String, String> getNotificationDataMapPartAdmissionSpec() {
        return Map.of(
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE
        );
    }

    private Map<String, String> getNotificationDataMapSpec() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
        );
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        } else {
            //if there are 2 respondents on the case, concatenate the names together for the template subject line
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1())
                    .concat(" and ")
                    .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        }
    }
}
