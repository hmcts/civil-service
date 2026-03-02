package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimData {

    private Amount amount;
    private List<CmcParty> claimants;
    private List<CmcParty> defendants;
    private BreathingSpace breathingSpace;

    public String getClaimantName() {
        return getPartyName(claimants);
    }

    public String getDefendantName() {
        return getPartyName(defendants);
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return breathingSpace != null && breathingSpace.applies();
    }

    private String getPartyName(List<CmcParty> parties) {
        if (parties.isEmpty()) {
            return "";
        }
        return parties.stream().map(party -> party.getName()).collect(Collectors.joining(", "));
    }
}
