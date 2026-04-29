package uk.gov.hmcts.reform.civil.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * An element of the {@link DynamicList}.
 *
 * <p>There are two properties which map to the relevant items of an option html tag.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Accessors(chain = true)
public class DynamicListElement {

    public static final DynamicListElement EMPTY = new DynamicListElement();

    /**
     * Property that maps to the value attribute of the option tag.
     */
    private String code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    private String label;

    public static DynamicListElement dynamicElement(String label) {
        return new DynamicListElement()
            .setCode(UUID.randomUUID().toString())
            .setLabel(label);
    }

    public static DynamicListElement dynamicElementFromCode(String code, String label) {
        return new DynamicListElement()
            .setCode(code)
            .setLabel(label);
    }
}
