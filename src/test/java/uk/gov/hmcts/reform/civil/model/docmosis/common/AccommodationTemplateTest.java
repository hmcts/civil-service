package uk.gov.hmcts.reform.civil.model.docmosis.common;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AccommodationTemplateTest {

    @Test
    void shouldDisplayRentWhenPrivateRental() {
        //Given
        AccommodationTemplate accommodationTemplate = new AccommodationTemplate(HomeDetails.builder().type(
            HomeTypeOptionLRspec.PRIVATE_RENTAL).build());
        String expectedResult = "Rent";
        //When
        String actualResult = accommodationTemplate.getDisplayValue();
        //Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void shouldDisplayOtherCustomAccommodationValueWhenOther() {
        //Given
        String expectedValue = "Mansion";
        AccommodationTemplate accommodationTemplate = new AccommodationTemplate(HomeDetails.builder().type(
            HomeTypeOptionLRspec.OTHER).typeOtherDetails(expectedValue).build());
        //When
        String actualResult = accommodationTemplate.getDisplayValue();
        //Then
        assertThat(actualResult).isEqualTo(expectedValue);
    }

    @Test
    void shouldDisplayEmptyStringWhenHomeDetailsIsNull() {
        //Given
        AccommodationTemplate accommodationTemplate = new AccommodationTemplate();
        //When
        String actualResult = accommodationTemplate.getDisplayValue();
        //Then
        assertThat(actualResult).isEqualTo("");
    }

}
