package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseManagementCategory {

    @CCD(label = " ", searchable = false)
    private CaseManagementCategoryElement value;
    @CCD(label = " ", searchable = false)
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<CaseManagementCategoryElement>> list_items;
}
