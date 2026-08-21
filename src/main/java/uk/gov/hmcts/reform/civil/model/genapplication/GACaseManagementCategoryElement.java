package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GACaseManagementCategoryElement {

    private String code;
    private String label;

}
