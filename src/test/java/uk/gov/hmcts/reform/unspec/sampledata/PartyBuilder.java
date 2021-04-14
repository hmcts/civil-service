package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.Party;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.unspec.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.unspec.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.unspec.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.unspec.model.Party.Type.SOLE_TRADER;

public class PartyBuilder {

    public  static final LocalDate DATE_OF_BIRTH = LocalDate.now().minusYears(20);

    private Party.Type type;
    private String individualTitle;
    private String individualFirstName;
    private String individualLastName;
    private LocalDate individualDateOfBirth;
    private String companyName;
    private String organisationName;
    private String soleTraderTitle;
    private String soleTraderFirstName;
    private String soleTraderLastName;
    private String soleTraderTradingAs;
    private LocalDate soleTraderDateOfBirth;
    private Address primaryAddress;
    private String partyName;

    public static PartyBuilder builder() {
        return new PartyBuilder();
    }

    public PartyBuilder ofType(Party.Type type) {
        switch (type) {
            case INDIVIDUAL:
                return individual();
            case SOLE_TRADER:
                return soleTrader();
            case COMPANY:
                return company();
            case ORGANISATION:
                return organisation();
            default:
                throw new IllegalArgumentException("Invalid party type: " + type);
        }
    }

    public PartyBuilder soleTrader() {
        type = SOLE_TRADER;
        soleTraderTitle = "Mr.";
        soleTraderFirstName = "Sole";
        soleTraderLastName = "Trader";
        soleTraderDateOfBirth = DATE_OF_BIRTH;
        soleTraderTradingAs = "Sole Trader co";
        partyName = soleTraderTitle + " " + soleTraderFirstName + " " + soleTraderLastName;
        primaryAddress = AddressBuilder.defaults().build();
        return this;
    }

    public PartyBuilder company() {
        type = COMPANY;
        companyName = "Company ltd";
        partyName = companyName;
        primaryAddress = AddressBuilder.defaults().build();
        return this;
    }

    public PartyBuilder companyWithMinimalData() {
        type = COMPANY;
        companyName = "C";
        partyName = companyName;
        primaryAddress = AddressBuilder.minimal().build();
        return this;
    }

    public PartyBuilder organisation() {
        type = ORGANISATION;
        organisationName = "The Organisation";
        partyName = organisationName;
        primaryAddress = AddressBuilder.defaults().build();
        return this;
    }

    public PartyBuilder individual() {
        type = INDIVIDUAL;
        individualTitle = "Mr.";
        individualFirstName = "John";
        individualLastName = "Rambo";
        individualDateOfBirth = DATE_OF_BIRTH;
        partyName = individualTitle + " " + individualFirstName + " " + individualLastName;
        primaryAddress = AddressBuilder.defaults().build();
        return this;
    }

    public PartyBuilder individualDateOfBirth(LocalDate dateOfBirth) {
        individualDateOfBirth = dateOfBirth;
        return this;
    }

    public PartyBuilder soleTraderDateOfBirth(LocalDate dateOfBirth) {
        soleTraderDateOfBirth = dateOfBirth;
        return this;
    }

    public Party build() {
        return Party.builder()
            .type(type)
            .individualTitle(individualTitle)
            .individualFirstName(individualFirstName)
            .individualLastName(individualLastName)
            .individualDateOfBirth(individualDateOfBirth)
            .partyName(partyName)
            .primaryAddress(primaryAddress)
            .companyName(companyName)
            .organisationName(organisationName)
            .soleTraderTitle(soleTraderTitle)
            .soleTraderFirstName(soleTraderFirstName)
            .soleTraderLastName(soleTraderLastName)
            .soleTraderDateOfBirth(soleTraderDateOfBirth)
            .soleTraderTradingAs(soleTraderTradingAs)
            .build();
    }
}
