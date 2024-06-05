package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseManagementCategory {

    private CaseManagementCategoryElement value;
    @SuppressWarnings("checkstyle:MemberName")
    @JsonProperty("list_items")
    private List<Element<CaseManagementCategoryElement>> listItems;
}
