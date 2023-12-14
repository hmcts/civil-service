package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceMethodType {
    POST(2, DateOrDateTime.DATE, "First class post"),
    DOCUMENT_EXCHANGE(2, DateOrDateTime.DATE, "Document exchange"),
    FAX(0, DateOrDateTime.DATE_TIME, "Fax"),
    EMAIL(0, DateOrDateTime.DATE_TIME, "Email"),
    PERSONAL_SERVICE(0, DateOrDateTime.DATE_TIME, "Personal service"),
    LEAVING_AT_PERMITTED_ADDRESS(0, DateOrDateTime.DATE_TIME, "Leaving at permitted address"),
    OTHER(2, DateOrDateTime.DATE_TIME, "Other");

    private final int days;
    private final DateOrDateTime dateOrDateTime;
    private final String label;

    private enum DateOrDateTime {
        DATE,
        DATE_TIME
    }

    public boolean requiresDateEntry() {
        return this.dateOrDateTime == DateOrDateTime.DATE;
    }
}
