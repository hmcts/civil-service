package uk.gov.hmcts.reform.civil.ga.utils;

import uk.gov.hmcts.reform.civil.ga.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;

public class EmailSubjectUtils {

    private static final String NOT_PROVIDED = "Not provided";

    private EmailSubjectUtils() {
        //NO-OP
    }

    public static Map<String, String> addSubjectReferences(GeneralApplicationCaseData mainCaseData,
                                                           String parentCaseReference,
                                                           Map<String, String> properties) {
        properties.put(CLAIM_16_DIGIT_NUMBER, parentCaseReference);
        properties.put(CASEMAN_REF, Optional.ofNullable(mainCaseData)
            .map(GeneralApplicationCaseData::getLegacyCaseReference)
            .orElse(NOT_PROVIDED));
        return properties;
    }

    public static Map<String, String> addLipClaimantVDefendant(GeneralApplicationCaseData caseData,
                                                               Map<String, String> properties) {
        properties.put(CASE_TITLE, JudicialFinalDecisionHandler.getAllPartyNames(caseData));
        return properties;
    }
}
