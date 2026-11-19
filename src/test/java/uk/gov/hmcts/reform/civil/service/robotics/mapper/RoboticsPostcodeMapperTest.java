package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RoboticsPostcodeMapperTest {

    private final PostcodeLookupService lookup = mock(PostcodeLookupService.class);
    private final RoboticsPostcodeMapper mapper = new RoboticsPostcodeMapper(lookup);

    @ParameterizedTest
    @ValueSource(strings = {"TD12 4AA"})
    void movesOutsidePostcodesInBothPayloadTypes(String postcode) throws Exception {
        when(lookup.findCountryForPostcode(postcode.replace(" ", ""))).thenReturn(Optional.of("Scotland"));
        RoboticsAddress partyAddress = address(postcode, null);
        RoboticsAddress solicitorAddress = address(postcode, "Midlothian");
        List<LitigiousParty> parties = List.of(new LitigiousParty().setAddresses(wrap(partyAddress)));
        List<Solicitor> solicitors = List.of(Solicitor.builder().addresses(wrap(solicitorAddress)).build());

        mapper.mapPostcodes(null, parties, solicitors);

        RoboticsCaseDataSpec spec = new RoboticsCaseDataSpec().setLitigiousParties(parties).setSolicitors(solicitors);
        RoboticsCaseData unspec = new RoboticsCaseData().setLitigiousParties(parties).setSolicitors(solicitors);
        for (String json : List.of(spec.toJsonString(), unspec.toJsonString())) {
            JsonNode root = new ObjectMapper().readTree(json);
            assertThat(root.at("/litigiousParties/0/addresses/contactAddress/postCode").isNull()).isTrue();
            assertThat(root.at("/litigiousParties/0/addresses/contactAddress/addressLine3").asText()).isEqualTo(postcode);
            assertThat(root.at("/solicitors/0/addresses/contactAddress/postCode").isNull()).isTrue();
            assertThat(root.at("/solicitors/0/addresses/contactAddress/addressLine3").asText())
                .isEqualTo("Midlothian - " + postcode);
        }
        assertThat(partyAddress.getAddressLine1()).isEqualTo("10 High Street");
        assertThat(partyAddress.getAddressLine2()).isEqualTo("Town");
        assertThat(solicitorAddress.getAddressLine4()).isEqualTo("Country");
        verify(lookup).findCountryForPostcode(postcode.replace(" ", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SW1A 1AA", "CF10 1EP", "CA6 5AA", " sw1a 1aa "})
    void preservesEnglandAndWalesAddresses(String postcode) {
        RoboticsAddress address = address(postcode, "Existing line");
        RoboticsAddress expected = address(postcode, "Existing line");

        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);

        assertThat(address).isEqualTo(expected);
        verifyNoInteractions(lookup);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void handlesMissingPostcodesAndAddresses(String postcode) {
        RoboticsAddress address = address(postcode, "Existing line");
        mapper.mapPostcodes(null, List.of(new LitigiousParty(), new LitigiousParty().setAddresses(wrap(address))),
                            List.of(Solicitor.builder().addresses(new RoboticsAddresses()).build()));
        mapper.mapPostcodes(null, null, null);
        assertThat(address.getPostCode()).isEqualTo(postcode);
        assertThat(address.getAddressLine3()).isEqualTo("Existing line");
        verifyNoInteractions(lookup);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UK", "GBR", "Unknown", "U.K.", "CY"})
    void preservesUnknownPostcodes(String country) {
        RoboticsAddress address = address("SW1A 1AA", "Existing line").setCountry(country);
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(address.getAddressLine3()).isEqualTo("Existing line");
    }

    @ParameterizedTest
    @CsvSource({"10001,", "10001-1234, United Kingdom", "D02 X285,", "BT1 1AA, England", "EH1 1AA, Wales",
        "ABC, United States of America", "ABC, U.S.", "ABC, Holland"})
    void movesIdentifiableOutsidePostcodes(String postcode, String country) {
        RoboticsAddress address = address(postcode, null).setCountry(country);
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo(postcode);
        assertThat(address.getPostCode()).isNull();
        verifyNoInteractions(lookup);
    }

    @Test
    void preparesTheRewrittenJsonForMultipleDefendants() throws Exception {
        RoboticsAddress claimant = address("SW1A 1AA", null).setCountry("England");
        RoboticsAddress firstDefendant = address("08001", null).setCountry("Spain");
        RoboticsAddress secondDefendant = address("20121", "Milan").setCountry("Italy");
        RoboticsCaseDataSpec payload = new RoboticsCaseDataSpec().setLitigiousParties(
            java.util.stream.Stream.of(claimant, firstDefendant, secondDefendant)
                .map(value -> new LitigiousParty().setAddresses(wrap(value))).toList());

        String json = mapper.prepareJson(payload);

        assertThat(claimant.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(firstDefendant.getPostCode()).isNull();
        assertThat(secondDefendant.getAddressLine3()).isEqualTo("Milan - 20121");
        assertThat(json).doesNotContain("country");
        verifyNoInteractions(lookup);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "'AC1, AC5, AC9', BT1 1AA, DD1 2HS, IV1 1PL, , true",
        "'AC2, AC6, AC10', BT1 1AA, DD1 2HS, IV1 1PL, District, true",
        "'AC3, AC7, AC11', SW1A 1AA, CF10 1EP, B1 1BB, , false",
        "'AC4, AC8, AC12', SW1A 1AA, CF10 1EP, B1 1BB, District, false"
    })
    void preparesSpecifiedClaimantAndBothDefendantAddresses(String criteria, String claimant, String defendant1,
                                                           String defendant2, String line3, boolean outside)
        throws Exception {
        List<String> postcodes = List.of(claimant, defendant1, defendant2);
        List<String> ids = List.of("claimant", "defendant1", "defendant2");
        List<LitigiousParty> parties = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            RoboticsAddress address = address(postcodes.get(i), line3).setAddressLine4(null);
            parties.add(new LitigiousParty().setId(ids.get(i)).setAddresses(wrap(address)));
        }
        String json = mapper.prepareJson(new RoboticsCaseDataSpec().setLitigiousParties(parties));
        JsonNode result = new ObjectMapper().readTree(json).required("litigiousParties");
        assertThat(result.size()).as(criteria).isEqualTo(3);
        for (int i = 0; i < ids.size(); i++) {
            JsonNode party = result.get(i);
            assertThat(party.required("ID").textValue()).isEqualTo(ids.get(i));
            JsonNode address = party.at("/addresses/contactAddress");
            String postCode = postcodes.get(i);
            String expectedLine3 = outside && line3 != null ? line3 + " - " + postCode : line3;
            if (outside && line3 == null) {
                expectedLine3 = postCode;
            }
            assertThat(address.required("addressLine3").textValue()).isEqualTo(expectedLine3);
            if (outside) {
                assertThat(address.required("postCode").isNull()).isTrue();
            } else {
                assertThat(address.required("postCode").textValue()).isEqualTo(postCode);
            }
        }
        verifyNoInteractions(lookup);
    }

    @ParameterizedTest
    @ValueSource(ints = {25, 26, 50, 51})
    void usesLineThreeThroughTheSchemaLimitThenFallsBackLosslessly(int length) {
        String existing = "A".repeat(length);
        RoboticsAddress address = address("BT1 1AA", existing).setAddressLine4(null);
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo(length <= 50 ? existing + " - BT1 1AA" : existing);
        assertThat(address.getAddressLine4()).isEqualTo(length <= 50 ? null : "BT1 1AA");
        assertThat(address.getPostCode()).isNull();
    }

    @Test
    void retainsAllFieldsWhenNoLineHasRoomForThePostcode() {
        RoboticsAddress original = address("BT1 1AA", "A".repeat(60))
            .setAddressLine4("B".repeat(60)).setAddressLine5("C".repeat(60));
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(original))), null);
        assertThat(original.getAddressLine3()).isEqualTo("A".repeat(60));
        assertThat(original.getAddressLine4()).isEqualTo("B".repeat(60));
        assertThat(original.getAddressLine5()).isEqualTo("C".repeat(60));
        assertThat(original.getPostCode()).isEqualTo("BT1 1AA");
    }

    @Test
    void appendsPostcodeToTheTownWhenItOccupiesMappedLineThree() {
        Address source = new Address("10 High Street", "Building B", null, "Edinburgh", null, null, "EH1 1AA");
        RoboticsAddress address = new RoboticsAddressMapper(new AddressLinesMapper()).toRoboticsAddress(source);
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo("Edinburgh - EH1 1AA");
        assertThat(address.getPostCode()).isNull();
    }

    @Test
    void mapsInternationalClientsAndDomesticSolicitorsIndependently() throws Exception {
        RoboticsAddressMapper addresses = new RoboticsAddressMapper(new AddressLinesMapper());
        Address claimant = new Address("10 Example Street", "Barcelona", "Barcelona Province", null, null, "Spain", "08001");
        Address defendant = new Address("20 Example Street", "Milan", "Lombardy", null, null, "Italy", "20121");
        Address solicitor = new Address("30 Example Street", "Cardiff", null, null, null, "CY", "CF10 1EP");
        RoboticsCaseDataSpec payload = new RoboticsCaseDataSpec()
            .setLitigiousParties(List.of(new LitigiousParty().setAddresses(addresses.toRoboticsAddresses(claimant)),
                                         new LitigiousParty().setAddresses(addresses.toRoboticsAddresses(defendant))))
            .setSolicitors(List.of(Solicitor.builder().addresses(addresses.toRoboticsAddresses(solicitor)).build()));

        JsonNode json = new ObjectMapper().readTree(mapper.prepareJson(payload));
        JsonNode first = json.at("/litigiousParties/0/addresses/contactAddress");
        JsonNode second = json.at("/litigiousParties/1/addresses/contactAddress");
        assertThat(first.required("addressLine3").textValue()).isEqualTo("Barcelona Province - 08001");
        assertThat(second.required("addressLine3").textValue()).isEqualTo("Lombardy - 20121");
        assertThat(first.required("postCode").isNull()).isTrue();
        assertThat(second.required("postCode").isNull()).isTrue();
        assertThat(json.at("/solicitors/0/addresses/contactAddress/postCode").textValue()).isEqualTo("CF10 1EP");
        assertThat(claimant.getPostCode()).isEqualTo("08001");
        assertThat(defendant.getPostCode()).isEqualTo("20121");
        verifyNoInteractions(lookup);
    }

    @Test
    void previewReturnsPreparedPayloadWithoutSchemaValidation() throws Exception {
        RoboticsAddress address = address("BT1 1AA", null).setCountry("United Kingdom");
        RoboticsCaseDataSpec payload = new RoboticsCaseDataSpec().setLitigiousParties(
            List.of(new LitigiousParty().setAddresses(wrap(address))));
        JsonNode json = new ObjectMapper().readTree(mapper.prepareJson(payload));
        assertThat(json.at("/litigiousParties/0/addresses/contactAddress/addressLine3").asText()).isEqualTo("BT1 1AA");
    }

    @Test
    void cachesPostcodeCountriesAcrossPayloads() {
        when(lookup.findCountryForPostcode("TD124AA")).thenReturn(java.util.Optional.of("Scotland"));
        for (int index = 0; index < 2; index++) {
            mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address("TD12 4AA", null)))), null);
        }
        verify(lookup).findCountryForPostcode("TD124AA");
    }

    @ParameterizedTest
    @ValueSource(strings = {"bt1 1aa", "JE2 3AA", "GY1 1AA", "IM1 1AA", "G1 1AA", "KW1 1AA",
        "AB10 1AA", "DD1 2HS", "EH1 1AA", "FK8 1EG", "HS1 2QN", "IV1 1PL", "KA1 2PY", "KY1 1EX",
        "DG1 1AA", "DG14 0AA", "TD1 1AA", "TD10 6AA", "TD11 3AA", "TD13 5AA", "TD14 5AA",
        "ML3 6BU", "PA31 8QX", "PH21 1LU", "ZE1 0HA"})
    void resolvesOutsideAreasWithGenericCountry(String postcode) {
        RoboticsAddress address = address(postcode, null).setCountry("United Kingdom");
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo(postcode);
        assertThat(address.getPostCode()).isNull();
        verifyNoInteractions(lookup);
    }

    @Test
    void retriesAnInconclusiveLookup() {
        when(lookup.findCountryForPostcode("TD124AA")).thenReturn(Optional.empty()).thenReturn(Optional.of("Scotland"));
        RoboticsAddress address = address("TD12 4AA", null);
        var parties = List.of(new LitigiousParty().setAddresses(wrap(address)));
        mapper.mapPostcodes(null, parties, null);
        assertThat(address.getPostCode()).isEqualTo("TD12 4AA");
        mapper.mapPostcodes(null, parties, null);
        assertThat(address.getPostCode()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DG16 5AA", "TD5 7AA", "TD9 0AA", "TD12 4AA", "TD15 1AA"})
    void keepsBorderPostcodeWhenLookupReturnsEngland(String postcode) {
        String key = postcode.replace(" ", "");
        when(lookup.findCountryForPostcode(key)).thenReturn(Optional.of("England"));
        RoboticsAddress address = address(postcode, null).setCountry("United Kingdom");
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        verify(lookup).findCountryForPostcode(key);
        assertThat(address.getPostCode()).isEqualTo(postcode);
        assertThat(address.getAddressLine3()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USA", "Eire", "Éire", "Republic of Ireland"})
    void recognisesExplicitOverseasCountryAliases(String country) {
        RoboticsAddress address = address("12345", null).setCountry(country);
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo("12345");
        assertThat(address.getPostCode()).isNull();
        verifyNoInteractions(lookup);
    }

    @Test
    void keepsLongThirdLineAndTownInPlaceWhenPostcodeFits() {
        Address source = new Address("10 High Street", "Building B", "A".repeat(35), " ", "Dundee", null, "DD1 2HS");
        RoboticsAddress address = new RoboticsAddressMapper(new AddressLinesMapper()).toRoboticsAddress(source);
        assertThat(address.getAddressLine4()).isBlank();
        assertThat(address.getAddressLine5()).isEqualTo("Dundee");
        mapper.mapPostcodes(null, List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getAddressLine3()).isEqualTo("A".repeat(35) + " - DD1 2HS");
        assertThat(address.getAddressLine4()).isBlank();
        assertThat(address.getAddressLine5()).isEqualTo("Dundee");
        assertThat(address.getPostCode()).isNull();
    }

    private RoboticsAddress address(String postcode, String line3) {
        return new RoboticsAddress().setAddressLine1("10 High Street").setAddressLine2("Town")
            .setAddressLine3(line3).setAddressLine4("Country").setPostCode(postcode);
    }

    private RoboticsAddresses wrap(RoboticsAddress address) {
        return new RoboticsAddresses().setContactAddress(address);
    }
}
