package uk.gov.hmcts.reform.civil.handler.tasks.variables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.util.List;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HearingNoticeSchedulerVars implements MappableObject {

    private String serviceId;
    private List<String> dispatchedHearingIds;
    private int totalNumberOfUnnotifiedHearings;

}
