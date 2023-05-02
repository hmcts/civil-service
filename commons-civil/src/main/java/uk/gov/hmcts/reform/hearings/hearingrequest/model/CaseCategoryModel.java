package uk.gov.hmcts.reform.hearings.hearingrequest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseCategoryModel {

    private CategoryType categoryType;
    private String categoryValue;
    private String categoryParent;
}

