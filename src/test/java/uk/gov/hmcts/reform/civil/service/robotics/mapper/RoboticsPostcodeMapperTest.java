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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RoboticsPostcodeMapperTest {

    private final PostcodeLookupService lookup = mock(PostcodeLookupService.class);
    private final RoboticsPostcodeMapper mapper = new RoboticsPostcodeMapper(lookup);

    @ParameterizedTest
    @ValueSource(strings = {"EH1 1AA", "BT1 5GS", "08001", "20121"})
    void movesOutsidePostcodesInBothPayloadTypes(String postcode) throws Exception {
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
        verify(lookup).validatePostCodeForDefendant(postcode.replace(" ", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SW1A 1AA", "CF10 1EP", " sw1a 1aa "})
    void preservesEnglandAndWalesAddresses(String postcode) {
        when(lookup.validatePostCodeForDefendant(postcode.replaceAll("\\s", "").toUpperCase(java.util.Locale.UK)))
            .thenReturn(true);
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

    @Test
    void doesNotTreatLookupFailureAsOutsideEnglandAndWales() {
        when(lookup.validatePostCodeForDefendant("SW1A1AA")).thenThrow(new RuntimeException("Lookup unavailable"));
        RoboticsAddress address = address("SW1A 1AA", "Existing line");

        assertThatThrownBy(() -> mapper.mapPostcodes(List.of(new LitigiousParty().setAddresses(wrap(address))), null))
            .hasMessage("Lookup unavailable");
        assertThat(address.getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(address.getAddressLine3()).isEqualTo("Existing line");
    }

    private RoboticsAddress address(String postcode, String line3) {
        return new RoboticsAddress().setAddressLine1("10 High Street").setAddressLine2("Town")
            .setAddressLine3(line3).setAddressLine4("Country").setPostCode(postcode);
    }

    private RoboticsAddresses wrap(RoboticsAddress address) {
        return new RoboticsAddresses().setContactAddress(address);
    }
}
