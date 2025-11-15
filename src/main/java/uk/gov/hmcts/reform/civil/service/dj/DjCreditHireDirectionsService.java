package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHireDetails;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DEFENDANT_UPLOAD_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DISCLOSURE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_NON_COMPLIANCE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_PARTIES_LIAISE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_DEADLINE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_PROMPT_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_WITNESS_LIMIT_DJ;

@Service
@RequiredArgsConstructor
public class DjCreditHireDirectionsService {

    private static final List<AddOrRemoveToggle> CREDIT_HIRE_TOGGLE = List.of(AddOrRemoveToggle.ADD);

    private final DjDeadlineService deadlineService;

    public SdoDJR2TrialCreditHire buildCreditHireDirections() {
        SdoDJR2TrialCreditHireDetails creditHireDetails = SdoDJR2TrialCreditHireDetails.builder()
            .input2(CREDIT_HIRE_STATEMENT_PROMPT_DJ)
            .input3(CREDIT_HIRE_STATEMENT_DEADLINE_DJ)
            .date1(deadlineService.nextWorkingDayInWeeks(8))
            .input4(CREDIT_HIRE_NON_COMPLIANCE_DJ)
            .date2(deadlineService.nextWorkingDayInWeeks(10))
            .input5(CREDIT_HIRE_PARTIES_LIAISE)
            .build();

        return SdoDJR2TrialCreditHire.builder()
            .input1(CREDIT_HIRE_DISCLOSURE_DJ)
            .input6(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ + " " + CREDIT_HIRE_DEFENDANT_UPLOAD_DJ)
            .date3(deadlineService.nextWorkingDayInWeeks(12))
            .input7(CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ)
            .date4(deadlineService.nextWorkingDayInWeeks(14))
            .input8(CREDIT_HIRE_WITNESS_LIMIT_DJ)
            .detailsShowToggle(CREDIT_HIRE_TOGGLE)
            .sdoDJR2TrialCreditHireDetails(creditHireDetails)
            .build();
    }
}
