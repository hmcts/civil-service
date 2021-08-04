package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.prd.model.ContactInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.assertion.CustomAssertions.assertThat;

class RoboticsAddressMapperTest {

    RoboticsAddressMapper mapper = new RoboticsAddressMapper();

    @Nested
    class RoboticsAddressMapping {

        @Test
        void shouldMapToRoboticsAddress_whenAddressIsProvided() {
            Address address = AddressBuilder.defaults().build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldThrowNullPointerException_whenAddressIsNullForToRoboticsAddress() {
            assertThrows(NullPointerException.class, () ->
                             mapper.toRoboticsAddress(null),
                         "address cannot be null"
            );
        }

        @Test
        void shouldMapToRoboticsAddressWithDefaultForAddressLine2_whenAddressLine2IsNull() {
            Address address = Address.builder()
                .addressLine1("address line 1")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldMapToRoboticsAddress_whenAllAddressLinesAreEqualTo35Characters() {
            Address address = AddressBuilder.maximal().build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldRollOverToAddressLine2_whenAddressLine1IsMoreThan35CharactersWithCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1, address line 2")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress)
                .isEqualTo(RoboticsAddress.builder()
                               .addressLine1("address line 1, address line 1")
                               .addressLine2("address line 2")
                               .postCode("SW1 1AA")
                               .build());
        }

        @Test
        void shouldRollOverToAddressLine3_whenAddressLine2IsMoreThan35CharactersWithCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1")
                .addressLine2("address line 2, address line 2, address line 3")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress)
                .isEqualTo(RoboticsAddress.builder()
                               .addressLine1("address line 1, address line 1")
                               .addressLine2("address line 2, address line 2")
                               .addressLine3("address line 3")
                               .postCode("SW1 1AA")
                               .build());
        }

        @Test
        void shouldRollOverBothAddressLines1And2_whenBothAddressLine1And2AreMoreThan35CharactersWithCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1, address line 2")
                .addressLine2("address line 2, address line 3")
                .addressLine3("address line 3")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress)
                .isEqualTo(RoboticsAddress.builder()
                               .addressLine1("address line 1, address line 1")
                               .addressLine2("address line 2, address line 2")
                               .addressLine3("address line 3, address line 3")
                               .postCode("SW1 1AA")
                               .build());
        }

        @Test
        void shouldMapToRoboticsAddress_whenAddressLine1IsMoreThan35CharactersWithoutCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1 address line 1 address line 1")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldMapToRoboticsAddress_whenAddressLine1IsMoreThan70CharactersWithCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1 address line 1, address line 1 address line 1 address line 1")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldMapToRoboticsAddress_whenAddressLine2IsMoreThan35CharactersWithoutCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1")
                .addressLine2("address line 2 address line 2 address line 2")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldMapToRoboticsAddress_whenAddressLine2IsMoreThan70CharactersWithCommas() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1")
                .addressLine2("address line 2 address line 2, address line 2 address line 2 address line 2")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }

        @Test
        void shouldMapToRoboticsAddress_whenAddressLine3IsMoreThan35Characters() {
            Address address = Address.builder()
                .addressLine1("address line 1, address line 1")
                .addressLine2("address line 2, address line 2")
                .addressLine3("address line 3, address line 3, address line 3")
                .postCode("SW1 1AA").build();

            RoboticsAddress roboticsAddress = mapper.toRoboticsAddress(address);

            assertThat(roboticsAddress).isEqualTo(address);
        }
    }

    @Nested
    class RoboticsAddressesMapping {

        @Test
        void shouldMapToRoboticsAddresses_whenAddressIsProvided() {
            Address address = AddressBuilder.defaults().build();

            RoboticsAddresses roboticsAddresses = mapper.toRoboticsAddresses(address);

            Assertions.assertThat(roboticsAddresses).isNotNull();
            assertThat(roboticsAddresses.getContactAddress()).isEqualTo(address);
        }

        @Test
        void shouldThrowNullPointerException_whenAddressIsNullForToRoboticsAddresses() {
            assertThrows(NullPointerException.class, () ->
                             mapper.toRoboticsAddresses((Address) null),
                         "address cannot be null"
            );
        }

        @Test
        void shouldMapToRoboticsAddresses_whenContactInformationIsProvided() {
            List<ContactInformation> contactInformationList = List.of(ContactInformation.builder()
                                                                          .addressLine1("line 1")
                                                                          .addressLine2("line 2")
                                                                          .postCode("AB1 2XY")
                                                                          .county("My county")
                                                                          .build());

            RoboticsAddresses roboticsAddresses = mapper.toRoboticsAddresses(contactInformationList);

            Assertions.assertThat(roboticsAddresses).isNotNull();
            assertThat(contactInformationList)
                .isEqualTo(roboticsAddresses.getContactAddress());
        }

        @Test
        void shouldMapToNull_whenContactInformationIsEmpty() {
            List<ContactInformation> contactInformationList = List.of();

            RoboticsAddresses roboticsAddresses = mapper.toRoboticsAddresses(contactInformationList);

            Assertions.assertThat(roboticsAddresses).isNull();
        }

        @Test
        void shouldMapToNull_whenContactInformationIsNull() {
            RoboticsAddresses roboticsAddresses = mapper.toRoboticsAddresses((List<ContactInformation>) null);

            Assertions.assertThat(roboticsAddresses).isNull();
        }
    }
}
