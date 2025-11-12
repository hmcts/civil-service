package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.BUILDING_SCHEDULE_INTRO_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_DJ;

@Service
@RequiredArgsConstructor
public class DjBuildingDisputeDirectionsService {

    private final DjDeadlineService deadlineService;

    public TrialBuildingDispute buildTrialBuildingDispute() {
        return TrialBuildingDispute.builder()
            .input1(BUILDING_SCHEDULE_INTRO_DJ)
            .input2(BUILDING_SCHEDULE_COLUMNS_DJ)
            .input3(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input4(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .build();
    }

    public TrialHousingDisrepair buildTrialHousingDisrepair() {
        return TrialHousingDisrepair.builder()
            .input1(HOUSING_SCHEDULE_INTRO_DJ)
            .input2(HOUSING_SCHEDULE_COLUMNS_DJ)
            .input3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .build();
    }
}
