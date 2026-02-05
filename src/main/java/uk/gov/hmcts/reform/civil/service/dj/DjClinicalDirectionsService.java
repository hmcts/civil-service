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
        return new TrialClinicalNegligence()
            .setInput1(CLINICAL_DOCUMENTS_HEADING)
            .setInput2(CLINICAL_PARTIES_DJ)
            .setInput3(CLINICAL_NOTES_DJ)
            .setInput4(CLINICAL_BUNDLE_DJ);
    }

    public TrialPersonalInjury buildTrialPersonalInjury() {
        return new TrialPersonalInjury()
            .setInput1(PERSONAL_INJURY_PERMISSION_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(4))
            .setInput2(PERSONAL_INJURY_QUESTIONS)
            .setDate2(deadlineService.nextWorkingDayInWeeks(8))
            .setInput3(PERSONAL_INJURY_ANSWERS)
            .setDate3(deadlineService.nextWorkingDayInWeeks(4))
            .setInput4(PERSONAL_INJURY_UPLOAD)
            .setDate4(deadlineService.nextWorkingDayInWeeks(8));
    }
}
