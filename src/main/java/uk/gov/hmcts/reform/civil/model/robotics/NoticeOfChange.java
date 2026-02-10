package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NoticeOfChange {

    @JsonProperty("litigiousPartyID")
    private String litigiousPartyID;
    @JsonProperty("dateOfNoC")
    private String dateOfNoC;
}
