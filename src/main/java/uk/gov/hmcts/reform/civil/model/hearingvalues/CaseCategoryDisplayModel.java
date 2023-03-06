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
public class CaseCategoryDisplayModel {
    private String categoryDisplayValue;
    private List<CaseCategoryDisplayModel> childNodes;
}
