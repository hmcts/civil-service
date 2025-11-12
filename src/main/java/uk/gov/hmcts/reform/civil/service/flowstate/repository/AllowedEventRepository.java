package uk.gov.hmcts.reform.civil.service.flowstate.repository;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Set;

public interface AllowedEventRepository {

    Set<CaseEvent> getAllowedEvents(String scenarioKey, String stateFullName);

    Set<CaseEvent> getWhitelist();

    default boolean isWhitelistEvent(CaseEvent event) {
        return getWhitelist().contains(event);
    }

}
