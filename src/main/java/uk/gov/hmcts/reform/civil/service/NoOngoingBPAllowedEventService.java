package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S3077")
public class NoOngoingBPAllowedEventService {

    private static final String ALLOWED_EVENTS_FILE = "no-ongoing-bp-allowed-events.yml";

    private final AllowedEventRepository allowedEventRepository;

    public boolean isAllowed(CaseEvent event) {
        return allowedEventRepository.getNoOngoingBPAllowedEvents(ALLOWED_EVENTS_FILE).contains(event);
    }
}
