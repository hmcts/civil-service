package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class AddressLinesMapperTest {

    AddressLinesMapper mapper = new AddressLinesMapper();

    @Nested
    class ShouldSplitAddressLines {

        @Test
        void shouldSplitAddressLine1_whenExceedsLimitAndContainsComma() {
            Address address = Address.builder()
                .addressLine1("1234567890, 12345678901234567890, abcdefghijk")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("1234567890, 12345678901234567890");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("abcdefghijk");
            assertThat(result).extracting("addressLine3")
                .isNull();
        }

        @Test
        void shouldSplitAddressLine1_whenExceedsLimitAndContainsCommaAsLastChar() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890,1234567890abcdefghij,")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("1234567890abcdefghij");
            assertThat(result).extracting("addressLine3")
                .isNull();
        }

        @Test
        void shouldSplitAddressLine1AndPushDown_whenExceedsLimitAndContainsComma() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890,12345678909876543210")
                .addressLine2("abcdefghijk")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("12345678909876543210");
            assertThat(result).extracting("addressLine3")
                .isEqualTo("abcdefghijk");
        }

        @Test
        void shouldSplitAddressLine2_whenExceedsLimitAndContainsComma() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("abcdefghijk12345678901234567890");
            assertThat(result).extracting("addressLine3")
                .isEqualTo("zxcvbnmzxcvbnm");
        }

        @Test
        void shouldSplitAndTrimExtraSpace_whenSpaceAroundComma() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890123456789012345 , 1234567890")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890123456789012345");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("1234567890");
            assertThat(result).extracting("addressLine3")
                .isNull();
        }
    }

    @Nested
    class ShouldReturnOriginalAddress {

        @Test
        void shouldReturnOriginalAddress_whenSplittingSpreadsOverThreeLines() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("I am the second line")
                .addressLine3("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).isEqualTo(address);
        }

        @Test
        void shouldReturnOriginalAddress_whenSplittingLeavesIndividualLineExceedingLimit() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("12345678901234567890abcdefghijk12345678901234567890,zxcvbnmzxcvbnm")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).isEqualTo(address);
        }

        @Test
        void shouldReturnOriginalAddress_whenNoNeedToSplit() {
            Address address = AddressBuilder.maximal().build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).isEqualTo(address);
        }

        @Test
        void shouldReturnOriginalAddress_whenLineLongerThanLimitButNoComma() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890abcdefghijk12345678901234567890")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).isEqualTo(address);
        }
    }
}
