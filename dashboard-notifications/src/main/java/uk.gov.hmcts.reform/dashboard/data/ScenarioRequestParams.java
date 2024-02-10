package uk.gov.hmcts.reform.dashboard.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScenarioRequestParams {

    Map<String, Object> params = new HashMap<>();
}
