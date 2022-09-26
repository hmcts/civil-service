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

        @Test
        void shouldSplitAddressBySpace_Line2MissingButLine3Present() {
            Address address = Address.builder()
                .addressLine1("21 Belgian Place")
                .addressLine3("Gateshead")
                .postTown("Newcastle upon Tyne")
                .postCode("NW10 3PX")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("21 Belgian Place");
            assertThat(result.getAddressLine2()).isEqualTo("Gateshead");
            assertThat(result.getAddressLine3()).isEqualTo(null);
            assertThat(result.getPostTown()).isEqualTo("Newcastle upon Tyne");
            assertThat(result.getPostCode()).isEqualTo("NW10 3PX");
            assertThat(result.getCountry()).isEqualTo("United Kingdom");
        }
    }

    @Nested
    class ShouldReturnSpaceBasedSplitAddressLines {

        @Test
        void shouldSplitAddressBySpace_LongLine1Line3Missing() {
            Address address = Address.builder()
                .addressLine1("The aaaa bbbbbb and Aesthetics uuuuuu")
                .addressLine2("10Z Stockwell Door")
                .postTown("Edinburgh")
                .postCode("NE23 8ZZ")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("The aaaa bbbbbb and Aesthetics");
            assertThat(result.getAddressLine2()).isEqualTo("uuuuuu, 10Z Stockwell Door");
            assertThat(result.getAddressLine3()).isEqualTo(null);
            assertThat(result.getPostTown()).isEqualTo("Edinburgh");
            assertThat(result.getPostCode()).isEqualTo("NE23 8ZZ");
            assertThat(result.getCountry()).isEqualTo("United Kingdom");
        }

        @Test
        void shouldSplitAddressBySpace_LongLine1Line2MissingLine3Present() {
            Address address = Address.builder()
                .addressLine1("ttttttttttt Group, ttttttttttt House, Unit 77, Nelson Court")
                .addressLine3("Broadbridge")
                .postTown("Manchester")
                .postCode("NW12 3AC")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("ttttttttttt Group, ttttttttttt");
            assertThat(result.getAddressLine2()).isEqualTo("House, Unit 77, Nelson Court, ");
            assertThat(result.getAddressLine3()).isEqualTo("Broadbridge");
            assertThat(result.getPostTown()).isEqualTo("Manchester");
            assertThat(result.getPostCode()).isEqualTo("NW12 3AC");
            assertThat(result.getCountry()).isEqualTo("United Kingdom");
        }

        @Test
        void shouldSplitAddressBySpace_LongLine1Line2And3Missing() {
            Address address = Address.builder()
                .addressLine1("vvvvv zzzzzzzz, 7th Floor, Rusmore Building, 70 Rusmore Circus")
                .postTown("Birmingham")
                .postCode("N32 3AX")
                .country("United Kingdom")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("vvvvv zzzzzzzz, 7th Floor, Rusmore");
            assertThat(result.getAddressLine2()).isEqualTo("Building, 70 Rusmore Circus, ");
            assertThat(result.getAddressLine3()).isEqualTo(null);
            assertThat(result.getPostTown()).isEqualTo("Birmingham");
            assertThat(result.getPostCode()).isEqualTo("N32 3AX");
            assertThat(result.getCountry()).isEqualTo("United Kingdom");
        }

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
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines_ExcessOccupiesAbsentAddLine4() {
            Address address = Address.builder()
                .addressLine1("denhamdrivdenhamdrivdenhamdrivden, denhamdrivdenhamdrivdenhamdriv,")
                .addressLine2("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .addressLine3("drivdenhamdrivdenhamdrivdenh adsdsdsdsb")
                .county("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd")
                .postCode("wcwec2c33x ff3")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("denhamdrivdenhamdrivdenhamdrivden,");
            assertThat(result.getAddressLine2()).isEqualTo("denhamdrivdenhamdrivdenhamdriv,");
            assertThat(result.getAddressLine3()).isEqualTo("drivdenhamdrivdenhamdrivdenh");
            assertThat(result.getPostTown()).isEqualTo("sdsdsdsdsd, drivdenhamdrivdenham, ");
            assertThat(result.getCounty()).isEqualTo("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd");
            assertThat(result.getPostCode()).isEqualTo("wcwec2c33x ff3");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines_OverflowInLimit() {
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
        void shouldSplitAddressBySpace_whenSplittingExcessOverflowIgnored() {
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
            assertThat(result.getPostTown()).isEqualTo("floor, 823-827 Cran, Flat , Ilford");
            assertThat(result.getCounty()).isEqualTo("Essex");
            assertThat(result.getCountry()).isEqualTo("United Kingdom");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverFourLines_ExcessOverflowIgnore() {

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
            assertThat(result.getPostTown()).isEqualTo("zxcvbnmzx, there is something here");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLinesWithSpaces() {
            Address address = Address.builder()
                .addressLine1("1234567890 1234567890")
                .addressLine2("I am the    second   line")
                .addressLine3("abcdefghijk 1234567890   1234567890 1234567899,zxcvbnmzxcvbnm 12345")
                .build();

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo("I am the second line");
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk 1234567890 1234567890");
            assertThat(result.getPostTown()).isEqualTo("1234567899,zxcvbnmzxcvbnm 12345, ");
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
