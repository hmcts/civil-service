package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_1v1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_1v2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.CONFIRMATION_SUMMARY_2v1;

@SpringBootTest(classes = {
    CreateSDOCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimIssueConfiguration.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class},
    properties = {"reference.database.enabled=false"})
public class CreateSDOCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";

    @MockBean
    private Time time;

    @MockBean
    private IdamClient idamClient;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    protected LocationRefDataService locationRefDataService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().email(EMAIL).id(userId).build());

            given(time.now()).willReturn(submittedDate);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CREATE_SDO.name(), "READY");
        }
    }

    @Nested
    class MidEventDisposalHearingLocationRefDataCallback extends LocationRefSampleDataBuilder {
        private static final String PAGE_ID = "order-details";

        @Test
        void shouldPrePopulateDisposalHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            given(locationRefDataService.getCourtLocations(any())).willReturn(getSampleCourLocations());

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList))
                .containsOnly("ABCD - RG0 0 AL", "PQRS - GU0 0EE", "WXYZ - EW0 0HE", "LMNO - NE0 0BH");
        }
    }

    @Nested
    class MidEventPrePopulateOrderDetailsPagesCallback {
        private static final String PAGE_ID = "order-details";

        @Test
        void shouldPrePopulateOrderDetailsPages() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("fastTrackAltDisputeResolutionToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackVariationOfDirectionsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSettlementToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackTrialToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingQuestionsToExpertsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingBundleToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingClaimSettlingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingApplicationsOrderToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsWitnessStatementToggle").isNotNull();

            assertThat(response.getData()).extracting("disposalHearingJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the claim Form and Particulars of Claim/statements of case "
                               + "[and the directions questionnaires] \n\n"
                               + "IT IS ORDERED that:-");

            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("input")
                .isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is "
                               + "to be placed at the disposal hearing by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input1")
                .isEqualTo("The claimant shall serve on every other party the witness statements of all witnesses of "
                               + "fact on whose evidence reliance is to be placed by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input2")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input3")
                .isEqualTo("Any application by the defendant/s pursuant to CPR 32.7 must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(2).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input4")
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for trial on "
                               + "quantum as cross-examination will result in the hearing exceeding the 30 minute "
                               + "maximum time estimate for a disposal hearing");

            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("input1")
                .isEqualTo("The claimant has permission to rely upon the written expert evidence served with the "
                               + "Particulars of Claim to be disclosed by 4pm");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("input2")
                .isEqualTo("and any associated correspondence and/or updating report disclosed not later than "
                               + "4pm on the");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingQuestionsToExperts").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());

            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input1")
                .isEqualTo("If there is a claim for ongoing/future loss in the original schedule of losses then the "
                               + "claimant must send an up to date schedule of loss to the defendant by 4pm on the");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input2")
                .isEqualTo("The defendant, in the event of challenge, must send an up to date counter-schedule of loss"
                               + " to the claimant by 4pm on the");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("disposalHearingStandardDisposalOrder").extracting("input")
                .isEqualTo("input");

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("input")
                .isEqualTo("This claim be listed for final disposal before a Judge on the first available date after.");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundle").extracting("input")
                .isEqualTo("The claimant must lodge at court at least 7 days before the disposal");

            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have "
                               + "this Order set aside or varied. Any such application must be received by the Court "
                               + "(together with the appropriate fee) by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            assertThat(response.getData()).doesNotHaveToString("disposalHearingJudgementDeductionValue");
            assertThat(response.getData()).extracting("disposalHearingPreferredTelephone").extracting("telephone")
                .isEqualTo("N/A");
            assertThat(response.getData()).extracting("disposalHearingPreferredEmail").extracting("email")
                .isEqualTo("N/A");

            assertThat(response.getData()).extracting("fastTrackJudgesRecital").extracting("input")
                .isEqualTo("District Judge Perna has considered the statements of case and the information provided "
                               + "by the parties, \n\nIT IS ORDERED that:-");

            assertThat(response.getData()).doesNotHaveToString("fastTrackJudgementDeductionValue");

            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input1")
                .isEqualTo("By serving a list with a disclosure statement by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input2")
                .isEqualTo("Any request to inspect or for a copy of a document shall be made by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input3")
                .isEqualTo("and complied with within 7 days of receipt of the request.");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input4")
                .isEqualTo("Each party must serve and file with the court a list of issues relevant to the search for "
                               + "and disclosure of electronically stored documents, or must confirm there are "
                               + "no such issues, following Civil Procedure Rule Practise Direction 31B.");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input5")
                .isEqualTo("By 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input1")
                .isEqualTo("Each party shall serve on every other party the witness statements of all "
                               + "witnesses of fact on whom he intends to rely");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input2")
                .isEqualTo("All statements to be no more than");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").doesNotHaveToString("input3");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input4")
                .isEqualTo("pages long, A4, double spaced and in font size 12.");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input5")
                .isEqualTo("There shall be simultaneous exchange of such statements by 4pm on");
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackWitnessOfFact").extracting("input6")
                .isEqualTo("Oral evidence will not be permitted at trail from a witness whose statement has not been "
                               + "served in accordance with this order or has been served late, except with "
                               + "permission from the Court.");

            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input1")
                .isEqualTo("The claimant shall serve an updated schedule of loss on the defendant(s) by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input2")
                .isEqualTo("The defendant(s) shall serve a counter schedule on the Claimant by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input3")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties have not already set out "
                               + "their case on periodical payments, then they must do so in the respective schedule "
                               + "and counter-schedule.");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input4")
                .isEqualTo("Upon it being noted that the schedule of loss contains no claim for continuing loss and is "
                               + "therefore final, no further schedule of loss shall be served without permission "
                               + "to amend. The defendant shall file a counter-schedule of loss by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input1")
                .isEqualTo("The time provisionally allowed for the trial is");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(30).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input2")
                .isEqualTo("If either party considers that the time estimate is insufficient, they must inform the "
                               + "court within 7 days of the date of this Order.");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input3")
                .isEqualTo("Not more than seven nor less than three clear days before the trial, "
                               + "the claimant must file at court and serve an indexed and paginated bundle of "
                               + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                               + "and Practice Direction 39A. The parties must endeavour to agree the contents of "
                               + "the bundle before it is filed. the bundle will include a case summary "
                               + "and a chronology.");

            assertThat(response.getData()).extracting("fastTrackNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have "
                               + "this Order set aside or varied. Any such application must be received by the Court"
                               + " (together with the appropriate fee) by 4pm on");
            assertThat(response.getData()).extracting("fastTrackNotes").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(1).toString());

            assertThat(response.getData()).extracting("fastTrackPreferredTelephone").extracting("telephone")
                .isEqualTo("N/A");
            assertThat(response.getData()).extracting("fastTrackPreferredEmail").extracting("email")
                .isEqualTo("N/A");

            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage "
                               + "or any other relevant matters");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input2")
                .isEqualTo("The column headings will be as follows: Item; Alleged Defect; claimant's Costing; "
                               + "defendant's Response; defendant's Costing; Reserved for Judge's Use");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input3")
                .isEqualTo("The claimant must serve the Scott Schedule with the relevant columns completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input4")
                .isEqualTo("The defendant must file and serve the Scott Schedule with the relevant columns "
                               + "in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());

            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input1")
                .isEqualTo("Documents are to be retained as follows:");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input2")
                .isEqualTo("the parties must retain all electronically stored documents relating to the issues "
                               + "in this Claim.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input3")
                .isEqualTo("the defendant must retain the original clinical notes relating to the issues in this Claim."
                               + " The defendant must give facilities for inspection by the claimant, the claimant's"
                               + " legal advisers and experts of these original notes on 7 days written notice.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input4")
                .isEqualTo("Legible copies of the medical and educational records of the claimant / Deceased / "
                               + "claimant's Mother are to be placed in a separate paginated bundle by the "
                               + "claimant's Solicitors and kept up to date. All references to medical notes are to be "
                               + "made by reference to the pages in that bundle.");

            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input1")
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
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input2")
                .isEqualTo("3. The claimant must file and serve a witness statement addressing, (a) need to hire a "
                               + "replacement vehicle and (b) impecuniosity no later than 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input3")
                .isEqualTo("Failure to comply with the paragraph above will result in the claimant being debarred from "
                               + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                               + "save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input4")
                .isEqualTo("4. The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on.");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input5")
                .isEqualTo("5. If the parties fail to agree rates subject to liability and/or other issues pursuant to "
                               + "the paragraph above, each party may rely upon written evidence by way of witness "
                               + "statement of one witness to provide evidence of basic hire rates available within "
                               + "the claimant's geographical location, from a mainstream (or, if none available, a "
                               + "local reputable) supplier. The defendant's evidence to be served by 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input6")
                .isEqualTo("and the claimant's evidence in reply if so advised to be served by 4pm on");
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(14).toString());
            assertThat(response.getData()).extracting("fastTrackCreditHire").extracting("input7")
                .isEqualTo("This witness statement is limited to 10 pages per party (to include any appendices).");

            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the items of disrepair");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input2")
                .isEqualTo("The column headings will be as follows: Item; Alleged disrepair; "
                               + "Defendant's Response; Reserved for Judge's Use");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input3")
                .isEqualTo("The claimant must serve the Scott Schedule with the relevant columns completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString()); // placeholder date for now. tbc
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input4")
                .isEqualTo("The Defendant must file and serve the Scott Schedule with the relevant column "
                               + "in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString()); // placeholder date for now, tbc

            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input1")
                .isEqualTo("1. The claimant has permission to rely on the written expert evidence annexed to the "
                               + "Particulars of Claim. Defendant may raise written questions of the expert by 4pm on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input2")
                .isEqualTo("which must be answered by 4pm on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input3")
                .isEqualTo("No other permission is given for expert evidence.");

            assertThat(response.getData()).extracting("fastTrackRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a plan of the location of the accident shall be prepared and "
                               + "agreed by the parties.");

            assertThat(response.getData()).extracting("smallClaimsJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the statements of case and the information provided by the parties,");

            assertThat(response.getData()).doesNotHaveToString("smallClaimsJudgementDeductionValue");

            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input1")
                .isEqualTo("The hearing of the claim will be on a date to be notified to you by a separate "
                               + "notification. The hearing will have a time estimate of");
            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input2")
                .isEqualTo("The claimant must by no later than 14 days before the hearing date, pay the court the "
                               + "required hearing fee or submit a fully completed application for Help with Fees. "
                               + "If the claimant fails to pay the fee or obtain a fee exemption by that time the "
                               + "claim will be struck without further order.");

            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of all documents which they wish the"
                               + " court to consider when reaching its decision not less than 14 days before "
                               + "the hearing.");
            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input2")
                .isEqualTo("The court may refuse to consider any document which has not been uploaded to the "
                               + "Digital Portal by the above date.");

            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of all witness statements of the"
                               + " witnesses upon whose evidence they intend to rely at the hearing not less than 14"
                               + " days before the hearing.");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").doesNotHaveToString("input2");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").doesNotHaveToString("input3");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("input4")
                .isEqualTo("For this limitation, a party is counted as a witness.");
            assertThat(response.getData()).extracting("smallClaimsWitnessStatement").extracting("text")
                .isEqualTo("A witness statement must: \na) Start with the name of the case and the claim number;"
                               + "\nb) State the full name and address of the witness; "
                               + "\nc) Set out the witness's evidence clearly in numbered paragraphs on numbered pages;"
                               + "\nd) End with this paragraph: 'I believe that the facts stated in this witness "
                               + "statement are true. I understand that proceedings for contempt of court may be "
                               + "brought against anyone who makes, or causes to be made, a false statement in a "
                               + "document verified by a statement of truth without an honest belief in its truth'."
                               + "\ne) be signed by the witness and dated."
                               + "\nf) If a witness is unable to read the statement there must be a certificate that "
                               + "it has been read or interpreted to the witness by a suitably qualified person and "
                               + "at the final hearing there must be an independent interpreter who will not be "
                               + "provided by the Court."
                               + "\n\nThe judge may refuse to allow a witness to give evidence or consider any "
                               + "statement of any witness whose statement has not been uploaded to the Digital Portal"
                               + " in accordance with the paragraphs above."
                               + "\n\nA witness whose statement has been uploaded in accordance with the above must"
                               + " attend the hearing. If they do not attend, it will be for the court to decide how"
                               + " much reliance, if any, to place on their evidence.");

            assertThat(response.getData()).extracting("smallClaimsNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have "
                               + "this Order set aside or varied. Any such application must be received by the Court, "
                               + "together with the appropriate fee by 4pm on");

            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input1")
                .isEqualTo("If impecuniosity is alleged by the claimant and not admitted by the defendant, the "
                               + "claimant's disclosure as ordered earlier in this Order must include:\n"
                               + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                               + "commencement of hire until the earlier of:\n "
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "b) Copies of all bank, credit card, and saving account statements for a period of 3"
                               + " months prior to the commencement of hire until the earlier of:\n"
                               + "     i) 3 months after cessation of hire\n"
                               + "     ii) the repair or replacement of the claimant's vehicle\n"
                               + "c) Evidence of any loan, overdraft or other credit facilities available to the "
                               + "claimant.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input2")
                .isEqualTo("The claimant must upload to the Digital Portal a witness statement addressing\n"
                               + "a) the need to hire a replacement vehicle; and\n"
                               + "b) impecuniosity");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input3")
                .isEqualTo("A failure to comply with the paragraph above will result in the claimant being debarred "
                               + "from asserting need or relying on impecuniosity as the case may be at the final "
                               + "hearing, save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input4")
                .isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input5")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to"
                               + " the paragraph above, each party may rely upon written evidence by way of witness"
                               + " statement of one witness to provide evidence of basic hire rates available within"
                               + " the claimant's geographical location, from a mainstream supplier, or a local"
                               + " reputable supplier if none is available.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input6")
                .isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input7")
                .isEqualTo("and the claimant's evidence is reply if so advised to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input8")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to "
                               + "the paragraph above, each party may rely upon the written evidence by way of witness"
                               + " statement of one witness to provide evidence of basic hire rates available within"
                               + " the claimant's geographical location from a mainstream supplier, or a local"
                               + " reputable supplier if none is available.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input9")
                .isEqualTo("The defendant’s evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date5")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input10")
                .isEqualTo(", and the claimant’s evidence in reply if so advised is to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date6")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input11")
                .isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");

            assertThat(response.getData()).extracting("smallClaimsRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a place of the accident location shall be prepared and agreed by the "
                               + "parties and uploaded to the Digital Portal no later than 14 days before the "
                               + "hearing.");
        }

        @Test
        void shouldPrePopulateDisposalHearingJudgementDeductionValueWhenDrawDirectionsOrderIsNotNull() {
            JudgementSum tempJudgementSum = JudgementSum.builder()
                .judgementSum(12.0)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrder(tempJudgementSum)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("fastTrackJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("smallClaimsJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
        }

        @Test
        void shouldPrePopulateDisposalHearingPreferredTelephoneAndEmailWhenHearingSupportRequirementsDJIsNotNull() {
            HearingSupportRequirementsDJ tempHearingSupportRequirementsDJ = HearingSupportRequirementsDJ.builder()
                .hearingPreferredTelephoneNumber1("000")
                .hearingPreferredEmail("test@email.com")
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .hearingSupportRequirementsDJ(tempHearingSupportRequirementsDJ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingPreferredTelephone").extracting("telephone")
                .isEqualTo("000");
            assertThat(response.getData()).extracting("disposalHearingPreferredEmail").extracting("email")
                .isEqualTo("test@email.com");
            assertThat(response.getData()).extracting("fastTrackPreferredTelephone").extracting("telephone")
                .isEqualTo("000");
            assertThat(response.getData()).extracting("fastTrackPreferredEmail").extracting("email")
                .isEqualTo("test@email.com");
        }
    }

    @Nested
    class MidEventSetOrderDetailsFlags {
        private static final String PAGE_ID = "order-details-navigation";

        @Test
        void smallClaimsFlagAndFastTrackFlagSetToNo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void fastTrackFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1v1,
                "Mr. John Rambo",
                "Mr. Sole Trader"
            );

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1v2,
                "Mr. John Rambo",
                "Mr. Sole Trader",
                "Mr. John Rambo"
            );

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_2v1,
                "Mr. John Rambo",
                "Mr. Jason Rambo",
                "Mr. Sole Trader"
            );

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_SDO);
    }
}
