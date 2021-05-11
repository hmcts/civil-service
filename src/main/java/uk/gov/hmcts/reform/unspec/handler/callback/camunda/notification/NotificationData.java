package uk.gov.hmcts.reform.unspec.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.unspec.model.CaseData;

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
    String FRONTEND_BASE_URL_KEY = "frontendBaseUrl";
    String REASON = "reason";
    String FRONTEND_BASE_URL = "https://www.MyHMCTS.gov.uk";

    Map<String, String> addProperties(CaseData caseData);

}
