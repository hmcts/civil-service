package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class GAParties {

    private final String litigiousPartyID;
    private final String applicantPartyName;

    @JsonCreator
    GAParties(@JsonProperty("litigiousPartyID") String litigiousPartyID,
              @JsonProperty("applicantPartyName") String applicantPartyName) {
        this.litigiousPartyID = litigiousPartyID;
        this.applicantPartyName = applicantPartyName;
    }
}
