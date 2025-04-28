package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

public interface NotificationData {

    String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    String CLAIM_16_DIGIT_NUMBER = "claim16DigitNumber";
    String RESPONDENT_NAME = "defendantName";
    String ISSUED_ON = "issuedOn";
    String CLAIM_NOTIFICATION_DEADLINE = "claimNotificationDeadline";
    String CLAIM_DETAILS_NOTIFICATION_DEADLINE = "claimDetailsNotificationDeadline";
    String RESPONSE_DEADLINE_PLUS_28 = "responseDeadlinePlus28";
    String RESPONSE_DEADLINE = "responseDeadline";
    String NOTIFICATION_DEADLINE = "notificationDeadline";
    String AGREED_EXTENSION_DATE = "agreedExtensionDate";
    String REASON = "reason";
    String REASON_FROM_CASEWORKER = "reason from caseworker";
    String PARTY_REFERENCES = "partyReferences";
    String ALLOCATED_TRACK = "allocatedTrack";
    String RECIPIENT_PARTY_NAME = "recipientPartyName";

    String CLAIM_LEGAL_ORG_NAME_SPEC = "legalOrgName";
    String WHEN_WILL_BE_PAID_IMMEDIATELY = "payImmediately";
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
    String RESPONDENT = "respondent";

    // CUI Pin in Post
    String CLAIMANT_NAME = "claimantName";
    String RESPOND_URL = "respondToClaimUrl";
    String PIN = "pin";
    String FRONTEND_URL = "frontendBaseUrl";

    //Case Progression
    String EMAIL_ADDRESS = "emailAddress";
    String HEARING_FEE = "hearingFee";
    String HEARING_DATE = "hearingDate";
    String HEARING_TIME = "hearingTime";
    String HEARING_DUE_DATE = "hearingDueDate";
    String CLAIMANT_REFERENCE_NUMBER = "claimantReferenceNumber";
    String DEFENDANT_REFERENCE_NUMBER = "defendantReferenceNumber";
    String HEARING_OR_TRIAL = "hearing/trial";
    String CLAIMANT_DEFENDANT_REFERENCE = "claimant/defendantReferenceNumber";
    String CLAIMANT_V_DEFENDANT = "claimantvdefendant";
    String COURT_LOCATION = "courtlocation";
    String LEGAL_ORG_NAME = "LegalOrgName";
    String LEGAL_ORG = "legal Org";
    String PARTY_NAME = "name";
    String BUNDLE_RESTITCH_DATE = "bundleRestitchDate";

    //NoC
    String CASE_NAME = "case name";
    String ISSUE_DATE = "issue date";
    String CCD_REF = "ccd reference code";
    String NEW_SOL = "new solicitor";
    String FORMER_SOL = "former solicitor";
    String OTHER_SOL_NAME = "other solicitor name";
    String EXTERNAL_ID = "externalId";

    // evidence upload
    String UPLOADED_DOCUMENTS = "uploaded documents";
    String HWF_MORE_INFO_DATE = "date";
    String HWF_MORE_INFO_DOCUMENTS = "documents";
    String HWF_MORE_INFO_DOCUMENTS_WELSH  = "documentsWelsh";

    // hwf outcome
    String REASONS = "reasons";
    String REASONS_WELSH = "reasonsWelsh";
    String TYPE_OF_FEE = "typeOfFee";
    String TYPE_OF_FEE_WELSH = "typeOfFeeWelsh";
    String HWF_REFERENCE_NUMBER = "hwFReferenceNumber";
    String AMOUNT = "amount";
    String PART_AMOUNT = "partAmount";
    String REMAINING_AMOUNT = "remainingAmount";
    String DEFENDANT_NAME_ONE = "defendantName1";
    String DEFENDANT_NAME_TWO = "defendantName2";
    String CLAIMANT_NAME_ONE = "claimant1Name";
    String CLAIMANT_NAME_TWO = "claimant2Name";
    String LEGAL_REP_NAME = "legalrepname";
    String CLAIM_NUMBER_CASE = "ClaimNumber";
    String CLAIM_NAME = "ClaimName";

    String CASEMAN_REF = "casemanRef";
    String LEGAL_REP_NAME_WITH_SPACE = "legal rep name";
    String REFERENCE = "reference";

    String QUERY_DATE = "queryDate";

    String CONTACT_DETAILS_FOOTER = "contactDetails";
    String WELSH_FOOTER = "welshContactDetails";
    String HMCTS_SIGNATURE = "hmctsSignature";
    String PHONE_CONTACT = "phoneContact";
    String OPENING_HOURS = "openingHours";
    String SPEC_UNSPEC_CONTACT = "specAndUnspecContact";

    Map<String, String> addProperties(CaseData caseData);

}
