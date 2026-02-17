package uk.gov.hmcts.reform.civil.service.refdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

@Service
@Slf4j
public class RdClientService {

    private final LocationReferenceDataApiClient locationRefDataApiClient;

    private static final String CIVIL_COURT_TYPE_ID = "10";
    private static final String LOCATION_TYPE = "Court";

    public RdClientService(LocationReferenceDataApiClient locationRefDataApiClient) {
        this.locationRefDataApiClient = locationRefDataApiClient;
    }

    @Cacheable(value = "courtVenueCache", key = "'allLocations'")
    public List<LocationRefData> fetchAllCivilCourts(String serviceAuth, String auth) {
        log.info("[CourtVenueService] Cache MISS → calling Location Reference Data API to fetch all courts");
        List<LocationRefData> locations = locationRefDataApiClient.getAllCivilCourtVenues(serviceAuth, auth, CIVIL_COURT_TYPE_ID, LOCATION_TYPE);
        try {
            log.info("[CourtVenueService] Locations returned: {}", new ObjectMapper().writeValueAsString(locations));
        } catch (JsonProcessingException e) {
            log.warn("[CourtVenueService] Failed to serialize locations to JSON", e);
        }
        return locations;
    }
}
