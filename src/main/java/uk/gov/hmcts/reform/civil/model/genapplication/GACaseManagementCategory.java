package uk.gov.hmcts.reform.civil.model.genapplication;

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
public class GACaseManagementCategory {

    private GACaseManagementCategoryElement value;
    @JsonProperty("list_items")
    @SuppressWarnings("checkstyle:MemberName")
    private List<Element<GACaseManagementCategoryElement>> listItems;
}
