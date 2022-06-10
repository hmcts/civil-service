package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
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
    class MidEventPrePopulateDisposalHearingPageCallback {
        private static final String PAGE_ID = "order-details";

        @Test
        void shouldPrePopulateDisposalHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

            assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").isEqualTo(null);
            assertThat(response.getData()).extracting("disposalHearingPreferredTelephone").extracting("telephone")
                .isEqualTo("N/A");
            assertThat(response.getData()).extracting("disposalHearingPreferredEmail").extracting("email")
                .isEqualTo("N/A");
        }

        @Test
        void shouldPrePopulateDisposalHearingJudgementDeductionValueWhenDrawDirectionsOrderIsNotNull() {
            JudgementSum tempJudgementSum = JudgementSum.builder()
                .judgementSum(12)
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
                .isEqualTo("12%");
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
