package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

@Service
@RequiredArgsConstructor
public class DjRoadTrafficAccidentDirectionsService {

    private final DjDeadlineService deadlineService;

    public TrialRoadTrafficAccident buildTrialRoadTrafficAccident() {
        return TrialRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location "
                       + "shall be prepared "
                       + "and agreed by the parties and uploaded to the"
                       + " Digital Portal by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .build();
    }
}
