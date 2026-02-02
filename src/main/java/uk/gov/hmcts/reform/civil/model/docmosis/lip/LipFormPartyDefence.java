package uk.gov.hmcts.reform.civil.model.docmosis.lip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public record LipFormPartyDefence(String name, boolean isIndividual,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
                                  String phone, String email, Address primaryAddress, Address correspondenceAddress) {

    @JsonIgnore
    public static LipFormPartyDefence toLipDefenceParty(Party party) {
        if (party == null) {
            return null;
        }
        return getLipDefenceFormPartyWithPartyData(party, null);
    }

    @JsonIgnore
    public static LipFormPartyDefence toLipDefenceParty(Party party, Address correspondenceAddress) {
        if (party == null) {
            return null;
        }
        return getLipDefenceFormPartyWithPartyData(party, correspondenceAddress);
    }

    private static LipFormPartyDefence getLipDefenceFormPartyWithPartyData(Party party, Address correspondenceAddress) {
        LocalDate dateOfBirth = Stream.of(party.getIndividualDateOfBirth(), party.getSoleTraderDateOfBirth())
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        return new LipFormPartyDefence(
            party.getPartyName(),
            party.isIndividual() || party.isSoleTrader(),
            dateOfBirth,
            party.getPartyPhone(),
            party.getPartyEmail(),
            party.getPrimaryAddress(),
            correspondenceAddress
        );
    }
}
