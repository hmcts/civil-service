package uk.gov.hmcts.reform.civil.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * An element of the {@link DynamicList}.
 *
 * <p>There are two properties which map to the relevant items of an option html tag.
 */
@Data
@Jacksonized
@Builder
@AllArgsConstructor
@NoArgsConstructor()
public class DynamicListElement {

    public static final DynamicListElement EMPTY = DynamicListElement.builder().build();

    /**
     * Property that maps to the value attribute of the option tag.
     */
    private String code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    private String label;

    public static DynamicListElement dynamicElement(String label) {
        return DynamicListElement.builder()
            .code(UUID.randomUUID().toString())
            .label(label)
            .build();
    }

    public static DynamicListElement dynamicElementFromCode(String code, String label) {
        return DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();
    }
}
