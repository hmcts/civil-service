package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import java.util.Map;

public interface NotificationDataGA {

    String CASE_REFERENCE = "claimReferenceNumber";
    String GENAPP_REFERENCE = "GenAppclaimReferenceNumber";
    String PARTY_REFERENCE = "partyReferences";
    String APPLICANT_REFERENCE = "claimantOrDefendant";
    String GA_NOTIFICATION_DEADLINE = "notificationDeadLine";
    String GA_APPLICATION_TYPE = "generalAppType";
    String GA_JUDICIAL_CONCURRENT_DATE_TEXT = "generalAppJudicialConcurrentDate";
    String GA_JUDICIAL_SEQUENTIAL_DATE_TEXT_RESPONDENT = "generalAppJudicialSequentialDateRespondent";
    String GA_REQUEST_FOR_INFORMATION_DEADLINE = "requestForInformationDeadline";
    String GA_HEARING_DATE = "hearingDate";
    String GA_HEARING_TIME = "hearingTime";
    String GA_LIP_APPLICANT_NAME = "applicantName";
    String GA_LIP_RESP_NAME = "respondentName";
    String CASE_TITLE = "ClaimantvDefendant";
    String CLAIM_LEGAL_ORG_NAME_SPEC = "legalOrgName";

    //hwf
    String CLAIMANT_NAME = "claimantName";
    String APPLICANT_NAME = "applicantName";
    String HWF_REFERENCE_NUMBER = "hwFReferenceNumber";
    String TYPE_OF_FEE = "typeOfFee";
    String TYPE_OF_FEE_WELSH = "typeOfFeeWelsh";
    String HWF_MORE_INFO_DATE = "date";
    String HWF_MORE_INFO_DATE_IN_WELSH = "dateInWelsh";
    String HWF_MORE_INFO_DOCUMENTS = "documents";
    String HWF_MORE_INFO_DOCUMENTS_WELSH  = "documentsWelsh";
    String PART_AMOUNT = "partAmount";
    String REMAINING_AMOUNT = "remainingAmount";
    String FEE_AMOUNT = "amount";
    String NO_REMISSION_REASONS = "reasons";
    String NO_REMISSION_REASONS_WELSH = "reasonsWelsh";

    //footer
    String HMCTS_SIGNATURE = "hmctsSignature";
    String WELSH_HMCTS_SIGNATURE = "welshHmctsSignature";
    String PHONE_CONTACT = "phoneContact";
    String WELSH_PHONE_CONTACT = "welshPhoneContact";
    String OPENING_HOURS = "openingHours";
    String WELSH_OPENING_HOURS = "welshOpeningHours";
    String SPEC_UNSPEC_CONTACT = "specAndUnspecContact";
    String SPEC_CONTACT = "specContact";
    String WELSH_CONTACT = "welshContact";

    Map<String, String> addProperties(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData);

}
