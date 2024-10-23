package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.engine.variable.VariableMap;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTaskData {

    private CaseData caseData;
    private CaseData generalApplicationCaseData;
    private VariableMap variables;

    public Optional<CaseData> caseData() {
        return Optional.ofNullable(caseData);
    }

    public Optional<CaseData> generalApplicationData() {
        return Optional.ofNullable(generalApplicationCaseData);
    }

    public Optional<VariableMap> variables() {
        return Optional.ofNullable(variables);
    }
}
