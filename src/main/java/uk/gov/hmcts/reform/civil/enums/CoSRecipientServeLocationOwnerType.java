package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoSRecipientServeLocationOwnerType {
    FRIEND("litigation friend's"),
    SOLICITOR("solicitor's"),
    DEFENDANT("defendant's"),
    CLAIMANT("claimant's");

    private final String label;
}
