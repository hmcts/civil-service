package uk.gov.hmcts.reform.civil.model.bulkclaims;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseworkerSubmitEventDTo {

    private CaseEvent event;
    private Map<String, Object> data;

}
