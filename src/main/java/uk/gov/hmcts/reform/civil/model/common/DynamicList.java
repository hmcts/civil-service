package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Representation of a CCD Dynamic List which is then converted to a select dropdown list.
 */
@Data
@Jacksonized
@Builder(toBuilder = true)
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
            .toList();

        return DynamicList.builder().listItems(items).value(DynamicListElement.EMPTY).build();
    }

    /**
     * A dynamic list can be pre-populated with a code and value.
     *
     * @param list    the original list of items
     * @param toCode  (optional) how to populate the DynamicListElement code, defaults to random UUID
     * @param toLabel how to create the label
     * @param value   (optional) value to be selected
     * @param <T>     type of element
     * @return dynamic list, possibly with value set
     */
    public static <T> DynamicList fromList(List<T> list, Function<T, String> toCode, Function<T, String> toLabel, T value, boolean sort) {
        List<DynamicListElement> items = list.stream()
            .map(item -> toCode != null
                ? DynamicListElement.dynamicElementFromCode(toCode.apply(item), toLabel.apply(item))
                : DynamicListElement.dynamicElement(toLabel.apply(item)))
            .collect(toList());

        int index = value != null ? list.indexOf(value) : -1;
        DynamicListElement chosen;
        if (index > -1) {
            chosen = items.get(index);
        } else {
            chosen = DynamicListElement.EMPTY;
        }

        if (sort) {
            items.sort(Comparator.comparing(DynamicListElement::getLabel));
        }

        return DynamicList.builder().listItems(items).value(chosen).build();
    }

    /**
     * Sometimes a dynamic list can be pre-populated with a value.
     *
     * @param list    the original list of items
     * @param toLabel how to create the label
     * @param value   (optional) value to be selected
     * @param <T>     type of element
     * @return dynamic list, possibly with value set
     */
    public static <T> DynamicList fromList(List<T> list, Function<T, String> toLabel, T value, boolean sort) {
        return fromList(list, null, toLabel, value, sort);
    }

    public static DynamicList fromDynamicListElementList(List<DynamicListElement> list) {
        return DynamicList.builder().listItems(list).value(DynamicListElement.EMPTY).build();
    }
}
