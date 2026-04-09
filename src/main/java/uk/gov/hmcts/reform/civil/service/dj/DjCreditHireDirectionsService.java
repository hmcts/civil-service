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
        SdoDJR2TrialCreditHireDetails creditHireDetails = new SdoDJR2TrialCreditHireDetails()
            .setInput2(CREDIT_HIRE_STATEMENT_PROMPT_DJ)
            .setInput3(CREDIT_HIRE_STATEMENT_DEADLINE_DJ)
            .setDate1(deadlineService.nextWorkingDayInWeeks(8))
            .setInput4(CREDIT_HIRE_NON_COMPLIANCE_DJ)
            .setDate2(deadlineService.nextWorkingDayInWeeks(10))
            .setInput5(CREDIT_HIRE_PARTIES_LIAISE);

        return new SdoDJR2TrialCreditHire()
            .setInput1(CREDIT_HIRE_DISCLOSURE_DJ)
            .setInput6(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ + " " + CREDIT_HIRE_DEFENDANT_UPLOAD_DJ)
            .setDate3(deadlineService.nextWorkingDayInWeeks(12))
            .setInput7(CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ)
            .setDate4(deadlineService.nextWorkingDayInWeeks(14))
            .setInput8(CREDIT_HIRE_WITNESS_LIMIT_DJ)
            .setDetailsShowToggle(CREDIT_HIRE_TOGGLE)
            .setSdoDJR2TrialCreditHireDetails(creditHireDetails);
    }
}
