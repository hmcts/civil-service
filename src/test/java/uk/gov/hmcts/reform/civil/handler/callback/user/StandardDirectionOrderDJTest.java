package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    StandardDirectionOrderDJ.class,
    JacksonAutoConfiguration.class
})

public class StandardDirectionOrderDJTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private StandardDirectionOrderDJ handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnExpectedResponse_WhenCase1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("applicantVRespondentText"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void shouldReturnExpectedResponse_WhenCase2v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .multiPartyClaimTwoApplicants()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("applicantVRespondentText"))
                .isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
        }

        @Test
        void shouldReturnExpectedResponse_WhenCase1v2() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getData().get("applicantVRespondentText"))
                .isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. John Rambo");
        }

    }

    @Nested
    class MidEventPrePopulateDisposalHearingPageCallback {

        private static final String PAGE_ID = "trial-disposal-screen";

        @Test
        void shouldPrePopulateDJDisposalAndTrialHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgesRecitalDJ").extracting("input")
                .isEqualTo("Upon considering the claim Form and Particulars of Claim/statements of case "
                               + "[and the directions questionnaires] \n\n"
                               + "IT IS ORDERED that:-");

            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsDJ").extracting("input")
                .isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is "
                               + "to be placed at the disposal hearing by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input1")
                .isEqualTo("The claimant shall serve on every other party the witness statements of all witnesses of "
                               + "fact on whose evidence reliance is to be placed by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input2")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input3")
                .isEqualTo("Any application by the defendant/s pursuant to CPR 32.7 must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(2).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for trial on "
                               + "quantum as cross-examination will result in the hearing exceeding the 30 minute "
                               + "maximum time estimate for a disposal hearing");

            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("input1")
                .isEqualTo("The claimant has permission to rely upon the written expert evidence served with the "
                               + "Particulars of Claim to be disclosed by 4pm");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("input2")
                .isEqualTo("and any associated correspondence and/or updating report disclosed not later than "
                               + "4pm on the");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingQuestionsToExpertsDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());

            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("input1")
                .isEqualTo("If there is a claim for ongoing/future loss in the original schedule of losses then the "
                               + "claimant must send an up to date schedule of loss to the defendant by 4pm on the");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("input2")
                .isEqualTo("The defendant, in the event of challenge, must send an up to date counter-schedule of loss"
                               + " to the claimant by 4pm on the");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("disposalHearingStandardDisposalOrderDJ").extracting("input")
                .isEqualTo("input");

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingDJ").extracting("input")
                .isEqualTo("This claim be listed for final disposal before a Judge on the first available date after.");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundleDJ").extracting("input")
                .isEqualTo("The claimant must lodge at court at least 7 days before the disposal");

            assertThat(response.getData()).extracting("disposalHearingNotesDJ").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have "
                               + "this Order set aside or varied. Any such application must be received by the Court "
                               + "(together with the appropriate fee) by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingNotesDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            //trialHearingJudgesRecitalDJ
            assertThat(response.getData()).extracting("trialHearingJudgesRecitalDJ").extracting("input")
                .isEqualTo("[Title] [your name] has considered the statements of "
                               + "the case and the information provided "
                               + "by the parties, \n\n "
                               + "IT IS ORDERED THAT:");

            //trialHearingDisclosureOfDocumentsDJ
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input1")
                .isEqualTo("By serving a list with a disclosure statement by 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input2")
                .isEqualTo("Any request to inspect or for a copy of a document "
                               + "shall by made by 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input3")
                .isEqualTo("and complied with with 7 days of the request");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input4")
                .isEqualTo("Each party must serve and file with the court a "
                               + "list of issues relevant to the search for and "
                               + "disclosure of electronically stored documents, "
                               + "or must confirm there are no such issues, following"
                               + " Civil Rule Practice Direction 31B.");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input5")
                .isEqualTo("By 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            //trialHearingWitnessOfFactDJ
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input1")
                .isEqualTo("Each party shall serve on every other party the witness "
                               + "statements of all witnesses of fact on whom he "
                               + "intends to rely");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input2")
                .isEqualTo("All statements to be no more than");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("pages long, A4, double spaced and in font size 12.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input5")
                .isEqualTo("There shall be simultaneous exchange of such "
                               + "statements by 4pm on");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input6")
                .isEqualTo("Oral evidence will not be permitted at trial from a "
                               + "witness whose statement has not been served in accordance"
                               + " with this order or has been served late, except with "
                               + "permission from the court");

            //trialHearingSchedulesOfLossDJ
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input1")
                .isEqualTo("The claimant shall serve an updated schedule of loss "
                               + "on the defendant(s) by 4pm on");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input2")
                .isEqualTo("The defendant(s) shall serve a counter "
                               + "schedule on the claimant by 4pm on");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input3")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties"
                               + " have not already set out their case on periodical payments. "
                               + "then they must do so in the respective schedule "
                               + "and counter-schedule");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input4")
                .isEqualTo("Upon it being noted that the schedule of loss contains no claim "
                               + "for continuing loss and is therefore final, no further"
                               + " schedule of loss shall be served without permission "
                               + "to amend. The defendant shall file a counter-schedule "
                               + "of loss by 4pm on");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            //trialHearingTrialDJ
            assertThat(response.getData()).extracting("trialHearingTrialDJ").extracting("input1")
                .isEqualTo("The time provisionally allowed for the trial is");
            assertThat(response.getData()).extracting("trialHearingTrialDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("trialHearingTrialDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(34).toString());
            assertThat(response.getData()).extracting("trialHearingTrialDJ").extracting("input2")
                .isEqualTo("If either party considers that the time estimates is"
                               + " insufficient, they must inform the court within "
                               + "7 days of the date of this order.");
            assertThat(response.getData()).extracting("trialHearingTrialDJ").extracting("input3")
                .isEqualTo("Not more than seven nor less than three clear days before "
                               + "the trial, the claimant must file at court and serve an"
                               + "indexed and paginated bundle of documents which complies"
                               + " with the requirements of Rule 39.5 Civil "
                               + "Procedure Rules"
                               + " and Practice Direction 39A. The parties must "
                               + "endeavour to agree the contents of the "
                               + "bundle before it is filed. "
                               + "The bundle will include a case summary"
                               + " and a chronology.");

            //trialHearingNotesDJ
            assertThat(response.getData()).extracting("trialHearingNotesDJ").extracting("input")
                .isEqualTo("This order has been made without a hearing. Each party has "
                               + "the right to apply to have this order set aside or varied."
                               + " Any such application must be received by the court "
                               + "(together with the appropriate fee) by 4pm on");
            assertThat(response.getData()).extracting("trialHearingNotesDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            //Additional instructions
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage "
                               + "or any other relevant matters");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input2")
                .isEqualTo("The column headings will be as follows: Item; Alleged Defect; claimant's Costing; "
                               + "defendant's Response; defendant's Costing; Reserved for Judge's Use");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input3")
                .isEqualTo("The claimant must serve the Scott Schedule with the relevant columns completed by 4pm on");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input4")
                .isEqualTo("The defendant must file and serve the Scott Schedule with the relevant columns "
                               + "in response completed by 4pm on");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("trialClinicalNegligence").extracting("input1")
                .isEqualTo("Documents are to be retained as follows:");
            assertThat(response.getData()).extracting("trialClinicalNegligence").extracting("input2")
                .isEqualTo("the parties must retain all electronically stored documents relating to the issues "
                               + "in this Claim.");
            assertThat(response.getData()).extracting("trialClinicalNegligence").extracting("input3")
                .isEqualTo("the defendant must retain the original clinical notes relating to the issues in this Claim."
                               + " The defendant must give facilities for inspection by the claimant, the claimant's"
                               + " legal advisers and experts of these original notes on 7 days written notice.");
            assertThat(response.getData()).extracting("trialClinicalNegligence").extracting("input4")
                .isEqualTo("Legible copies of the medical and educational records of the claimant / Deceased / "
                               + "claimant's Mother are to be placed in a separate paginated bundle by the "
                               + "claimant's Solicitors and kept up to date. All references to medical notes are to be "
                               + "made by reference to the pages in that bundle.");

            assertThat(response.getData()).extracting("trialCreditHire").extracting("input1")
                .isEqualTo("1. If impecuniosity is alleged by the claimant and not admitted by the defendant, the "
                               + "claimant's disclosure as ordered earlier in this order must include:\n"
                               + "a. Evidence of all income from all sources for a period of 3 months prior to the "
                               + "commencement of hire until the earlier of i) 3 months after cessation of hire or ii) "
                               + "the repair/replacement of the claimant's vehicle;\n"
                               + "b. Copy statements of all blank, credit care and savings accounts for a period of "
                               + "3 months prior to the commencement of hire until the earlier of i) 3 months after "
                               + "cessation of hire or ii) the repair/replacement of the claimant's vehicle;\n"
                               + "c. Evidence of any loan, overdraft or other credit facilities available to the "
                               + "claimant");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input2")
                .isEqualTo("The claimant must file and serve a witness statement addressing, (a) need to hire a "
                               + "replacement vehicle and (b) impecuniosity no later than 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input3")
                .isEqualTo("Failure to comply with the paragraph above will result in the claimant being debarred from "
                               + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                               + "save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input4")
                .isEqualTo("4. The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on.");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input5")
                .isEqualTo("5. If the parties fail to agree rates subject to liability and/or other issues pursuant to "
                               + "the paragraph above, each party may rely upon written evidence by way of witness "
                               + "statement of one witness to provide evidence of basic hire rates available within "
                               + "the claimant's geographical location, from a mainstream (or, if none available, a "
                               + "local reputable) supplier. The defendant's evidence to be served by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input6")
                .isEqualTo("and the claimant's evidence in reply if so advised to be served by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(14).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input7")
                .isEqualTo("This witness statement is limited to 10 pages per party (to include any appendices).");

            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input1")
                .isEqualTo("1. The claimant has permission to rely on the written expert evidence annexed to the "
                               + "Particulars of Claim. Defendant may raise written questions of the expert by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input2")
                .isEqualTo("which must be answered by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input3")
                .isEqualTo("No other permission is given for expert evidence.");

            assertThat(response.getData()).extracting("trialRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a plan of the location of the accident shall be prepared and "
                               + "agreed by the parties.");

        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldFinishBusinessProcess() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("businessProcess").isNotNull();
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked1v1() {
            String body = "The directions order has been sent to: %n%n ## Claimant 1 %n%n Mr. John Rambo%n%n "
                + "## Defendant 1 %n%n Mr. Sole Trader";
            String header = "# Your order has been issued %n%n ## Claim number %n%n # 000DC001";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment().build().toBuilder()
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(header))
                    .confirmationBody(format(body))
                    .build());
        }
    }
}

