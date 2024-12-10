package uk.gov.hmcts.reform.civil.service.robotics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class EventHistoryDTO {

    private EventHistory.EventHistoryBuilder builder;
    private CaseData caseData;
    private String authToken;
    private String eventType;
}
