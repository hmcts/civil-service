package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ROAD_TRAFFIC_ACCIDENT_UPLOAD_DJ;

@Service
@RequiredArgsConstructor
public class DjRoadTrafficAccidentDirectionsService {

    private final DjDeadlineService deadlineService;

    public TrialRoadTrafficAccident buildTrialRoadTrafficAccident() {
        return new TrialRoadTrafficAccident()
            .setInput(ROAD_TRAFFIC_ACCIDENT_UPLOAD_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(4));
    }
}
