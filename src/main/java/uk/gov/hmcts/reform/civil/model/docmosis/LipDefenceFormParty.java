package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class LipDefenceFormParty {

    private final String name;
    private final boolean isIndividual;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private final LocalDate dateOfBirth;
    private final String phone;
    private final String email;
    private final Address primaryAddress;
    private final Address correspondenceAddress;

    @JsonIgnore
    public static LipDefenceFormParty from(Party party) {
        if (party == null) {
            return null;
        }
        LipDefenceFormParty.LipDefenceFormPartyBuilder builder = LipDefenceFormParty.builder()
            .name(party.getPartyName())
            .phone(party.getPartyPhone())
            .email(party.getPartyEmail())
            .primaryAddress(party.getPrimaryAddress());
        if (party.isIndividual() || party.isSoleTrader()) {
            builder.isIndividual(true);
            Stream.of(party.getIndividualDateOfBirth(), party.getSoleTraderDateOfBirth())
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::dateOfBirth);
        } else {
            builder.isIndividual(false);
        }
        return builder.build();

    }
}
