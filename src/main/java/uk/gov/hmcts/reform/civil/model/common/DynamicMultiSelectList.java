package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a CCD DynamicMultiSelectList which is then converted to checkboxes.
 */
@Data
@Jacksonized
@Builder(toBuilder = true)
public class DynamicMultiSelectList {

    /**
     * The selected value for the dropdown.
     */
    private List<DynamicListElement> value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;

    public static DynamicMultiSelectList fromList(List<String> list) {
        List<DynamicListElement> items = list.stream()
            .map(DynamicListElement::dynamicElement)
            .toList();

        return DynamicMultiSelectList.builder().listItems(items).value(new ArrayList<>()).build();
    }

    public static DynamicMultiSelectList fromDynamicListElementList(List<DynamicListElement> list) {
        return DynamicMultiSelectList.builder().listItems(list).value(new ArrayList<>()).build();
    }
}
