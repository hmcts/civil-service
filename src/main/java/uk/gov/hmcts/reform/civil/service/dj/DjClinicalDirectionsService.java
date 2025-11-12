package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_DJ;

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
            .input1("The claimant has permission to rely upon the written "
                        + "expert evidence already uploaded to the Digital"
                        + " Portal with the particulars of claim and in addition "
                        + "has permission to rely upon any associated "
                        + "correspondence or updating report which is uploaded "
                        + "to the Digital Portal by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .input2("Any questions which are to be addressed to an expert must "
                        + "be sent to the expert directly and"
                        + " uploaded to the Digital "
                        + "Portal by 4pm on")
            .date2(deadlineService.nextWorkingDayInWeeks(8))
            .input3("The answers to the questions shall be answered "
                        + "by the Expert by")
            .date3(deadlineService.nextWorkingDayInWeeks(4))
            .input4("and uploaded to the Digital Portal by")
            .date4(deadlineService.nextWorkingDayInWeeks(8))
            .build();
    }
}
