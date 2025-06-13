package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum QueryMessageType {
    QUERY("Query"),
    RESPONSE("Caseworker response"),
    FOLLOW_UP("Follow up");

    private final String label;
}
