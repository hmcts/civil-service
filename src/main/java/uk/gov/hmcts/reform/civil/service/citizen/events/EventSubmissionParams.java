package uk.gov.hmcts.reform.civil.service.citizen.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSubmissionParams {

    private static final String DRAFT_CLAIM_ID = "draft";

    private String authorisation;
    private String userId;
    private String caseId;
    private CaseEvent event;
    private Map<String, Object> updates;

    public boolean isDraftClaim() {
        return caseId.equals(DRAFT_CLAIM_ID);
    }
}
