package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String RESPONDENT_NAME = "defendantName";
    String ISSUED_ON = "issuedOn";
    String CLAIM_NOTIFICATION_DEADLINE = "claimNotificationDeadline";
    String CLAIM_DETAILS_NOTIFICATION_DEADLINE = "claimDetailsNotificationDeadline";
    String RESPONSE_DEADLINE_PLUS_28 = "responseDeadlinePlus28";
    String RESPONSE_DEADLINE = "responseDeadline";
    String NOTIFICATION_DEADLINE = "notificationDeadline";
    String AGREED_EXTENSION_DATE = "agreedExtensionDate";
    String REASON = "reason";
    String PARTY_REFERENCES = "partyReferences";

    String CLAIM_LEGAL_ORG_NAME_SPEC = "legalOrgName";
    String CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC = "defendantLegalRep";
    String DEFENDANT_NAME_SPEC = "defendantLR";
    String CLAIM_NAME_SPEC = "claimantLR";
    String RESPONSE_INTENTION = "responseIntention";

    //Optional Multiparty fields for notifications
    String RESPONDENT_ONE_NAME = "defendantOneName";
    String RESPONDENT_TWO_NAME = "defendantTwoName";
    String RESPONDENT_ONE_RESPONSE = "defendantOneResponse";
    String RESPONDENT_TWO_RESPONSE = "defendantTwoResponse";
    String APPLICANT_ONE_NAME = "Claimant name";

    //Default judgment
    String LEGAL_ORG_SPECIFIED = "legalOrg";
    String CLAIM_NUMBER = "claimnumber";
    String DEFENDANT_NAME = "DefendantName";
    String BOTH_DEFENDANTS = "Both Defendants";
    String CLAIM_NUMBER_INTERIM = "Claim number";
    String LEGAL_ORG_DEF = "Defendant LegalOrg Name";
    String DEFENDANT_NAME_INTERIM = "Defendant Name";
    String LEGAL_REP_CLAIMANT = "Legal Rep Claimant";
    String DEFENDANT_EMAIL = "DefendantLegalOrgName";
    String CLAIMANT_EMAIL = "ClaimantLegalOrgName";
    String LEGAL_ORG_APPLICANT1 = "legalOrgApplicant1";
    String PAYMENT_TYPE = "payment type";
    String AMOUNT_CLAIMED = "amountClaimed";
    String AMOUNT_OF_COSTS = "amountOfCosts";
    String AMOUNT_PAID = "amountPaidBeforeJudgment";
    String AMOUNT_OF_JUDGMENT = "amountOfJudgment";

    Map<String, String> addProperties(CaseData caseData);

}
