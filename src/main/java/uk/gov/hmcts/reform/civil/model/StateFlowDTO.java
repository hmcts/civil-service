package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StateFlowDTO {

    private State state;
    private List<State> stateHistory;
    private Map<String, Boolean> flags;

    public boolean isFlagSet(FlowFlag flowFlag) {
        return Optional.ofNullable(getFlags().get(flowFlag.name())).orElse(false);
    }
}
