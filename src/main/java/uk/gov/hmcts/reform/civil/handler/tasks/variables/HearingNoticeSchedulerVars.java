package uk.gov.hmcts.reform.civil.handler.tasks.variables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.util.List;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class HearingNoticeSchedulerVars implements MappableObject {

    private String serviceId;
    private List<String> dispatchedHearingIds;
    private int totalNumberOfUnnotifiedHearings;

}
