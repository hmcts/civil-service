package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyFlagsModel {

    private String partyID;
    private String partyName;
    private String flagParentId;
    private String flagId;
    private String flagDescription;
    private String flagStatus;
}
