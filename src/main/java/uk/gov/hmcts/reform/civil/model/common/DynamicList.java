package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Representation of a CCD Dynamic List which is then converted to a select dropdown list.
 */
@Data
@Jacksonized
@Builder
public class DynamicList {

    /**
     * The selected value for the dropdown.
     */
    private DynamicListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;

    public static DynamicList fromList(List<String> list) {
        List<DynamicListElement> items = list.stream()
            .map(DynamicListElement::dynamicElement)
            .collect(toList());

        return DynamicList.builder().listItems(items).value(DynamicListElement.EMPTY).build();
    }
}
