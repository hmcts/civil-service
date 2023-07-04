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
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ACKNOWLEDGEMENT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgmentOrderFormGenerator.class,
    StandardDirectionOrderDJ.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})

public class StandardDirectionOrderDJTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private StandardDirectionOrderDJ handler;
    @Autowired
    private AssignCategoryId assignCategoryId;
    @MockBean
    private DefaultJudgmentOrderFormGenerator defaultJudgmentOrderFormGenerator;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private UserDetails userDetails;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CategoryService categoryService;

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
        private final LocalDate date = LocalDate.of(2022, 3, 29);

        @BeforeEach
        void setup() {
            when(deadlinesCalculator.plusWorkingDays(any(), anyInt())).thenReturn(date);
            given(idamClient.getUserDetails(any()))
                .willReturn(UserDetails.builder().forename("test").surname("judge").build());
        }

        @Test
        void shouldPrePopulateDJDisposalAndTrialHearingPage() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                              .courtName("Court Name").region("Region").regionId("1").courtVenueId("000")
                              .epimmsId("123").build());
            locations.add(LocationRefData.builder().siteName("Loc").courtAddress("1").postcode("1")
                              .courtName("Court Name").region("Region").regionId("1").courtVenueId("000")
                              .epimmsId("123").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(categorySearchResult));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearing().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgesRecitalDJ").extracting("input")
                .isEqualTo("test judge" + ",");

            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsDJ").extracting("input")
                .isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is "
                               + "to be placed at the disposal hearing by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input1")
                .isEqualTo("The claimant must upload to the Digital Portal copies of "
                               + "the witness statements of all witnesses "
                               + "of fact on whose evidence reliance is "
                               + "to be placed by 4pm on ");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input2")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input3")
                .isEqualTo("Any application by the defendant in relation to CPR 32.7 "
                               + "must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(2).toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("and must be accompanied by proposed directions for allocation"
                               + " and listing for trial on quantum. This is because"
                               + " cross-examination will cause the hearing to exceed"
                               + " the 30 minute maximum time estimate for a disposal"
                               + " hearing.");

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
                .isEqualTo("If there is a claim for ongoing or future loss in the original schedule of losses then the "
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
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossDJ").extracting("inputText4")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties have not already set out "
                               + "their case on periodical payments, they must do so in the respective schedule"
                               + " and counter-schedule.");

            assertThat(response.getData())
                .extracting("disposalHearingFinalDisposalHearingDJ").extracting("input")
                .isEqualTo("This claim will be listed for final disposal "
                                + "before a Judge on the first available date after");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundleDJ").extracting("input")
                .isEqualTo("At least 7 days before the disposal hearing, the claimant must file and serve");

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
                .isEqualTo("test judge" + ",");

            //trialHearingDisclosureOfDocumentsDJ
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input1")
                .isEqualTo("Standard disclosure shall be provided by "
                               + "the parties by uploading to the digital "
                               + "portal their lists of documents by 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input2")
                .isEqualTo("Any request to inspect a document, or for a copy of a "
                               + "document, shall be made directly to the other party by 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(6).toString());
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input3")
                .isEqualTo("Requests will be complied with within 7 days of the receipt of the request");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input4")
                .isEqualTo("Each party must upload to the Digital Portal"
                               + " copies of those documents on which they wish to rely"
                               + " at trial");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("input5")
                .isEqualTo("by 4pm on");
            assertThat(response.getData()).extracting("trialHearingDisclosureOfDocumentsDJ").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());

            //trialHearingWitnessOfFactDJ
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of the "
                               + "statements of all witnesses of fact on whom they "
                               + "intend to rely.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input2")
                .isEqualTo("3");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input3")
                .isEqualTo("3");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input4")
                .isEqualTo("For this limitation, a party is counted as witness.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input5")
                .isEqualTo("Each witness statement should be no more than");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input6")
                .isEqualTo("10");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input7")
                .isEqualTo("A4 pages. Statements should be double spaced "
                               + "using a font size of 12.");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input8")
                .isEqualTo("Witness statements shall be uploaded to the "
                               + "Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialHearingWitnessOfFactDJ").extracting("input9")
                .isEqualTo("Evidence will not be permitted at trial from a witness whose "
                               + "statement has not been uploaded in accordance with this"
                               + " Order. Evidence not uploaded, or uploaded late, will not "
                               + "be permitted except with permission from the Court");

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
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialHearingSchedulesOfLossDJ").extracting("date2")
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
                .isEqualTo("The columns should be headed: \n - Item \n - "
                               + "Alleged Defect "
                               + "\n - Claimant's costing\n - Defendant's"
                               + " response\n - Defendant's costing"
                               + " \n - Reserved for Judge's use");
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
                .isEqualTo("Legible copies of the medical and educational "
                               + "records of the claimant are to be placed in a"
                               + " separate paginated bundle by the claimant’s "
                               + "solicitors and kept up to date. All references "
                               + "to medical notes are to be made by reference to"
                               + " the pages in that bundle");

            assertThat(response.getData()).extracting("trialCreditHire").extracting("input1")
                .isEqualTo("If impecuniosity is alleged by the claimant and not admitted "
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
                               + "prior to the commencement of hire until the earlier of \n    i)"
                               + " 3 months after cessation of hire "
                               + "or \n    ii) the repair or replacement of the claimant's vehicle;\n"
                               + "c. Evidence of any loan, overdraft or other credit "
                               + "facilities available to the claimant");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input2")
                .isEqualTo("The claimant must upload to the Digital Portal a witness "
                               + "statement addressing \na) the need to hire a replacement "
                               + "vehicle; and \nb) impecuniosity");
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
                .isEqualTo("The parties are to liaise and use reasonable endeavours to"
                               + " agree the basic hire rate no "
                               + "later than 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input6")
                .isEqualTo("If the parties fail to agree rates subject to liability "
                               + "and/or other issues pursuant to the paragraph above, "
                               + "each party may rely upon the written evidence by way of"
                               + " witness statement of one witness to provide evidence of "
                               + "basic hire rates available within the claimant’s geographical"
                               + " location from a mainstream supplier, or a local reputable "
                               + "supplier if none is available. The defendant’s evidence is "
                               + "to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(12).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input7")
                .isEqualTo("and the claimant’s evidence in reply if "
                               + "so advised is to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("trialCreditHire").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(14).toString());
            assertThat(response.getData()).extracting("trialCreditHire").extracting("input8")
                .isEqualTo("This witness statement is limited to 10 pages per party "
                               + "(to include any appendices).");

            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the items "
                               + "in disrepair");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input2")
                .isEqualTo("The columns should be headed: \n - Item \n - Alleged disrepair "
                               + "\n - Defendant's Response \n - Reserved for Judge's Use");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the "
                               + "Scott Schedule with the relevant columns "
                               + "completed by 4pm on");
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(10).toString());
            assertThat(response.getData()).extracting("trialHousingDisrepair").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal "
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
                .isEqualTo("Any questions which are to be addressed to an expert must "
                               + "be sent to the expert directly and uploaded to the Digital "
                               + "Portal by 4pm on");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input3")
                .isEqualTo("The answers to the questions shall be answered by the Expert by");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date3")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("input4")
                .isEqualTo("and uploaded to the Digital Portal by");
            assertThat(response.getData()).extracting("trialPersonalInjury").extracting("date4")
                .isEqualTo(LocalDate.now().plusWeeks(8).toString());

            assertThat(response.getData()).extracting("trialRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a place of the accident location shall be prepared "
                               + "and agreed by the parties and uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("trialRoadTrafficAccident").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(4).toString());

            assertThat(response.getData()).extracting("disposalHearingOrderMadeWithoutHearingDJ").extracting("input")
                .isEqualTo(String.format("This order has been made without a hearing. Each party "
                                             + "has the right to apply to have this Order "
                                             + "set aside or varied. Any such application must be "
                                             + "received by the Court "
                                             + "(together with the appropriate fee) by 4pm on %s.",
                                         date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))));

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingTimeDJ").extracting("input")
                .isEqualTo("This claim will be listed for final "
                               + "disposal before a Judge on the first "
                               + "available date after");

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingTimeDJ").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("trialHearingTimeDJ").extracting("helpText1")
                .isEqualTo("If either party considers that the time estimate is insufficient, "
                               + "they must inform the court within 7 days of the date of this order.");
            assertThat(response.getData()).extracting("trialHearingTimeDJ").extracting("helpText2")
                .isEqualTo("Not more than seven nor less than three clear days before the trial, "
                               + "the claimant must file at court and serve an indexed and paginated bundle of "
                               + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                               + "and which complies with requirements of PD32. The parties must endeavour to agree "
                               + "the contents of the bundle before it is filed. The bundle will include a case "
                               + "summary and a chronology.");

            assertThat(response.getData()).extracting("trialOrderMadeWithoutHearingDJ").extracting("input")
                .isEqualTo(String.format("This order has been made without a hearing. Each party has the right to "
                                             + "apply to have this Order set aside or varied. Any such application "
                                             + "must be received by the Court (together with the appropriate fee) "
                                             + "by 4pm on %s.",
                                         date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))));
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

        @Test
        void shouldPopulateDynamicLists() {
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(UNSPEC_CLAIM)
                .atStateClaimDraft()
                .atStateClaimIssuedTrialHearing().build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            DynamicList hearingMethodValuesDisposalHearingDJ = getHearingMethodValuesDisposalHearingDJ(response);
            DynamicList hearingMethodValuesTrialHearingDJ = getHearingMethodValuesTrialHearingDJ(response);

            List<String> hearingMethodValuesDisposalHearingDJActual = hearingMethodValuesDisposalHearingDJ.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            List<String> hearingMethodValuesTrialHearingDJActual = hearingMethodValuesDisposalHearingDJ.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .collect(Collectors.toList());

            assertThat(hearingMethodValuesDisposalHearingDJActual).containsOnly("In Person");
            assertThat(hearingMethodValuesTrialHearingDJActual).containsOnly("In Person");
        }

        private DynamicList getHearingMethodValuesDisposalHearingDJ(AboutToStartOrSubmitCallbackResponse response) {
            CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
            System.out.println(responseCaseData);
            return responseCaseData.getHearingMethodValuesDisposalHearingDJ();
        }

        private DynamicList getHearingMethodValuesTrialHearingDJ(AboutToStartOrSubmitCallbackResponse response) {
            CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
            System.out.println(responseCaseData);
            return responseCaseData.getHearingMethodValuesTrialHearingDJ();
        }
    }

    @Nested
    class MidEventCreateOrderCallback {
        private static final String PAGE_ID = "create-order";

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedTrialSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedTrialDJInPersonHearingNew().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
        }

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedTrialSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedTrialSDOTelephoneHearingNew().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
        }

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedTrialSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedTrialSDOVideoHearingNew().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
        }

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedDisposalSDOInPerson() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalSDOInPerson().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
        }

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedDisposalSDOTelephoneCall() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalSDOTelephoneCall().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(defaultJudgmentOrderFormGenerator.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("orderSDODocumentDJ").isNotNull();
        }

        @Test
        void shouldCreateAndSaveSDOOrder_whenStateClaimIssuedDisposalSDOVideoCall() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalDJVideoCallNew().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
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

        @Test
        void shouldReturnCaseManagementListFromTrialHearing() {
            List<DynamicListElement> temporaryLocationList = List.of(
                DynamicListElement.builder().label("Loc 1").build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder().trialHearingMethodInPersonDJ(DynamicList.builder().listItems(temporaryLocationList)
                                                              .value(DynamicListElement.builder().label("Loc - 1 - 1")
                                                                         .build()).build()).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Loc").courtAddress("1").postcode("1")
                              .courtName("Court Name").region("Region").regionId("1").courtVenueId("000")
                              .epimmsId("123").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("caseManagementLocation").extracting("region")
                .isEqualTo(locations.get(0).getRegionId());
            assertThat(response.getData()).extracting("caseManagementLocation").extracting("baseLocation")
                .isEqualTo(locations.get(0).getEpimmsId());
        }

        @Test
        void shouldReturnCaseManagementListFromDisposalHearing() {
            List<DynamicListElement> temporaryLocationList = List.of(
                DynamicListElement.builder().label("Loc 1").build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder().disposalHearingMethodInPersonDJ(DynamicList.builder().listItems(temporaryLocationList)
                                                              .value(DynamicListElement.builder().label("Loc - 1 - 1")
                                                                         .build()).build()).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Loc").courtAddress("1").postcode("1")
                              .courtName("Court Name").region("Region").regionId("1").courtVenueId("000")
                              .epimmsId("123").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("caseManagementLocation").extracting("region")
                .isEqualTo(locations.get(0).getRegionId());
            assertThat(response.getData()).extracting("caseManagementLocation").extracting("baseLocation")
                .isEqualTo(locations.get(0).getEpimmsId());
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            CaseDocument testDocument = CaseDocument.builder()
                .createdBy("John")
                .documentName("document name")
                .documentSize(0L)
                .documentType(ACKNOWLEDGEMENT_OF_CLAIM)
                .createdDatetime(LocalDateTime.now())
                .documentLink(Document.builder()
                                  .documentUrl("fake-url")
                                  .documentFileName("file-name")
                                  .documentBinaryUrl("binary-url")
                                  .build())
                .build();
            List<Element<CaseDocument>> documentList = new ArrayList<>();
            documentList.add(element(testDocument));
            //Given
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .orderSDODocumentDJCollection(documentList)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            //Then
            assertThat(updatedData.getOrderSDODocumentDJCollection().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("sdo");
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

