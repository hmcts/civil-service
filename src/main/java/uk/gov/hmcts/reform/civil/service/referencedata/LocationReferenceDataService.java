package uk.gov.hmcts.reform.civil.service.referencedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApi;
import uk.gov.hmcts.reform.civil.referencedata.LRDConfiguration;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.concat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationReferenceDataService {

    private final LocationReferenceDataApi locationReferenceDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public LocationRefData getCcmccLocation(String authToken) {
        try {
            List<LocationRefData> ccmccLocations =
                locationReferenceDataApi.getCourtVenueByName(
                    authTokenGenerator.generate(),
                    authToken,
                    "County Court Money Claims Centre"
                );
            if (ccmccLocations == null || ccmccLocations.isEmpty()) {
                log.warn("Location Reference Data Lookup did not return any CCMCC location");
                return LocationRefData.builder().build();
            } else {
                if (ccmccLocations.size() > 1) {
                    log.warn("Location Reference Data Lookup returned more than one CCMCC location");
                }
                return ccmccLocations.get(0);
            }
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return LocationRefData.builder().build();
    }

    public List<LocationRefData> getCourtLocationsForDefaultJudgments(String authToken) {
        try {
            return
                locationReferenceDataApi.getCourtVenue(
                    authTokenGenerator.generate(),
                    authToken,
                    "Y",
                    "Y",
                    "10",
                    "Court"
                );

        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsForGeneralApplication(String authToken) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApi.getCourtVenue(
                    authTokenGenerator.generate(),
                    authToken,
                    "Y",
                    "Y",
                    "10",
                    "Court"

                );
            return onlyEnglandAndWalesLocations(responseEntity)
                .stream().sorted(Comparator.comparing(LocationRefData::getSiteName)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsByEpimmsId(String authToken, String epimmsId) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApi.getCourtVenueByEpimmsId(
                    authTokenGenerator.generate(),
                    authToken, epimmsId
                );
            return responseEntity;
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCourtLocationsByEpimmsIdAndCourtType(String authToken, String epimmsId) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApi.getCourtVenueByEpimmsIdAndType(
                    authTokenGenerator.generate(),
                    authToken, epimmsId, "10"
                );
            return responseEntity;
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Returns the list of locations that can then be added in dynamic list on the
     * Judge Assisted order screen, SDO and Hearing Schedule Venue list.
     *
     * @param authToken BEARER_TOKEN from CallbackParams
     * @return List of Hearing court Locations / Venues
     */
    public List<LocationRefData> getHearingCourtLocations(String authToken) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApi.getHearingVenue(
                    authTokenGenerator.generate(),
                    authToken, "Y", "Y", "Court"
                );
            return responseEntity;
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private List<LocationRefData> onlyEnglandAndWalesLocations(List<LocationRefData> locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .collect(Collectors.toList());
    }

    public Optional<LocationRefData> getLocationMatchingLabel(String label, String bearerToken) {
        if (StringUtils.isBlank(label)) {
            return Optional.empty();
        }

        List<LocationRefData> locations = getHearingCourtLocations(bearerToken);
        return locations.stream().filter(loc -> LocationReferenceDataService.getDisplayEntry(loc)
                .equals(label))
            .findFirst();
    }

    /**
     * Label is siteName - courtAddress - postCode.
     *
     * @param location a location
     * @return string to serve as label
     */
    public static String getDisplayEntry(LocationRefData location) {
        return concat(
            concat(concat(location.getSiteName(), " - "), concat(location.getCourtAddress(), " - ")),
            location.getPostcode()
        );
    }

    public LocationRefData getCourtLocation(String authToken, String threeDigitCode) {
        try {
            List<LocationRefData> responseEntity =
                locationReferenceDataApi.getCourtVenueByLocationCode(
                    authTokenGenerator.generate(),
                    authToken, "Y", "10", threeDigitCode, "Open"
                );
            List<LocationRefData> locations = responseEntity;
            if (locations == null || locations.isEmpty()) {
                return LocationRefData.builder().build();
            } else {
                return filterCourtLocation(locations, threeDigitCode);

            }
        } catch (Exception e) {
            log.error("Location Reference Data Lookup Failed - " + e.getMessage(), e);
            throw e;
        }

    }

    private LocationRefData filterCourtLocation(List<LocationRefData> locations, String courtCode) {
        List<LocationRefData> filteredLocations = locations.stream().filter(location -> location.getCourtLocationCode()
                .equals(courtCode))
            .collect(Collectors.toList());
        if (filteredLocations.isEmpty()) {
            log.warn("No court Location Found for three digit court code : {}", courtCode);
            throw new LocationRefDataException("No court Location Found for three digit court code : " + courtCode);
        } else if (filteredLocations.size() > 1) {
            log.warn("More than one court location found : {}", courtCode);
            throw new LocationRefDataException("More than one court location found : " + courtCode);
        }

        return filteredLocations.get(0);

    }

}
