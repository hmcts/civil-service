package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocCategory {
    BUNDLES("bundles"),
    DEF1_DEFENSE_DQ("defendant1DefenseDirectionsQuestionnaire"),
    DEF2_DEFENSE_DQ("defendant2DefenseDirectionsQuestionnaire"),
    APP1_DQ("directionsQuestionnaire"),
    DQ_APP1("DQApplicant"),
    DQ_DEF1("DQRespondent"),
    DQ_DEF2("DQRespondentTwo"),
    HEARING_NOTICES("hearingNotices"),
    NOTICE_OF_DISCONTINUE("discontinueNotices"),
    CLAIMANT1_DETAILS_OF_CLAIM("detailsOfClaim"),
    PARTICULARS_OF_CLAIM("particularsOfClaim"),
    CLAIMANT_QUERY_DOCUMENTS("ClaimantQueryDocuments"),
    CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS("ClaimantQueryDocumentAttachments"),
    DEFENDANT_QUERY_DOCUMENTS("DefendantQueryDocuments"),
    DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS("DefendantQueryDocumentAttachments");

    private final String value;
}
