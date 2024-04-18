package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

@Data
@Builder(toBuilder = true)
public class ScenarioRequestParams {

    HashMap<String, Object> params;

    @JsonCreator
    public ScenarioRequestParams(HashMap<String, Object> params) {
        this.params = params;
    }
}
