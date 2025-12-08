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
            Address address = new Address();
            address.setAddressLine1("1234567890, 12345678901234567890, abcdefghijk");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890,1234567890abcdefghij,");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890,12345678909876543210");
            address.setAddressLine2("abcdefghijk");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890");
            address.setAddressLine2("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890123456789012345 , 1234567890");

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("12345678901234567890123456789012345");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("1234567890");
            assertThat(result).extracting("addressLine3")
                .isNull();
        }

        @Test
        void shouldSplitAndTrimExtraSpace_whenMutipleSpaceWithinText() {
            Address address = new Address();
            address.setAddressLine1("123456 6 78   91011 12131415 16171819");

            Address result = mapper.splitLongerLines(address);

            assertThat(result).extracting("addressLine1")
                .isEqualTo("123456 6 78 91011 12131415");
            assertThat(result).extracting("addressLine2")
                .isEqualTo("16171819, ");
            assertThat(result).extracting("addressLine3")
                .isNull();
        }

        @Test
        void shouldSplitAddressBySpace_Line2MissingButLine3Present() {
            Address address = new Address();
            address.setAddressLine1("21 Belgian Place");
            address.setAddressLine3("Gateshead");
            address.setPostTown("Newcastle upon Tyne");
            address.setPostCode("NW10 3PX");
            address.setCountry("United Kingdom");

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
            Address address = new Address();
            address.setAddressLine1("The aaaa bbbbbb and Aesthetics uuuuuu");
            address.setAddressLine2("10Z Stockwell Door");
            address.setPostTown("Edinburgh");
            address.setPostCode("NE23 8ZZ");
            address.setCountry("United Kingdom");

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
            Address address = new Address();
            address.setAddressLine1("ttttttttttt Group, ttttttttttt House, Unit 77, Nelson Court");
            address.setAddressLine3("Broadbridge");
            address.setPostTown("Manchester");
            address.setPostCode("NW12 3AC");
            address.setCountry("United Kingdom");

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
            Address address = new Address();
            address.setAddressLine1("vvvvv zzzzzzzz, 7th Floor, Rusmore Building, 70 Rusmore Circus");
            address.setPostTown("Birmingham");
            address.setPostCode("N32 3AX");
            address.setCountry("United Kingdom");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890");
            address.setAddressLine2("I am the second line");
            address.setAddressLine3("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo(address.getAddressLine2());
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLines_ExcessOccupiesAbsentAddLine4() {
            Address address = new Address();
            address.setAddressLine1("denhamdrivdenhamdrivdenhamdrivden, denhamdrivdenhamdrivdenhamdriv,");
            address.setAddressLine2("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd");
            address.setAddressLine3("drivdenhamdrivdenhamdrivdenh adsdsdsdsb");
            address.setCounty("drivdenhamdrivdenhamdrivdenh sdsdsdsdsd");
            address.setPostCode("wcwec2c33x ff3");

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
            Address address = new Address();
            address.setAddressLine1("Flat 3 Knighton court, second floor, 823-827 Cranbrook Road");
            address.setAddressLine2("Barkingside South");
            address.setAddressLine3("Barkingside South");
            address.setPostTown("Ilford");
            address.setCounty("Essex");
            address.setPostCode("IG11 6QW");
            address.setCountry("United Kingdom");

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo("Flat 3 Knighton court, second");
            assertThat(result.getAddressLine2()).isEqualTo("floor, 823-827 Cranbrook Road,");
            assertThat(result.getAddressLine3()).isEqualTo("Barkingside South, Barkingside");
            assertThat(result.getPostTown()).isEqualTo("South, Ilford");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingExcessOverflowIgnored() {
            Address address = new Address();
            address.setAddressLine1("Flat 3 Knighton court, second floor, 823-827 Cranbrook Road");
            address.setAddressLine2("Flat 4 Knighton court, second floor, 823-827 Cran");
            address.setAddressLine3("Flat 5 Knighton court, second floor, 823-827 Cran");
            address.setPostTown("Ilford");
            address.setCounty("Essex");
            address.setPostCode("IG11 6QW");
            address.setCountry("United Kingdom");

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

            Address address = new Address();
            address.setAddressLine1("12345678901234567890");
            address.setAddressLine2("I am the second line");
            address.setAddressLine3("abcdefghijk12345678901234567890 zxcvbnmzxcvbnm");
            address.setPostTown("there is something here");

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo(address.getAddressLine2());
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk12345678901234567890");
            assertThat(result.getPostTown()).isEqualTo("zxcvbnmzx, there is something here");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingSpreadsOverThreeLinesWithSpaces() {
            Address address = new Address();
            address.setAddressLine1("1234567890 1234567890");
            address.setAddressLine2("I am the second line");
            address.setAddressLine3("abcdefghijk 1234567890   1234567890 1234567899,zxcvbnmzxcvbnm 12345");

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2()).isEqualTo("I am the second line");
            assertThat(result.getAddressLine3()).isEqualTo("abcdefghijk 1234567890 1234567890");
            assertThat(result.getPostTown()).isEqualTo("1234567899,zxcvbnmzxcvbnm 12345, ");
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingLeavesIndividualLineExceedingLimit() {
            Address address = new Address();
            address.setAddressLine1("12345678901234567890");
            address.setAddressLine2("12345678901234567890abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");

            Address result = mapper.splitLongerLines(address);

            assertThat(result.getAddressLine1()).isEqualTo(address.getAddressLine1());
            assertThat(result.getAddressLine2())
                .isEqualTo("12345678901234567890abcdefghijk12345678901234567890,zxcvbnmzxcvbnm");
            assertThat(result.getAddressLine3()).isEqualTo(null);
        }

        @Test
        void shouldSplitAddressBySpace_whenSplittingBySpaceLeavesIndividualLineExceedingLimit() {
            Address address = new Address();
            address.setAddressLine1("12345678901234567890abcdefghijk 12345678901234567890, zxcvbnmzxcvbnm");
            address.setAddressLine2("123456789012345678901");

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
            Address address = new Address();
            address.setAddressLine1("12345678901234567890abcdefghijk12345678901234567890");

            Address result = mapper.splitLongerLines(address);

            assertThat(result).isEqualTo(address);
        }
    }
}
