package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.camunda.bpm.engine.variable.VariableMap;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExternalTaskData {

    private CaseData caseData;
    private CaseData generalApplicationData;
    private GeneralApplicationCaseData parentCaseData;
    private GeneralApplicationCaseData updateGeneralApplicationCaseData;
    private VariableMap variables;

    public Optional<CaseData> caseData() {
        return Optional.ofNullable(caseData);
    }

    public Optional<CaseData> generalApplicationData() {
        return Optional.ofNullable(generalApplicationData);
    }

    public Optional<GeneralApplicationCaseData> parentCaseData() {
        return Optional.ofNullable(parentCaseData);
    }

    public Optional<GeneralApplicationCaseData> updateGeneralApplicationCaseData() {
        return Optional.ofNullable(updateGeneralApplicationCaseData);
    }

    public Optional<VariableMap> variables() {
        return Optional.ofNullable(variables);
    }
}
