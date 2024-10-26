package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

@FeignClient(name = "location-ref-data-api", url = "${location.api.baseUrl}")
public interface LocationReferenceDataApiClient {

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getCourtVenueByName(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("court_venue_name") final String courtVenueName
    );

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getCourtVenueByEpimmsId(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("epimms_id") final String epimmsId
    );

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getCourtVenueByEpimmsIdAndType(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("epimms_id") final String epimmsId,
        @RequestParam("court_type_id") final String courtTypeId
    );

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getCourtVenueByLocationCode(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("is_case_management_location") final String isCaseManagementLocation,
        @RequestParam("court_type_id") final String courtTypeId,
        @RequestParam("court_location_code") final String courtLocationCode,
        @RequestParam("court_status") final String courtStatus
    );

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getHearingVenue(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("is_hearing_location") final String isHearingLocation,
        @RequestParam("court_type_id") final String courtTypeId,
        @RequestParam("location_type") final String locationType
    );

    @GetMapping(value = "/refdata/location/court-venues")
    List<LocationRefData> getCourtVenue(
        @RequestHeader("ServiceAuthorization") String serviceAuthorisation,
        @RequestHeader("Authorization") final String authorisation,
        @RequestParam("is_hearing_location") final String isHearingLocation,
        @RequestParam("is_case_management_location") final String isCaseManagementLocation,
        @RequestParam("court_type_id") final String courtTypeId,
        @RequestParam("location_type") final String locationType
    );
}
