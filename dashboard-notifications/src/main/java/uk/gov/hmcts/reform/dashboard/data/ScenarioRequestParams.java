package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

@Data
@Builder(toBuilder = true)
public class ScenarioRequestParams {

    /**
     * We will use the params to mapping the data.
     *
     * @param params variables
     * @return Sonar requests this to be serializable, hence HashMap instead of Map.
     */
    HashMap<String, Object> params;

    @SuppressWarnings("java:S1319")
    @JsonCreator
    public ScenarioRequestParams(HashMap<String, Object> params) {
        this.params = params;
    }
}
