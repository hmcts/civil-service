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
    class ShouldReturnSpaceBasedSplitAddressLines {

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("I am the second line")
                .addressLine3("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo(address.getAddressLine2());
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines1() {
            Address address = Address.builder()
                .addressLine1("denhamdrivdenhamdrivdenhamdrivden, denhamdrivdenhamdrivdenhamdriv,")
                .addressLine2("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .addressLine3("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .postTown("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .county("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .postCode("wcwec2c33x ff3")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("denhamdrivdenhamdrivdenhamdrivden,");
            assertThat(result.getAddressLine2()).isEqualTo("denhamdrivdenhamdrivdenhamdriv,");
            assertThat(result.getAddressLine3()).isEqualTo("drivdenhamdrivdenhamdrivdenh");
            assertThat(result.getPostTown()).isEqualTo("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines2() {
            Address address = Address.builder()
                .addressLine1("Flat 3 Knighton court, second floor, 823-827 Cranbrook Road")
                .addressLine2("Barkingside South")
                .addressLine3("Barkingside South")
                .postTown("Ilford")
                .county("Essex")
                .postCode("IG11 6QW")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("Flat 3 Knighton court, second");
            assertThat(result.getAddressLine2()).isEqualTo("floor, 823-827 Cranbrook Road,");
            assertThat(result.getAddressLine3()).isEqualTo("Barkingside South, Barkingside");
            assertThat(result.getPostTown()).isEqualTo("South, Ilford");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines3() {
            Address address = Address.builder()
                .addressLine1("Flat 3 Knighton court, second floor, 823-827 Cranbrook Road")
                .addressLine2("Flat 4 Knighton court, second floor, 823-827 Cran")
                .addressLine3("Flat 5 Knighton court, second floor, 823-827 Cran")
                .postTown("Ilford")
                .county("Essex")
                .postCode("IG11 6QW")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("Flat 3 Knighton court, second");
            assertThat(result.getAddressLine2()).isEqualTo("floor, 823-827 Cranbrook Road,");
            assertThat(result.getAddressLine3()).isEqualTo("Flat 4 Knighton court, second");
            assertThat(result.getPostTown()).isEqualTo("Ilford");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverFourLines() {

            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("I am the second line")
                .addressLine3("abcdefghijk12345678901234567890 zxcvbnmzxcvbnm")
                .postTown("there is something here")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo(address.getAddressLine2());
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk12345678901234567890");
            assertThat(result.getPostTown()).isEqualTo("there is something here");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLinesWithSpaces() {
            Address address = Address.builder()
                .addressLine1("1234567890 1234567890")
                .addressLine2("I am the    second   line")
                .addressLine3("abcdefghijk 1234567890   1234567890 1234567899,zxcvbnmzxcvbnm 1234567890")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo("I am the second line");
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk 1234567890 1234567890");
            assertThat(result.getPostTown()).isEqualTo(null);
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingLeavesIndividualLineExceedingLimit() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890")
                .addressLine2("12345678901234567890abcdefghijk12345678901234567890,zxcvbnmzxcvbnm")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2())
                .isEqualTo("12345678901234567890abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");
            assertThat(result.getAddressLine3()).isEqualTo(null);
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingBySpaceLeavesIndividualLineExceedingLimit() {
            Address address = Address.builder()
                .addressLine1("12345678901234567890abcdefghijk 12345678901234567890, zxcvbnmzxcvbnm")
                .addressLine2("123456789012345678901")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890abcdefghijk");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("12345678901234567890,");
            assertThat(result).extracting("addressLine3")
                .isEqualTo("zxcvbnmzxcvbnm,");
            assertThat(result).extracting("postTown")
                .isEqualTo("123456789012345678901, ");
        }
    }

    @Nested
    class ShouldReturnOriginalAddressLines {

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
