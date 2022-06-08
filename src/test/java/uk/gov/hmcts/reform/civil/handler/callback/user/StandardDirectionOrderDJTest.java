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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
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

        private static final String PAGE_ID = "disposal-screen";

        @Test
        void shouldPrePopulateDJDisposalHearingPage() {
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

        }
    }

}
