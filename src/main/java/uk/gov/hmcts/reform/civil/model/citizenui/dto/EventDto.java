package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EventDto {

    private CaseEvent event;
    private Map<String, Object> caseDataUpdate;

}
