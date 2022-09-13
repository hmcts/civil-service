package uk.gov.hmcts.reform.prd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HearingChannel {

    @JsonProperty("category_key")
    private String categoryKey;
    private String key;
    @JsonProperty("value_en")
    private String valueEn;
    @JsonProperty("value_cy")
    private String valueCy;
    @JsonProperty("hint_text_en")
    private Boolean hintTextEn;
    @JsonProperty("hint_text_cy")
    private Boolean hintTextCy;
    @JsonProperty("parent_category")
    private String parentCategory;
    @JsonProperty("parent_key")
    private Integer parentKey;
    @JsonProperty("active_flag")
    private String activeFlag;

}
