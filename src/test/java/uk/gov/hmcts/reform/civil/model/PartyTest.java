package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTest {

    @Test
    void shouldGetPartyNameAndDisplayType_whenInstantiated() {
        String companyName = "The name";
        Party.Type partyType = Party.Type.COMPANY;

        Party party = new Party().setType(partyType).setCompanyName(companyName);

        assertThat(party.getPartyName()).isEqualTo(companyName);
        assertThat(party.getPartyTypeDisplayValue()).isEqualTo("Company");
    }

    @Test
    void shouldGetIndividualPartyNameIncludingTitle_whenInstantiated() {
        Party party = new Party().setType(Party.Type.INDIVIDUAL)
            .setIndividualTitle("Mr")
            .setIndividualFirstName("James")
            .setIndividualLastName("Carver");

        assertThat(party.getPartyName()).isEqualTo("Mr James Carver");
    }

    @Test
    void shouldGetIndividualPartyNameExcludingTitle_whenInstantiated() {
        Party party = new Party().setType(Party.Type.INDIVIDUAL)
            .setIndividualTitle("Mr")
            .setIndividualFirstName("James")
            .setIndividualLastName("Carver");

        assertThat(party.getPartyName(true)).isEqualTo("James Carver");
    }

    @Test
    void shouldGetSoulTraderPartyNameIncludingTitle_whenInstantiated() {
        Party party = new Party().setType(Party.Type.SOLE_TRADER)
            .setSoleTraderTitle("Mr")
            .setSoleTraderFirstName("James")
            .setSoleTraderLastName("Carver");

        assertThat(party.getPartyName()).isEqualTo("Mr James Carver");
    }

    @Test
    void shouldGetSoulTraderPartyNameExcludingTitle_whenInstantiated() {
        Party party = new Party().setType(Party.Type.SOLE_TRADER)
            .setSoleTraderTitle("Mr")
            .setSoleTraderFirstName("James")
            .setSoleTraderLastName("Carver");

        assertThat(party.getPartyName(true)).isEqualTo("James Carver");
    }

    @Test
    void shouldReturnTrueWhenPartyIsCompany() {
        Party party = new Party().setType(Party.Type.COMPANY);

        assertThat(party.isCompanyOROrganisation()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPartyIsOrganisation() {
        Party party = new Party().setType(Party.Type.ORGANISATION);

        assertThat(party.isCompanyOROrganisation()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPartyIsNOTOrganisationOrCompany() {
        Party party = new Party().setType(Party.Type.INDIVIDUAL);

        assertThat(party.isCompanyOROrganisation()).isFalse();
    }

}
