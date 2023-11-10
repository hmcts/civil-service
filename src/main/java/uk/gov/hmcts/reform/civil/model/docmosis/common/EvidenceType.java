package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EvidenceType {
    CONTRACTS_AND_AGREEMENTS("Contracts and agreements"),
    EXPERT_WITNESS("Expert witness"),
    LETTERS_EMAILS_AND_OTHER_CORRESPONDENCE("Letters, emails and other correspondence"),
    PHOTO_EVIDENCE("Photo Evidence"),
    RECEIPTS("Receipts"),
    STATEMENT_OF_ACCOUNT("Statement of account"),
    OTHER("Other");

    private String displayValue;
}
