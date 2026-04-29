package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GACaseManagementCategory {

    private GACaseManagementCategoryElement value;
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<GACaseManagementCategoryElement>> list_items;
}
