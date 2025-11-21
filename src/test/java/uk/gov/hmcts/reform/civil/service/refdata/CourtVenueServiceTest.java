package uk.gov.hmcts.reform.civil.service.refdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourtVenueServiceTest {

    @Mock
    private LocationReferenceDataApiClient locationRefDataApiClient;

    @InjectMocks
    private CourtVenueService courtVenueService;

    private final String serviceAuth = "serviceAuth";
    private final String auth = "auth";

    private LocationRefData court1;
    private LocationRefData court2;
    private LocationRefData court3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        court1 = LocationRefData.builder()
            .epimmsId("111")
            .courtName("London Court")
            .region("South")
            .regionId("10")
            .locationType("Court")
            .courtLocationCode("AAA")
            .courtStatus("Open")
            .isCaseManagementLocation("Y")
            .isHearingLocation("Y")
            .welshSiteName("Llys")
            .build();

        court2 = LocationRefData.builder()
            .epimmsId("222")
            .courtName("Bristol Court")
            .region("West")
            .regionId("20")
            .locationType("Court")
            .courtLocationCode("BBB")
            .courtStatus("Closed")
            .isCaseManagementLocation("N")
            .isHearingLocation("Y")
            .welshSiteName("None")
            .build();

        court3 = LocationRefData.builder()
            .epimmsId("333")
            .courtName("London Court Annex")
            .region("South")
            .regionId("10")
            .locationType("Tribunal")
            .courtLocationCode("CCC")
            .courtStatus("Open")
            .isCaseManagementLocation("Y")
            .isHearingLocation("N")
            .welshSiteName("Llys2")
            .build();

        when(locationRefDataApiClient.getAllCivilCourtVenues(any(), any(), any(), any()))
            .thenReturn(List.of(court1, court2, court3));
    }

    @Test
    void shouldReturnAllCivilCourts() {
        List<LocationRefData> result = courtVenueService.getAllCivilCourts(serviceAuth, auth);
        assertThat(result).containsExactlyInAnyOrder(court1, court2, court3);

        verify(locationRefDataApiClient, times(1))
            .getAllCivilCourtVenues(serviceAuth, auth, "10", "Court");
    }

    @Test
    void shouldFilterByEpimmsId() {
        List<LocationRefData> result = courtVenueService.getCourtByEpimmsId(serviceAuth, auth, "111");
        assertThat(result).containsExactly(court1);
    }

    @Test
    void shouldFilterCMLByEpimmsId() {
        List<LocationRefData> result = courtVenueService.getCMLCourtByEpimmsId(serviceAuth, auth, "111");
        assertThat(result).containsExactly(court1); // court3 has CML=Y but hearing=N
    }

    @Test
    void shouldFilterByCourtName() {
        List<LocationRefData> result = courtVenueService.getCourtVenueByName(serviceAuth, auth, "London Court");
        assertThat(result).containsExactly(court1);
    }

    @Test
    void shouldFilterByRegion() {
        List<LocationRefData> result = courtVenueService.getByRegion(serviceAuth, auth, "South");
        assertThat(result).containsExactlyInAnyOrder(court1, court3);
    }

    @Test
    void shouldFilterByRegionId() {
        List<LocationRefData> result = courtVenueService.getByRegionId(serviceAuth, auth, "10");
        assertThat(result).containsExactlyInAnyOrder(court1, court3);
    }

    @Test
    void shouldFilterByLocationType() {
        List<LocationRefData> result = courtVenueService.getByLocationType(serviceAuth, auth, "Court");
        assertThat(result).containsExactlyInAnyOrder(court1, court2);
    }

    @Test
    void shouldFilterByLocationCode() {
        List<LocationRefData> result = courtVenueService.getCourtVenueByLocationCode(serviceAuth, auth, "AAA");
        assertThat(result).containsExactly(court1); // must be Open + CML=Y
    }

    @Test
    void shouldFilterByWelshSiteName() {
        List<LocationRefData> result = courtVenueService.getCourtByWelshSiteName(serviceAuth, auth, "Llys");
        assertThat(result).containsExactly(court1);
    }

    @Test
    void shouldReturnHearingLocationCourts() {
        List<LocationRefData> result = courtVenueService.getHearingLocationCourts(serviceAuth, auth);
        assertThat(result).containsExactlyInAnyOrder(court1, court2);
    }

    @Test
    void shouldReturnCMLAndHLCourts() {
        List<LocationRefData> result = courtVenueService.getCMLAndHLCourts(serviceAuth, auth);
        assertThat(result).containsExactly(court1);
    }
}
