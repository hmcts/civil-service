package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GACaseManagementCategory {

    private GACaseManagementCategoryElement value;
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<GACaseManagementCategoryElement>> list_items;
}
