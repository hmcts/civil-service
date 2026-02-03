package uk.gov.hmcts.reform.civil.model.docmosis.lip;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;
import java.util.Optional;

public record LipFormParty(String name, boolean isIndividual, boolean isSoleTrader, boolean isCompany,
                           boolean isOrganisation, String soleTraderBusinessName, String contactPerson,
                           @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
                           String phone, String email, Address primaryAddress, Address correspondenceAddress) {

    public static LipFormParty toLipFormParty(Party party, Address correspondenceAddress, String respondent1LiPContactPerson) {
        return new LipFormParty(
            party.getPartyName(),
            party.isIndividual(),
            party.isSoleTrader(),
            party.isCompany(),
            party.isOrganisation(),
            party.getSoleTraderTradingAs(),
            respondent1LiPContactPerson,
            Optional.ofNullable(party.getIndividualDateOfBirth()).orElse(party.getSoleTraderDateOfBirth()),
            party.getPartyPhone(),
            party.getPartyEmail(),
            party.getPrimaryAddress(),
            correspondenceAddress
        );
    }
}
