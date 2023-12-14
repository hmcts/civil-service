package uk.gov.hmcts.reform.civil.service.bulkclaims;

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
public class CaseworkerEventSubmissionParams {

    private String authorisation;
    private String userId;
    private CaseEvent event;
    private Map<String, Object> updates;

}
