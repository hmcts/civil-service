package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourtLocationUtilsTest {

    private CourtLocationUtils courtLocationUtils;
    List<LocationRefData> locations = new ArrayList<>();
    LocationRefData locationA;
    DynamicList locationList;

    @BeforeEach
    void setup() {
        courtLocationUtils = new CourtLocationUtils();
        locationA = LocationRefData.builder().siteName("Site 1").courtAddress("Lane 1").postcode("123").build();
        locations.add(locationA);
        locations.add(LocationRefData.builder().siteName("Site 2").courtAddress("Lane 2").postcode("124").build());
        locationList = courtLocationUtils.getLocationsFromList(locations);
    }

    @Test
    void shouldReturnDynamicListWithLocations() {
        assertThat(locationList.getListItems().get(0).getLabel()).isEqualTo("Site 1 - Lane 1 - 123");
        assertThat(locationList.getListItems().get(1).getLabel()).isEqualTo("Site 2 - Lane 2 - 124");
        assertThat(locationList.getListItems().size()).isEqualTo(2);
    }

    @Test
    void shouldFindPreferredLocationData_whenLocationDataMatches() {
        locationList.setValue(DynamicListElement.builder()
                                  .code(locationList.getListItems().get(0).getCode())
                                  .label(locationList.getListItems().get(0).getLabel())
                                  .build());

        assertThat(courtLocationUtils.findPreferredLocationData(locations, locationList)).isEqualTo(locationA);
    }

    @Test
    void shouldReturnNull_WhenLocationNotFound() {
        assertThat(courtLocationUtils.findPreferredLocationData(locations, locationList)).isEqualTo(null);
    }

    @Test
    void shouldReturnNull_WhenNoLocationsProvided() {
        locationList.setValue(DynamicListElement.builder()
                                  .code(locationList.getListItems().get(0).getCode())
                                  .label(locationList.getListItems().get(0).getLabel())
                                  .build());

        assertThat(courtLocationUtils.findPreferredLocationData(null, locationList)).isEqualTo(null);
    }
}
