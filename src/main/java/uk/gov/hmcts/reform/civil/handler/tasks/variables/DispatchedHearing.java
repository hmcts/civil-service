package uk.gov.hmcts.reform.civil.handler.tasks.variables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class DispatchedHearing {

    private String hearingId;
    private int count;
}
