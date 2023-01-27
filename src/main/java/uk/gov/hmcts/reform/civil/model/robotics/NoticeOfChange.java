package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoticeOfChange {

    @JsonProperty("litigiousPartyID")
    private String litigiousPartyID;
    @JsonProperty("dateOfNoC")
    private String dateOfNoC;
}
