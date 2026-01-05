package uk.gov.hmcts.reform.civil.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Component
public class CourtLocationUtils {

    public DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream()
                            .map(location -> location.getSiteName()
                                + " - " + location.getCourtAddress()
                                + " - " + location.getPostcode())
                            .sorted()
                            .collect(Collectors.toList()));
    }

    public LocationRefData findPreferredLocationData(final List<LocationRefData> locations,
                                                            DynamicList data) {
        if (Objects.isNull(data) || Objects.isNull(data.getValue()) || Objects.isNull(locations)) {
            return null;
        }
        String locationLabel = data.getValue().getLabel();
        var preferredLocation =
            locations.stream()
                .filter(locationRefData -> checkLocation(locationRefData, locationLabel))
                .findFirst();
        return preferredLocation.orElse(null);
    }

    public Boolean checkLocation(final LocationRefData location, String locationTempLabel) {
        String locationLabel = location.getSiteName()
            + " - " + location.getCourtAddress()
            + " - " + location.getPostcode();
        return locationLabel.equals(locationTempLabel);
    }
}
