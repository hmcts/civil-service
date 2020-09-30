package uk.gov.hmcts.reform.unspec.handler.callback.notification;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String CLAIMANT_NAME = "claimantName";
    String DEFENDANT_NAME = "defendantName";
    String DEFENDANT_SOLICITOR_NAME = "defendantSolicitorName";
    String ISSUED_ON = "issuedOn";
    String RESPONSE_DEADLINE = "responseDeadline";

    Map<String, String> addProperties(CaseData caseData);

}
