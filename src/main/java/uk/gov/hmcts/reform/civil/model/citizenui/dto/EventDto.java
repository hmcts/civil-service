package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
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
public class EventDto {

    private CaseEvent event;
    private Map<String, Object> caseDataUpdate;

    @JsonAnySetter
    public void addUpdate(String key, Object value) {
        caseDataUpdate.put(key, value);
    }

}
