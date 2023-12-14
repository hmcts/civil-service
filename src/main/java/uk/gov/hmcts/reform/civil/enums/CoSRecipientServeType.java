package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CoSRecipientServeType {
    HANDED("Personally handed it to or left it with"),
    DELIVERED("Delivered to or left at permitted place"),
    POSTED("Sent by first class post or another service which delivers on the next business day"),
    OTHER("Other means permitted by the court");

    private final String label;
}
