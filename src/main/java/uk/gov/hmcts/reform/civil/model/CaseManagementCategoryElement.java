package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseManagementCategoryElement {

    @CCD(label = "Civil", searchable = false)
    private String code;
    @CCD(label = "Civil", searchable = false)
    private String label;

}
