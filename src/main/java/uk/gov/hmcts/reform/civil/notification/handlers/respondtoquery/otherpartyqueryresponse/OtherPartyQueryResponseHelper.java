package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
public class OtherPartyQueryResponseHelper {

    public Map<String, String> addCustomProperties(Map<String, String> properties,
                                                   CaseData caseData,
                                                   String legalOrgNameOrPartyName,
                                                   boolean isLipOtherParty) {
        if (isLipOtherParty) {
            properties.put(PARTY_NAME, legalOrgNameOrPartyName);
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgNameOrPartyName);
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        }
        return properties;
    }
}
