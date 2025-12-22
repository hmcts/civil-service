package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocCategory {
    BUNDLES("bundles"),
    DEF1_DEFENSE_DQ("defendant1DefenseDirectionsQuestionnaire"),
    DEF2_DEFENSE_DQ("defendant2DefenseDirectionsQuestionnaire"),

    DEF1_SCHEDULE_OF_LOSS("RespondentOneSchedulesOfLoss"),
    DEF2_SCHEDULE_OF_LOSS("RespondentTwoSchedulesOfLoss"),

    APP1_DQ("directionsQuestionnaire"),
    APP1_REPLIES_TO_FURTHER_INFORMATION("ApplicantRepliesToFurtherInformation"),
    APP1_REQUEST_FOR_FURTHER_INFORMATION("ApplicantRequestsForFurtherInformation"),
    APP1_REQUEST_SCHEDULE_OF_LOSS("ApplicantSchedulesOfLoss"),
    APP1_REPLY("reply"),
    CLAIMANT1_DETAILS_OF_CLAIM("detailsOfClaim"),
    PARTICULARS_OF_CLAIM("particularsOfClaim"),

    APP2_DQ("ApplicantTwoDirectionsQuestionnaire"),
    APP2_REPLIES_TO_FURTHER_INFORMATION("ApplicantTwoRepliesToFurtherInformation"),
    APP2_REQUEST_FOR_FURTHER_INFORMATION("ApplicantTwoRequestsForFurtherInformation"),
    APP2_REQUEST_SCHEDULE_OF_LOSS("ApplicantTwoSchedulesOfLoss"),
    APP2_REPLY("ApplicantTwoReply"),
    CLAIMANT2_DETAILS_OF_CLAIM("ApplicantTwoDetailsOfClaim"),
    APP2_PARTICULARS_OF_CLAIM("ApplicantTwoParticularsOfClaim"),

    DQ_APP1("DQApplicant"),
    DQ_APP2("DQApplicantTwo"),
    DQ_DEF1("DQRespondent"),
    DQ_DEF2("DQRespondentTwo"),
    HEARING_NOTICES("hearingNotices"),
    NOTICE_OF_DISCONTINUE("discontinueNotices"),
    CASE_MAANGEMENT_ORDERS("caseManagementOrders"),
    APPLICATION_ORDERS("applicationOrders"),
    JUDGEMENTS("judgments"),
    CLAIMANT_QUERY_DOCUMENTS("ClaimantQueryDocuments"),
    CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS("ClaimantQueryDocumentAttachments"),
    DEFENDANT_QUERY_DOCUMENTS("DefendantQueryDocuments"),
    DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS("DefendantQueryDocumentAttachments"),
    CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS("CaseWorkerQueryDocumentsDocumentAttachments");

    private final String value;
}
