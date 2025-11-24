package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;

import java.util.List;

@FeignClient(name = "location-ref-data-api", url = "${location.api.baseUrl}")
public interface LocationReferenceDataApiClient {

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getAllCivilCourtVenues(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("court_type_id") final String courtTypeId,
        @RequestParam("location_type") final String locationType
    );
}
