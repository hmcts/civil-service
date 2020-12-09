package uk.gov.hmcts.reform.unspec.handler.callback.notification;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String APPLICANT_NAME = "claimantName";
    String RESPONDENT_NAME = "defendantName";
    String RESPONDENT_SOLICITOR_NAME = "defendantSolicitorName";
    String APPLICANT_SOLICITOR_NAME = "applicantSolicitorName";
    String ISSUED_ON = "issuedOn";
    String RESPONSE_DEADLINE = "responseDeadline";
    String SOLICITOR_REFERENCE = "solicitorReference";
    String FRONTEND_BASE_URL_KEY = "frontendBaseUrl";
    String FRONTEND_BASE_URL = "https://www.MyHMCTS.gov.uk";

    Map<String, String> addProperties(CaseData caseData);

}
