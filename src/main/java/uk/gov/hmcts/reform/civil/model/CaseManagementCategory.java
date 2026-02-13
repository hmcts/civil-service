package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseManagementCategory {

    private CaseManagementCategoryElement value;
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<CaseManagementCategoryElement>> list_items;
}
