package uk.gov.hmcts.reform.cmc.model;

import lombok.Getter;

@Getter
public enum ClaimEvent {

    MIGRATE_CASE("migrateCase");

    private final String eventName;

    ClaimEvent(String eventName) {
        this.eventName = eventName;
    }

}
