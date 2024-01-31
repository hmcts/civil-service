package uk.gov.hmcts.reform.civil.model.docmosis.lip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

@Builder
public record LipFormPartyDefence(String name, boolean isIndividual,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
                                  String phone, String email, Address primaryAddress, Address correspondenceAddress) {

    @JsonIgnore
    public static LipFormPartyDefence toLipDefenceParty(Party party) {
        if (party == null) {
            return null;
        }
        return getLipDefenceFormPartyBuilderWithPartyData(party).build();
    }

    @JsonIgnore
    public static LipFormPartyDefence toLipDefenceParty(Party party, Address correspondenceAddress) {
        if (party == null) {
            return null;
        }
        LipFormPartyDefenceBuilder builder = getLipDefenceFormPartyBuilderWithPartyData(party);
        builder.correspondenceAddress(correspondenceAddress);
        return builder.build();
    }

    private static LipFormPartyDefenceBuilder getLipDefenceFormPartyBuilderWithPartyData(Party party) {
        LipFormPartyDefenceBuilder builder = LipFormPartyDefence.builder()
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
}
