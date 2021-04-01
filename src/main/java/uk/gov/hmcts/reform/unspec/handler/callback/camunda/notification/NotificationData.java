package uk.gov.hmcts.reform.unspec.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String APPLICANT_NAME = "claimantName";
    String RESPONDENT_NAME = "defendantName";
    String ISSUED_ON = "issuedOn";
    String RESPONSE_DEADLINE = "responseDeadline";
    String FRONTEND_BASE_URL_KEY = "frontendBaseUrl";
    String FRONTEND_BASE_URL = "https://www.MyHMCTS.gov.uk";

    Map<String, String> addProperties(CaseData caseData);

}
