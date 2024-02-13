package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
public class ScenarioRequestParams {

    Map<String, Object> params;

    @JsonCreator
    public ScenarioRequestParams(Map<String, Object> params) {
        this.params = params;
    }
}
