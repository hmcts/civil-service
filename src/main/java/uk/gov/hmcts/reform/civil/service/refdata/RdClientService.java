package uk.gov.hmcts.reform.civil.service.refdata;

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

    @Cacheable(value = "courtVenueCache", key = "T(String).format('allLocations-%s', #serviceId)")
    public List<LocationRefData> fetchAllCivilCourtsByServiceId(String serviceAuth, String auth, String serviceId) {
        log.info("RdClientService Cache MISS → calling Location Reference Data API to fetch all courts for service id {}", serviceId);
        return locationRefDataApiClient.getAllCivilCourtVenuesByServiceId(serviceAuth, auth, CIVIL_COURT_TYPE_ID, LOCATION_TYPE, serviceId);
    }
}
