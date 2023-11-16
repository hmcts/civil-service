package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsDataLoader;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AirlineEpimsService {

    private final AirlineEpimsDataLoader airlineEpimsDataLoader;

    public String getEpimsIdForAirline(String airline) {
        AirlineEpimsId airlineEpimsID = airlineEpimsDataLoader.getAirlineEpimsIDList()
            .stream().filter(item -> item.getAirline().equals(airline)).findFirst().orElse(null);

        return Optional.ofNullable(airlineEpimsID)
            .map(AirlineEpimsId::getEpimsID)
            .orElseThrow(() -> new IllegalStateException("Unable to find epims ID for airline: " + airline));
    }
}
