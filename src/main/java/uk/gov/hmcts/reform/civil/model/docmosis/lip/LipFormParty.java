package uk.gov.hmcts.reform.civil.model.docmosis.lip;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Optional;

@Builder
public record LipFormParty(String name, boolean isIndividual, boolean isSoleTrader, boolean isCompany,
                           boolean isOrganisation, String soleTraderBusinessName, String contactPerson,
                           @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
                           String phone, String email, Address primaryAddress, Address correspondenceAddress) {

    public static LipFormParty toLipFormParty(Party party, Address correspondenceAddress, String respondent1LiPContactPerson) {
        return LipFormParty.builder()
            .name(party.getPartyName())
            .phone(party.getPartyPhone())
            .email(party.getPartyEmail())
            .primaryAddress(party.getPrimaryAddress())
            .isIndividual(party.isIndividual())
            .isSoleTrader(party.isSoleTrader())
            .isOrganisation(party.isOrganisation())
            .isCompany(party.isCompany())
            .contactPerson(respondent1LiPContactPerson)
            .soleTraderBusinessName(party.getSoleTraderTradingAs())
            .dateOfBirth(Optional.ofNullable(party.getIndividualDateOfBirth()).orElse(party.getSoleTraderDateOfBirth()))
            .correspondenceAddress(correspondenceAddress)
            .build();
    }
}
