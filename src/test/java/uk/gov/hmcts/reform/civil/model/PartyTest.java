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

}
