package uk.gov.hmcts.reform.dashboard.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScenarioRequestParams {

    /**
     * We will use the params to mapping the data.
     *
     * @param params variables
     * @return Sonar requests this to be serializable, hence HashMap instead of Map.
     */
    HashMap<String, Object> params;

    @JsonCreator
    @SuppressWarnings("unchecked")
    public ScenarioRequestParams(Map<String, Object> params) {
        if (params == null) {
            this.params = null;
        } else if (params instanceof HashMap<?, ?>) {
            this.params = (HashMap<String, Object>) params;
        } else {
            this.params = new HashMap<>(params);
        }
    }
}
