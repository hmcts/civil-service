package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelRequirementsModel {

    private List<String> roleType;
    private List<String> authorisationTypes;
    private List<String> authorisationSubTypes;
    private List<PanelPreferenceModel> panelPreferences;
    private List<String> panelSpecialisms;
}
