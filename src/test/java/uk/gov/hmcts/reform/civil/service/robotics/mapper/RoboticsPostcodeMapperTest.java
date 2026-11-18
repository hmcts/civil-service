package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RoboticsPostcodeMapperTest {

    private final PostcodeLookupService lookup = mock(PostcodeLookupService.class);
    private final JsonSchemaValidationService schema = mock(JsonSchemaValidationService.class);
    private final RoboticsPostcodeMapper mapper = new RoboticsPostcodeMapper(lookup, schema);

    @ParameterizedTest
    @ValueSource(strings = {"EH1 1AA", "BT1 5GS", "08001", "20121"})
    void movesOutsidePostcodesInBothPayloadTypes(String postcode) throws Exception {
        when(lookup.findCountryForPostcode(postcode.replace(" ", ""))).thenReturn(Optional.of("Scotland"));
        RoboticsAddress partyAddress = address(postcode, null);
        RoboticsAddress solicitorAddress = address(postcode, "Midlothian");
        List<LitigiousParty> parties = List.of(new LitigiousParty().setAddresses(wrap(partyAddress)));
        List<Solicitor> solicitors = List.of(Solicitor.builder().addresses(wrap(solicitorAddress)).build());

        mapper.mapPostcodes(parties, solicitors);

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
    @ValueSource(strings = {"SW1A 1AA", "CF10 1EP", " sw1a 1aa "})
    void preservesEnglandAndWalesAddresses(String postcode) {
        when(lookup.findCountryForPostcode(postcode.replaceAll("\\s", "").toUpperCase(java.util.Locale.UK)))
            .thenReturn(Optional.of("England"));
        RoboticsAddress address = address(postcode, "Existing line");
        RoboticsAddress expected = address(postcode, "Existing line");

        mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address))), null);

        assertThat(address).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void handlesMissingPostcodesAndAddresses(String postcode) {
        RoboticsAddress address = address(postcode, "Existing line");
        mapper.mapPostcodes(List.of(new LitigiousParty(), new LitigiousParty().setAddresses(wrap(address))),
                            List.of(Solicitor.builder().addresses(new RoboticsAddresses()).build()));
        mapper.mapPostcodes(null, null);
        assertThat(address.getPostCode()).isEqualTo(postcode);
        assertThat(address.getAddressLine3()).isEqualTo("Existing line");
        verifyNoInteractions(lookup);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UK", "GBR", "Unknown", "U.K."})
    void preservesUnknownPostcodes(String country) {
        when(lookup.findCountryForPostcode("SW1A1AA")).thenReturn(Optional.empty());
        RoboticsAddress address = address("SW1A 1AA", "Existing line").setCountry(country);
        mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(address.getAddressLine3()).isEqualTo("Existing line");
    }

    @Test
    void retainsLongAddressTextAndPostcodeWithinLineLimits() {
        RoboticsAddress address = address("EH12 9AB", "A".repeat(35)).setCountry("Scotland");
        mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getPostCode()).isNull();
        assertThat(address.getAddressLine3()).isEqualTo("EH12 9AB");
        assertThat(address.getAddressLine4()).hasSizeLessThanOrEqualTo(35);
        assertThat(address.getAddressLine5()).hasSizeLessThanOrEqualTo(35);
        assertThat(address.getAddressLine4() + address.getAddressLine5()).isEqualTo("A".repeat(35) + ", Country");
        verifyNoInteractions(lookup);
    }

    @Test
    void preservesAddressWhenOverflowCannotFit() {
        RoboticsAddress address = address("EH12 9AB", "A".repeat(35)).setCountry("Scotland")
            .setAddressLine4("B".repeat(35)).setAddressLine5("C".repeat(35));
        mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address))), null);
        assertThat(address.getPostCode()).isEqualTo("EH12 9AB");
        assertThat(address.getAddressLine3()).isEqualTo("A".repeat(35));
    }

    @Test
    void preparesAndValidatesTheRewrittenJsonForMultipleDefendants() throws Exception {
        RoboticsAddress claimant = address("SW1A 1AA", null).setCountry("England");
        RoboticsAddress firstDefendant = address("08001", null).setCountry("Spain");
        RoboticsAddress secondDefendant = address("20121", "Milan").setCountry("Italy");
        RoboticsCaseDataSpec payload = new RoboticsCaseDataSpec().setLitigiousParties(
            java.util.stream.Stream.of(claimant, firstDefendant, secondDefendant)
                .map(value -> new LitigiousParty().setAddresses(wrap(value))).toList());

        String json = mapper.prepareJson(payload);

        verify(schema).validate(json);
        assertThat(claimant.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(firstDefendant.getPostCode()).isNull();
        assertThat(secondDefendant.getAddressLine3()).isEqualTo("Milan - 20121");
        assertThat(json).doesNotContain("country");
        verifyNoInteractions(lookup);
    }

    @Test
    void rejectsInvalidPreparedPayload() {
        when(schema.validate(org.mockito.ArgumentMatchers.anyString())).thenReturn(
            java.util.Set.of(com.networknt.schema.ValidationMessage.builder().message("Invalid").build()));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mapper.prepareJson(new RoboticsCaseData()))
            .isInstanceOf(uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException.class);
    }

    @Test
    void cachesPostcodeCountriesAcrossPayloads() {
        when(lookup.findCountryForPostcode("EH11AA")).thenReturn(java.util.Optional.of("Scotland"));
        for (int index = 0; index < 2; index++) {
            mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address("EH1 1AA", null)))), null);
        }
        verify(lookup).findCountryForPostcode("EH11AA");
    }

    private RoboticsAddress address(String postcode, String line3) {
        return new RoboticsAddress().setAddressLine1("10 High Street").setAddressLine2("Town")
            .setAddressLine3(line3).setAddressLine4("Country").setPostCode(postcode);
    }

    private RoboticsAddresses wrap(RoboticsAddress address) {
        return new RoboticsAddresses().setContactAddress(address);
    }
}
