package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GACaseManagementCategory {

    @CCD(label = " ", searchable = false)
    private GACaseManagementCategoryElement value;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseManagementCategoryElement"
    )
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<GACaseManagementCategoryElement>> list_items;
}
