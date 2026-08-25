package uk.gov.hmcts.reform.civil.callback;

import lombok.Getter;

@Getter
public enum CmcCaseEvent {

    MIGRATE_CASE("migrateCase");

    private final String eventName;

    CmcCaseEvent(String eventName) {
        this.eventName = eventName;
    }

}
