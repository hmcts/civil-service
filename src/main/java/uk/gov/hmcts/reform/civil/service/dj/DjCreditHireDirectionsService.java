package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHireDetails;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DjCreditHireDirectionsService {

    private static final List<AddOrRemoveToggle> CREDIT_HIRE_TOGGLE = List.of(AddOrRemoveToggle.ADD);

    private final DjDeadlineService deadlineService;

    public SdoDJR2TrialCreditHire buildCreditHireDirections() {
        SdoDJR2TrialCreditHireDetails creditHireDetails = SdoDJR2TrialCreditHireDetails.builder()
            .input2("The claimant must upload to the Digital Portal a witness "
                        + "statement addressing \na) the need to hire a replacement "
                        + "vehicle; and \nb) impecuniosity")
            .input3("This statement must be uploaded to the Digital Portal by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(8))
            .input4("A failure to comply will result in the claimant being "
                        + "debarred from asserting need or relying on impecuniosity "
                        + "as the case may be at the final hearing, unless they "
                        + "have the permission of the trial Judge.")
            .date2(deadlineService.nextWorkingDayInWeeks(10))
            .input5("The parties are to liaise and use reasonable endeavours to"
                        + " agree the basic hire rate no "
                        + "later than 4pm on")
            .build();

        return SdoDJR2TrialCreditHire.builder()
            .input1(
                "If impecuniosity is alleged by the claimant and not admitted "
                    + "by the defendant, the claimant's "
                    + "disclosure as ordered earlier in this order must "
                    + "include:\n"
                    + "a. Evidence of all income from all sources for a period "
                    + "of 3 months prior to the "
                    + "commencement of hire until the earlier of \n    i) 3 months "
                    + "after cessation of hire or \n    ii) "
                    + "the repair or replacement of the claimant's vehicle;\n"
                    + "b. Copy statements of all bank, credit card and savings "
                    + "account statements for a period of 3 months "
                    + "prior to the commencement of hire until"
                    + " the earlier of \n    i)"
                    + " 3 months after cessation of hire "
                    + "or \n    ii) the repair or replacement of the "
                    + "claimant's vehicle;\n"
                    + "c. Evidence of any loan, overdraft or other credit "
                    + "facilities available to the claimant")
            .input6(
                "If the parties fail to agree rates subject to liability "
                    + "and/or other issues pursuant to the paragraph above, "
                    + "each party may rely upon the written evidence by way of"
                    + " witness statement of one witness to provide evidence of "
                    + "basic hire rates available within the claimant’s "
                    + "geographical"
                    + " location from a mainstream supplier, or a local reputable "
                    + "supplier if none is available. The defendant’s evidence is "
                    + "to be uploaded to the Digital Portal by 4pm on")
            .date3(deadlineService.nextWorkingDayInWeeks(12))
            .input7("and the claimant’s evidence in reply if "
                        + "so advised is to be uploaded by 4pm on")
            .date4(deadlineService.nextWorkingDayInWeeks(14))
            .input8(
                "This witness statement is limited to 10 pages per party "
                    + "(to include any appendices).")
            .detailsShowToggle(CREDIT_HIRE_TOGGLE)
            .sdoDJR2TrialCreditHireDetails(creditHireDetails)
            .build();
    }
}
