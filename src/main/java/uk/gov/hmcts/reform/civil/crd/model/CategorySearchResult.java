package uk.gov.hmcts.reform.civil.crd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchResult {

    @JsonProperty("list_of_values")
    private List<Category> categories;
}
