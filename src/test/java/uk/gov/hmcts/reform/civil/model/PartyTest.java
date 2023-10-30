package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTest {

    @Test
    void shouldGetPartyNameAndDisplayType_whenInstantiated() {
        String companyName = "The name";
        Party.Type partyType = Party.Type.COMPANY;

        Party party = Party.builder().type(partyType).companyName(companyName).build();

        assertThat(party.getPartyName()).isEqualTo(companyName);
        assertThat(party.getPartyTypeDisplayValue()).isEqualTo("Company");
    }

    @Test
    void shouldGetIndividualPartyNameIncludingTitle_whenInstantiated() {
        Party party = Party.builder().type(Party.Type.INDIVIDUAL)
            .individualTitle("Mr")
            .individualFirstName("James")
            .individualLastName("Carver")
            .build();

        assertThat(party.getPartyName()).isEqualTo("Mr James Carver");
    }

    @Test
    void shouldGetIndividualPartyNameExcludingTitle_whenInstantiated() {
        Party party = Party.builder().type(Party.Type.INDIVIDUAL)
            .individualTitle("Mr")
            .individualFirstName("James")
            .individualLastName("Carver")
            .build();

        assertThat(party.getPartyName(true)).isEqualTo("James Carver");
    }

    @Test
    void shouldGetSoulTraderPartyNameIncludingTitle_whenInstantiated() {
        Party party = Party.builder().type(Party.Type.SOLE_TRADER)
            .soleTraderTitle("Mr")
            .soleTraderFirstName("James")
            .soleTraderLastName("Carver")
            .build();

        assertThat(party.getPartyName()).isEqualTo("Mr James Carver");
    }

    @Test
    void shouldGetSoulTraderPartyNameExcludingTitle_whenInstantiated() {
        Party party = Party.builder().type(Party.Type.SOLE_TRADER)
            .soleTraderTitle("Mr")
            .soleTraderFirstName("James")
            .soleTraderLastName("Carver")
            .build();

        assertThat(party.getPartyName(true)).isEqualTo("James Carver");
    }

    @Test
    void shouldReturnTrueWhenPartyIsCompany() {
        Party party = Party.builder().type(Party.Type.COMPANY)
            .build();

        assertThat(party.isCompanyOROrganistaion()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPartyIsOrganisation() {
        Party party = Party.builder().type(Party.Type.ORGANISATION)
            .build();

        assertThat(party.isCompanyOROrganistaion()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPartyIsNOTOrganisationOrCompany() {
        Party party = Party.builder().type(Party.Type.INDIVIDUAL)
            .build();

        assertThat(party.isCompanyOROrganistaion()).isFalse();
    }

}
