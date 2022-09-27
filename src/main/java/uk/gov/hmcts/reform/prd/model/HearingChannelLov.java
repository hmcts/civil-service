package uk.gov.hmcts.reform.prd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HearingChannelLov {

    @JsonProperty("list_of_values")
    private List<HearingChannel> values;
}
