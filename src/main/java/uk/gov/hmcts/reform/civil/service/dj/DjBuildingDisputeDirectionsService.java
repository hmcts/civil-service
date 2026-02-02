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
        return new TrialBuildingDispute()
            .setInput1(BUILDING_SCHEDULE_INTRO_DJ)
            .setInput2(BUILDING_SCHEDULE_COLUMNS_DJ)
            .setInput3(BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .setDate1(deadlineService.nextWorkingDayInWeeks(10))
            .setInput4(BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .setDate2(deadlineService.nextWorkingDayInWeeks(12));
    }

    public TrialHousingDisrepair buildTrialHousingDisrepair() {
        return new TrialHousingDisrepair()
            .setInput1(HOUSING_SCHEDULE_INTRO_DJ)
            .setInput2(HOUSING_SCHEDULE_COLUMNS_DJ)
            .setInput3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .setDate1(deadlineService.nextWorkingDayInWeeks(10))
            .setInput4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .setDate2(deadlineService.nextWorkingDayInWeeks(12));
    }
}
