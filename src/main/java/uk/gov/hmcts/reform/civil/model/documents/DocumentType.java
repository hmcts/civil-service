package uk.gov.hmcts.reform.civil.model.documents;

public enum DocumentType {
    SEALED_CLAIM,
    ACKNOWLEDGEMENT_OF_CLAIM,
    ACKNOWLEDGEMENT_OF_SERVICE,
    DIRECTIONS_QUESTIONNAIRE,
    DEFENDANT_DEFENCE,
    DEFENDANT_DRAFT_DIRECTIONS,
    DEFAULT_JUDGMENT,
    CLAIMANT_DEFENCE,
    CLAIMANT_DRAFT_DIRECTIONS,
    DEFAULT_JUDGMENT_SDO_ORDER,
    SDO_ORDER,

    //General Application Document Type
    GENERAL_ORDER,
    DIRECTION_ORDER,
    DISMISSAL_ORDER,
    REQUEST_FOR_INFORMATION,
    HEARING_ORDER,
    WRITTEN_REPRESENTATION_SEQUENTIAL,
    WRITTEN_REPRESENTATION_CONCURRENT;
}
