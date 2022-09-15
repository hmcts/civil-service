package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgmentOrderFormGenerator.class,
    StandardDirectionOrderDJ.class,
    JacksonAutoConfiguration.class
})

public class StandardDirectionOrderDJTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private StandardDirectionOrderDJ handler;
    @MockBean
    private DefaultJudgmentOrderFormGenerator defaultJudgmentOrderFormGenerator;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private UserDetails userDetails;

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

        @BeforeEach
        void setup() {

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().forename("test").surname("judge").build());
        }

        @Test
        void shouldPrePopulateDJDisposalAndTrialHearingPage() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearing().build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgesRecitalDJ").extracting("input")
                .isEqualTo("test judge, Upon considering the claim form and Particulars of Claim/statements of case "
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
                .isEqualTo("The claimant must upload to the Digital Portal copies of the "
                               + "witness statements of all witnesses whose evidence they "
                               + "wish the court to consider when deciding the amount of "
                               + "damages by by 4pm on ");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input5")
                .isEqualTo("Any application by the defendant/s pursuant to CPR 32.7 must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(2).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input6")
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for trial on "
                               + "quantum as cross-examination will result in the hearing exceeding the 30 minute "
                               + "maximum time estimate for a disposal hearing");

            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("input1")
                .isEqualTo("The claimant has permission to rely upon the"
                               + " written expert evidence already uploaded to"
                               + " the Digital Portal with the particulars of "
                               + "claim and in addition has permission to rely"
                               + " upon any associated correspondence or "
                               + "updating report which is uploaded to the "
                               + "Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingQuestionsToExpertsDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());

            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("input1")
                .isEqualTo("If there is a claim for ongoing/future loss in the original schedule of losses then the "
                               + "claimant must send an up to date schedule of loss to the defendant by 4pm on the");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("input2")
                .isEqualTo("If the defendant wants to challenge this claim,"
                               + " they must send an up-to-date "
                               + "counter-schedule of loss to the "
                               + "claimant by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("input3")
                .isEqualTo("If the defendant wants to challenge the"
                               + " sums claimed in the schedule of loss they"
                               + " must upload to the Digital Portal an "
                               + "updated counter schedule of loss by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());


            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingDJ").extracting("input")
                .isEqualTo("This claim be listed for final disposal before a Judge on the first available date after.");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundleDJ").extracting("input")
                .isEqualTo("The claimant must lodge at court at least 7 days before the disposal");

            assertThat(response.getData()).extracting("disposalHearingNotesDJ").extracting("input")
                .isEqualTo("This order has been made without a hearing. Each "
                               + "party has the right to apply to have this order set "
                               + "aside or varied. Any such application must be uploaded "
                               + "to the Digital Portal together with payment of any "
                               + "appropriate fee, by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingNotesDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            //trialHearingJudgesRecitalDJ
            assertThat(response.getData()).extracting("trialHearingJudgesRecitalDJ").extracting("input")
                .isEqualTo("test judge, has considered the statements of "
                               + "the case and the information provided "
                               + "by the parties, \n\n "
                               + "IT IS ORDERED THAT:");

            //trialHearingDisclosureOfDocumentsDJ
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input1")
                .isEqualTo("By uploading to a Digital Portal a list with a disclosure statement by 4pm on");
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
                .isEqualTo("Each party must upload to the Digital Portal copies "
                               + "of those documents on which they wish to rely at trial");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input5")
                .isEqualTo("By 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            //trialHearingWitnessOfFactDJ
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal"
                               + " copies of the statements of all witnesses of fact on whom they intend to rely.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input2")
                .isEqualTo("All statements to be no more than");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("pages long, A4, double spaced and in font size 12.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input5")
                .isEqualTo("Witness statements shall be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input6")
                .isEqualTo("Oral evidence will only be permitted at trial with permission from the Court "
                               + "from witnesses whose statements have not been uploaded to the Digital "
                               + "Portal in accordance with this order, or whose statements that "
                               + "have been served late.");

            //trialHearingSchedulesOfLossDJ
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input1")
                .isEqualTo("The claimant must upload to the Digital Portal "
                               + "an up-to-date schedule of loss to the defendant by 4pm on");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input2")
                .isEqualTo("If the defendant wants to challenge this claim, "
                               + "upload to the Digital Portal counter-schedule"
                               + " of loss by 4pm on");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input3")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties"
                               + " have not already set out their case on periodical payments. "
                               + "then they must do so in the respective schedule "
                               + "and counter-schedule");
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("input4")
                .isEqualTo("Upon it being noted that the schedule of loss "
                               + "contains no claim for continuing loss and is "
                               + "therefore final, no further schedule of loss shall"
                               + " be uploaded without permission to amend. "
                               + "The defendant shall upload to the Digital Portal"
                               + " an up-to-date counter "
                               + "schedule of loss by 4pm on");
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
                .isEqualTo("At least 7 days before the trial, the claimant must"
                               + " upload to the Digital Portal ");

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
                .isEqualTo("The columns should be headed: Item; Alleged Defect; "
                               + "Claimant's costing; Defendant's response; Defendant's"
                               + " costing; Reserved for Judge's use.");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the "
                               + "Scott Schedule with the relevant "
                               + "columns completed by 4pm on");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal "
                               + "an amended version of the Scott Schedule with the relevant"
                               + " columns in response completed by 4pm on");
            assertThat(response.getData()).extracting("trialBuildingDispute").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("trialClinicalNegligence").extracting("input1")
                .isEqualTo("Documents should be retained as follows:");
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
                .isEqualTo("If impecuniosity is alleged by the claimant and not admitted by the defendant, the "
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
                .isEqualTo("The claimant must upload to the Digital Portal a witness "
                               + "statement addressing a)the need to hire a replacement "
                               + "vehicle; and b)impecuniosity");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input3")
                .isEqualTo("This statement must be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input4")
                .isEqualTo("A failure to comply will result in the claimant being "
                               + "debarred from asserting need or relying on impecuniosity "
                               + "as the case may be at the final hearing, unless they "
                               + "have the permission of the trial Judge.");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input5")
                .isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on.");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input6")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to "
                               + "the paragraph above, each party may rely upon written evidence by way of witness "
                               + "statement of one witness to provide evidence of basic hire rates available within "
                               + "the claimant's geographical location, from a mainstream (or, if none available, a "
                               + "local reputable) supplier. The defendant's evidence to be served by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input7")
                .isEqualTo("and the claimant’s evidence in reply if so advised is to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(14).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input8")
                .isEqualTo("This witness statement is limited to 10 pages per party (to include any appendices).");

            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the items "
                               + "of disrepair");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input2")
                .isEqualTo("The column headings will be as follows: Item; Alleged "
                               + "disrepair; Defendant's Response; Reserved for Judge's Use");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input3")
                .isEqualTo("The claimant must uploaded to the Digital Portal the "
                               + "Scott Schedule with the relevant columns "
                               + "completed by 4pm on");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input4")
                .isEqualTo("The defendant must uploaded to the Digital Portal "
                               + "the amended Scott Schedule with the relevant columns "
                               + "in response completed by 4pm on");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input1")
                .isEqualTo("The claimant has permission to rely upon the written "
                               + "expert evidence already uploaded to the Digital"
                               + " Portal with the particulars of claim and in addition "
                               + "has permission to rely upon any associated "
                               + "correspondence or updating report which is uploaded "
                               + "to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input2")
                .isEqualTo("which must be answered by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input3")
                .isEqualTo("Any questions which are to be addressed to an expert" +
                               " must be sent to the expert directly and uploaded to " +
                               "the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("trialRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a place of the accident location shall be prepared "
                               + "and agreed by the parties and uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialRoadTrafficAccident").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
        }

        @Test
        void shouldPrePopulateDJTrialHearingToggle() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedTrialHearing().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("trialHearingVariationsDirectionsDJToggle").isNotNull();
        }
    }

    @Nested
    class MidEventCreateOrderCallback {
        private static final String PAGE_ID = "create-order";

        @Test
        void shouldCreateAndSaveSDOOrder() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
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

