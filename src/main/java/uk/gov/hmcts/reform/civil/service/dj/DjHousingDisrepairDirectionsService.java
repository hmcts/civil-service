package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_A;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_B;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_D;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_DISREPAIR_CLAUSE_E;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_COLUMNS_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.HOUSING_SCHEDULE_INTRO_DJ;

@Service
@RequiredArgsConstructor
public class DjHousingDisrepairDirectionsService {

    private final DjDeadlineService deadlineService;

    public TrialHousingDisrepair buildTrialHousingDisrepair() {
        return new TrialHousingDisrepair()
            .setInput1(HOUSING_SCHEDULE_INTRO_DJ)
            .setInput2(HOUSING_SCHEDULE_COLUMNS_DJ)
            .setInput3(HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION)
            .setDate1(deadlineService.nextWorkingDayInWeeks(10))
            .setInput4(HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION)
            .setDate2(deadlineService.nextWorkingDayInWeeks(12));
    }

    public TrialHousingDisrepair buildTrialHousingDisrepairOtherRemedy() {
        return new TrialHousingDisrepair()
            .setClauseA(HOUSING_DISREPAIR_CLAUSE_A)
            .setClauseB(HOUSING_DISREPAIR_CLAUSE_B)
            .setFirstReportDateBy(deadlineService.nextWorkingDayInWeeks(4))
            .setClauseCBeforeDate(HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE)
            .setJointStatementDateBy(deadlineService.nextWorkingDayInWeeks(8))
            .setClauseCAfterDate(HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE)
            .setClauseD(HOUSING_DISREPAIR_CLAUSE_D)
            .setClauseE(HOUSING_DISREPAIR_CLAUSE_E);
    }
}

