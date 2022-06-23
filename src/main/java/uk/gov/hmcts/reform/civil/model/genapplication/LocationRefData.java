package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class LocationRefData {

    private String courtVenueId;
    private String epimmsId;
    private String siteName;
    private String regionId;
    private String region;
    private String courtType;
    private String courtTypeId;
    private String courtAddress;
    private String postcode;
    private String phoneNumber;
    private String courtLocationCode;
    private String courtStatus;
    private String courtName;
    private String venueName;
    private String locationType;
    private String parentLocation;

    @JsonCreator
    LocationRefData(@JsonProperty("court_venue_id") String courtVenueId,
                    @JsonProperty("epimms_id") String epimmsId,
                    @JsonProperty("site_name") String siteName,
                    @JsonProperty("region_id") String regionId,
                    @JsonProperty("region") String region,
                    @JsonProperty("court_type") String courtType,
                    @JsonProperty("court_type_id") String courtTypeId,
                    @JsonProperty("court_address") String courtAddress,
                    @JsonProperty("postcode") String postcode,
                    @JsonProperty("phone_number") String phoneNumber,
                    @JsonProperty("court_location_code") String courtLocationCode,
                    @JsonProperty("court_status") String courtStatus,
                    @JsonProperty("court_name") String courtName,
                    @JsonProperty("venue_name") String venueName,
                    @JsonProperty("location_type") String locationType,
                    @JsonProperty("parent_location") String parentLocation) {
        this.courtVenueId = courtVenueId;
        this.epimmsId = epimmsId;
        this.siteName = siteName;
        this.regionId = regionId;
        this.region = region;
        this.courtType = courtType;
        this.courtTypeId = courtTypeId;
        this.courtAddress = courtAddress;
        this.postcode = postcode;
        this.phoneNumber = phoneNumber;
        this.courtLocationCode = courtLocationCode;
        this.courtStatus = courtStatus;
        this.courtName = courtName;
        this.venueName = venueName;
        this.locationType = locationType;
        this.parentLocation = parentLocation;
    }
}
