package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String RESPONDENT_NAME = "defendantName";
    String ISSUED_ON = "issuedOn";
    String CLAIM_NOTIFICATION_DEADLINE = "claimNotificationDeadline";
    String CLAIM_DETAILS_NOTIFICATION_DEADLINE = "claimDetailsNotificationDeadline";
    String RESPONSE_DEADLINE = "responseDeadline";
    String NOTIFICATION_DEADLINE = "notificationDeadline";
    String AGREED_EXTENSION_DATE = "agreedExtensionDate";
    String REASON = "reason";

    String CLAIM_LEGAL_ORG_NAME_SPEC = "legalOrgName";
    String CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC = "defendantLegalRep";

    //1v2 Handed Offline Email, we show both respondents + their responses
    String RESPONDENT_ONE_NAME = "defendantOneName";
    String RESPONDENT_TWO_NAME = "defendantTwoName";
    String RESPONDENT_ONE_RESPONSE = "defendantOneResponse";
    String RESPONDENT_TWO_RESPONSE = "defendantTwoResponse";

    Map<String, String> addProperties(CaseData caseData);

}
