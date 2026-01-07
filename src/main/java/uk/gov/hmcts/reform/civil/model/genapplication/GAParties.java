package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@Accessors(chain = true)
public class GAParties {

    private String litigiousPartyID;
    private String applicantPartyName;

    @JsonCreator
    GAParties(@JsonProperty("litigiousPartyID") String litigiousPartyID,
              @JsonProperty("applicantPartyName") String applicantPartyName) {
        this.litigiousPartyID = litigiousPartyID;
        this.applicantPartyName = applicantPartyName;
    }
}
