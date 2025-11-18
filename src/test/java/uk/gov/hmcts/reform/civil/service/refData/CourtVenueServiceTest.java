package uk.gov.hmcts.reform.civil.service.refData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CourtVenueServiceTest {

    private CourtVenueService courtVenueService;

    private LocationRefData court1;
    private LocationRefData court2;

    @BeforeEach
    void setup() {
        LocationReferenceDataApiClient apiClient = Mockito.mock(LocationReferenceDataApiClient.class);
        courtVenueService = new CourtVenueService(apiClient);

        court1 = LocationRefData.builder()
            .courtName("Central London")
            .epimmsId("EP123")
            .region("London")
            .regionId("R1")
            .locationType("Civil")
            .welshSiteName("Canol Llundain")
            .build();

        court2 = LocationRefData.builder()
            .courtName("Westminster")
            .epimmsId("EP456")
            .region("London")
            .regionId("R2")
            .locationType("Civil")
            .welshSiteName("Gorllewin")
            .build();

        when(apiClient.getAllCivilCourtVenues(anyString(), anyString(), anyString()))
            .thenReturn(List.of(court1, court2));
    }

    @Test
    void testGetAllCivilCourts() {
        List<LocationRefData> result = courtVenueService.getAllCivilCourts("serviceAuth", "auth");
        assertEquals(2, result.size());
        assertTrue(result.contains(court1));
        assertTrue(result.contains(court2));
    }

    @Test
    void testGetByCourtName() {
        LocationRefData result = courtVenueService.getByCourtName("serviceAuth", "auth", "Central London");
        assertNotNull(result);
        assertEquals("EP123", result.getEpimmsId());

        LocationRefData nullResult = courtVenueService.getByCourtName("serviceAuth", "auth", "Nonexistent");
        assertNull(nullResult);
    }

    @Test
    void testGetByEpimmsId() {
        List<LocationRefData> result = courtVenueService.getByEpimmsId("serviceAuth", "auth", "EP456");
        assertEquals(1, result.size());
        assertEquals("Westminster", result.get(0).getCourtName());
    }

    @Test
    void testGetByRegion() {
        List<LocationRefData> result = courtVenueService.getByRegion("serviceAuth", "auth", "London");
        assertEquals(2, result.size());
    }

    @Test
    void testGetByRegionId() {
        List<LocationRefData> result = courtVenueService.getByRegionId("serviceAuth", "auth", "R1");
        assertEquals(1, result.size());
        assertEquals("Central London", result.get(0).getCourtName());
    }

    @Test
    void testGetByLocationType() {
        List<LocationRefData> result = courtVenueService.getByLocationType("serviceAuth", "auth", "Civil");
        assertEquals(2, result.size());
    }

    @Test
    void testGetCourtByWelshSiteName() {
        List<LocationRefData> result = courtVenueService.getCourtByWelshSiteName("serviceAuth", "auth", "Canol Llundain");
        assertEquals(1, result.size());
        assertEquals("Central London", result.get(0).getCourtName());
    }
}
