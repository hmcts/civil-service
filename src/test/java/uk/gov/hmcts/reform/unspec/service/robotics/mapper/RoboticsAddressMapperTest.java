package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.unspec.sampledata.AddressBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.unspec.assertion.CustomAssertions.assertThat;

class RoboticsAddressMapperTest {

    RoboticsAddressMapper mapper = new RoboticsAddressMapper();

    @Nested
    class RoboticsAddressMapping {

        @Test
        void shouldMapToRoboticsAddress_whenAddressIsProvided() {
            Address address = AddressBuilder.builder().build();

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
    }

    @Nested
    class RoboticsAddressesMapping {

        @Test
        void shouldMapToRoboticsAddresses_whenAddressIsProvided() {
            Address address = AddressBuilder.builder().build();

            RoboticsAddresses roboticsAddresses = mapper.toRoboticsAddresses(address);

            Assertions.assertThat(roboticsAddresses).isNotNull();
            assertThat(roboticsAddresses.getContactAddress()).isEqualTo(address);
        }

        @Test
        void shouldThrowNullPointerException_whenAddressIsNullForToRoboticsAddresses() {
            assertThrows(NullPointerException.class, () ->
                             mapper.toRoboticsAddresses(null),
                         "address cannot be null"
            );
        }
    }
}
