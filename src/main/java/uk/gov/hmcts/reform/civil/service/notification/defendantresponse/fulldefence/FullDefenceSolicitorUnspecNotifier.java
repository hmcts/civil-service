package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@RequiredArgsConstructor
public abstract class  FullDefenceSolicitorUnspecNotifier extends FullDefenceSolicitorNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    public void notifySolicitorForDefendantResponse(CaseData caseData) {
        String recipient;
        recipient = getRecipient(caseData);
        sendNotificationToSolicitor(caseData, recipient);
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE) || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addCommonFooterSignature(properties, configuration);
            addSpecAndUnspecContact(caseData, properties, configuration,
                                    featureToggleService.isQueryManagementLRsEnabled());
            return properties;
        } else {
            //if there are 2 respondents on the case, concatenate the names together for the template subject line
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1())
                    .concat(" and ")
                    .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack()),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
            addCommonFooterSignature(properties, configuration);
            addSpecAndUnspecContact(caseData, properties, configuration,
                                    featureToggleService.isQueryManagementLRsEnabled());
            return properties;
        }
    }
}
