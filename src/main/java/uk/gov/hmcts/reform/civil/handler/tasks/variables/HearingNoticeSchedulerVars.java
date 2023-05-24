package uk.gov.hmcts.reform.civil.handler.tasks.variables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class HearingNoticeSchedulerVars implements MappableObject {
    private String serviceId;
    private List<String> dispatchedHearingIds;
    private int totalNumberOfUnnotifiedHearings;
}
