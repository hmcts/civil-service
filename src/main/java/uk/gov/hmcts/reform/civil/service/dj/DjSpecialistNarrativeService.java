package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DjSpecialistNarrativeService {

    private static final List<AddOrRemoveToggle> CREDIT_HIRE_TOGGLE = List.of(AddOrRemoveToggle.ADD);

    private final DjSpecialistDeadlineService deadlineService;

    public TrialBuildingDispute buildTrialBuildingDispute() {
        return TrialBuildingDispute.builder()
            .input1("The claimant must prepare a Scott Schedule of the defects,"
                        + " items of damage "
                        + "or any other relevant matters")
            .input2("The columns should be headed: \n - Item \n - "
                        + "Alleged Defect "
                        + "\n - Claimant's costing\n - Defendant's"
                        + " response\n - Defendant's costing"
                        + " \n - Reserved for Judge's use")
            .input3("The claimant must upload to the Digital Portal the "
                        + "Scott Schedule with the relevant "
                        + "columns completed by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input4("The defendant must upload to the Digital Portal "
                        + "an amended version of the Scott Schedule with the relevant"
                        + " columns in response completed by 4pm on")
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .build();
    }

    public TrialClinicalNegligence buildTrialClinicalNegligence() {
        return TrialClinicalNegligence.builder()
            .input1("Documents should be retained as follows:")
            .input2("the parties must retain all electronically stored "
                        + "documents relating to the issues in this Claim.")
            .input3("the defendant must retain the original clinical notes"
                        + " relating to the issues in this Claim. "
                        + "The defendant must give facilities for inspection "
                        + "by the claimant, "
                        + "the claimant's legal advisers and experts of these"
                        + " original notes on 7 days written notice.")
            .input4("Legible copies of the medical and educational "
                        + "records of the claimant are to be placed in a"
                        + " separate paginated bundle by the claimant’s "
                        + "solicitors and kept up to date. All references "
                        + "to medical notes are to be made by reference to"
                        + " the pages in that bundle")
            .build();
    }

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

    public TrialRoadTrafficAccident buildTrialRoadTrafficAccident() {
        return TrialRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location "
                       + "shall be prepared "
                       + "and agreed by the parties and uploaded to the"
                       + " Digital Portal by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(4))
            .build();
    }

    public TrialHousingDisrepair buildTrialHousingDisrepair() {
        return TrialHousingDisrepair.builder()
            .input1("The claimant must prepare a Scott Schedule of the items "
                        + "in disrepair")
            .input2("The columns should be headed: \n - Item \n - "
                        + "Alleged disrepair "
                        + "\n - Defendant's Response \n - "
                        + "Reserved for Judge's Use")
            .input3("The claimant must upload to the Digital Portal the "
                        + "Scott Schedule with the relevant columns "
                        + "completed by 4pm on")
            .date1(deadlineService.nextWorkingDayInWeeks(10))
            .input4("The defendant must upload to the Digital Portal "
                        + "the amended Scott Schedule with the relevant columns "
                        + "in response completed by 4pm on")
            .date2(deadlineService.nextWorkingDayInWeeks(12))
            .build();
    }
}
