package uk.gov.hmcts.reform.unspec.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.unspec.sampledata.PartyBuilder.DATE_OF_BIRTH;

class PartyUtilsTest {

    @Nested
    class PartyNameBasedOnType {
        @Test
        void shouldThrowNullPointer_whenPartyTypeIsNull() {
            Party party = Party.builder().type(null).build();
            assertThrows(NullPointerException.class, () -> PartyUtils.getPartyNameBasedOnType(party));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividual() {
            Party individual = Party.builder()
                .individualTitle("Mr")
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsIndividualWithoutTitle() {
            Party individual = Party.builder()
                .individualFirstName("Jacob")
                .individualLastName("Martin")
                .type(Party.Type.INDIVIDUAL).build();

            assertEquals("Jacob Martin", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsCompany() {
            Party individual = Party.builder()
                .companyName("XYZ Company House")
                .type(Party.Type.COMPANY).build();

            assertEquals("XYZ Company House", PartyUtils.getPartyNameBasedOnType(individual));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsOrganisation() {
            Party organisation = Party.builder()
                .organisationName("ABC Solutions")
                .type(Party.Type.ORGANISATION).build();

            assertEquals("ABC Solutions", PartyUtils.getPartyNameBasedOnType(organisation));
        }

        @Test
        void shouldProvideName_whenPartyTypeIsSoleTrader() {
            Party soleTrader = Party.builder()
                .soleTraderTitle("Mr")
                .soleTraderFirstName("Jacob")
                .soleTraderLastName("Martin")
                .type(Party.Type.SOLE_TRADER).build();

            assertEquals("Mr Jacob Martin", PartyUtils.getPartyNameBasedOnType(soleTrader));
        }
    }

    @Nested
    class PartyDateOfBirth {

        @ParameterizedTest
        @EnumSource(value = Party.Type.class, names = {"SOLE_TRADER", "INDIVIDUAL"})
        void shouldReturnDateOfBirth_whenPartyTypeIsIndividualOrSoleTrader(Party.Type type) {
            Party party = PartyBuilder.builder().ofType(type).build();

            assertThat(PartyUtils.getDateOfBirth(party)).contains(DATE_OF_BIRTH);
        }

        @ParameterizedTest
        @EnumSource(value = Party.Type.class, mode = EnumSource.Mode.EXCLUDE, names = {"SOLE_TRADER", "INDIVIDUAL"})
        void shouldReturnEmpty_whenPartyTypeIsNotIndividualOrSoleTrader(Party.Type type) {
            Party party = PartyBuilder.builder().ofType(type).build();

            assertThat(PartyUtils.getDateOfBirth(party)).isEmpty();
        }
    }
}
