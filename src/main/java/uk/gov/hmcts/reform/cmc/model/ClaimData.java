package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimData {

    private Amount amount;
    private List<CmcParty> claimants;
    private List<CmcParty> defendants;

    public String getClaimantName() {
        return getPartyName(claimants);
    }

    public String getDefendantName() {
        return getPartyName(defendants);
    }

    private String getPartyName(List<CmcParty> parties) {
        if (parties.isEmpty()) {
            return "";
        }
        return parties.stream().map(party -> party.getName()).collect(Collectors.joining(", "));
    }
}
