package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD;

@Service
@RequiredArgsConstructor
public class DjClinicalDirectionsService {

    private final DjDeadlineService deadlineService;

    public TrialClinicalNegligence buildTrialClinicalNegligence() {
        return TrialClinicalNegligence.builder()
            .input1(CLINICAL_DOCUMENTS_HEADING)
            .input2(CLINICAL_PARTIES_DJ)
            .input3(CLINICAL_NOTES_DJ)
            .input4(CLINICAL_BUNDLE_DJ)
            .build();
    }

    public TrialPersonalInjury buildTrialPersonalInjury() {
        return TrialPersonalInjury.builder()
            .input1(PERSONAL_INJURY_PERMISSION_DJ)
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2(PERSONAL_INJURY_QUESTIONS)
            .date2(deadlineService.nextWorkingDayInWeeks(8))
            .input3(PERSONAL_INJURY_ANSWERS)
            .date3(deadlineService.nextWorkingDayInWeeks(4))
            .input4(PERSONAL_INJURY_UPLOAD)
            .date4(deadlineService.nextWorkingDayInWeeks(8))
            .build();
    }
}
