package uk.gov.hmcts.reform.civil.service.flowstate.repository;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Set;

public interface AllowedEventRepository {

    Set<CaseEvent> getWhitelist();

    Set<CaseEvent> getFlowStateAllowedEvents(String scenarioKey, String stateFullName);

    Set<CaseEvent> getNoOngoingBPAllowedEvents(String fileName);

}
