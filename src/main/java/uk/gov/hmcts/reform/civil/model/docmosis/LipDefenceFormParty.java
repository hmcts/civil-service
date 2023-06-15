package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

@Builder
public record LipDefenceFormParty(String name, boolean isIndividual,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
                                  String phone, String email, Address primaryAddress, Address correspondenceAddress) {

    @JsonIgnore
    public static LipDefenceFormParty toLipDefenceParty(Party party) {
        if (party == null) {
            return null;
        }
        LipDefenceFormPartyBuilder builder = getLipDefenceFormPartyBuilderWithPartyData(party);
        return builder.build();
    }

    private static LipDefenceFormPartyBuilder getLipDefenceFormPartyBuilderWithPartyData(Party party) {
        LipDefenceFormPartyBuilder builder = LipDefenceFormParty.builder()
            .name(party.getPartyName())
            .phone(party.getPartyPhone())
            .email(party.getPartyEmail())
            .primaryAddress(party.getPrimaryAddress())
            .isIndividual(party.isIndividual() || party.isSoleTrader());
            Stream.of(party.getIndividualDateOfBirth(), party.getSoleTraderDateOfBirth())
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::dateOfBirth);
        return builder;
    }

    @JsonIgnore
    public static LipDefenceFormParty toLipDefenceParty(Party party, Address correspondenceAddress) {
        if (party == null) {
            return null;
        }
        LipDefenceFormPartyBuilder builder = getLipDefenceFormPartyBuilderWithPartyData(party);
        builder.correspondenceAddress(correspondenceAddress);
        return builder.build();
    }
}
