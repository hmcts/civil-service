package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.refdata.CourtVenueService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppLocationRefDataService {

    public static final String CIVIL_NATIONAL_BUSINESS_CENTRE = "Civil National Business Centre";
    public static final String COUNTY_COURT_MONEY_CLAIMS_CENTRE = "County Court Money Claims Centre";
    private final CourtVenueService courtVenueService;
    private final AuthTokenGenerator authTokenGenerator;
    private static final String DATA_LOOKUP_FAILED = "Location Reference Data Lookup Failed - ";

    public List<LocationRefData> getCourtLocations(String authToken) {
        try {
            List<LocationRefData> responseEntity =
                courtVenueService.getCMLAndHLCourts(
                    authTokenGenerator.generate(),
                    authToken);
            return onlyEnglandAndWalesLocations(responseEntity)
                .stream().sorted(Comparator.comparing(LocationRefData::getSiteName)).toList();
        } catch (Exception e) {
            log.error(DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<LocationRefData> getCcmccLocation(String authToken) {
        List<LocationRefData> ccmccLocations = null;
        try {
            ccmccLocations = courtVenueService.getCourtVenueByName(
                authTokenGenerator.generate(),
                authToken,
                COUNTY_COURT_MONEY_CLAIMS_CENTRE
            );
        } catch (Exception e) {
            log.error(DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return ccmccLocations;
    }

    public List<LocationRefData> getCnbcLocation(String authToken) {
        List<LocationRefData> cnbcLocations = null;
        try {
            cnbcLocations = courtVenueService.getCourtVenueByName(
                authTokenGenerator.generate(),
                authToken,
                CIVIL_NATIONAL_BUSINESS_CENTRE
            );
        } catch (Exception e) {
            log.error(DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return cnbcLocations;
    }

    public List<LocationRefData> getCourtLocationsByEpimmsId(String authToken, String epimmsId) {
        try {
            return courtVenueService.getCourtByEpimmsId(
                    authTokenGenerator.generate(),
                    authToken, epimmsId
                );
        } catch (Exception e) {
            log.error(DATA_LOOKUP_FAILED + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private List<LocationRefData> onlyEnglandAndWalesLocations(List<LocationRefData> locationRefData) {
        return locationRefData == null
            ? new ArrayList<>()
            : locationRefData.stream().filter(location -> !"Scotland".equals(location.getRegion()))
            .toList();
    }
}
