package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Representation of a CCD Dynamic Multi Select List which is rendered as a list of checkboxes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Accessors(chain = true)
public class DynamicMultiSelectList {

    /**
     * The selected values.
     */
    private List<DynamicListElement> value;

    /**
     * List of options for the checkboxes.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;
}
