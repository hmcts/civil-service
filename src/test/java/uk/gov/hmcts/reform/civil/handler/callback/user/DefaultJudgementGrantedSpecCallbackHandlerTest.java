package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_GRANTED_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DefaultJudgementGrantedSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    private static final String REPAYMENT_SUMMARY_OBJECT = "The judgment will order the defendant to pay £1172.00, "
        + "including the claim fee and interest, if applicable, as shown:\n### Claim amount \n £1000.00\n "
        + "### Fixed cost amount \n£102.00\n### Claim fee amount \n £70.00\n ## Subtotal \n £1172.00";

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    private DefaultJudgementGrantedSpecCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DefaultJudgementGrantedSpecCallbackHandler(objectMapper, new InterestCalculator());
    }

    @Test
    void shouldReturnHandledEvents() {
        assertThat(handler.handledEvents()).containsExactly(DEFAULT_JUDGEMENT_GRANTED_SPEC);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetJudgmentStateAndBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.JUDGMENT_REQUESTED)
                .activeJudgment(new JudgmentDetails())
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .repaymentSummaryObject(REPAYMENT_SUMMARY_OBJECT)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getActiveJudgment().getState()).isEqualTo(JudgmentState.ISSUED);
            assertThat(updatedData.getActiveJudgment().getIssueDate()).isEqualTo(LocalDate.now());
            assertThat(updatedData.getActiveJudgment().getRtlState()).isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(updatedData.getActiveJudgment().getIsRegisterWithRTL()).isEqualTo(YES);
            assertThat(updatedData.getActiveJudgment().getOrderedAmount()).isEqualTo("100000");
            assertThat(updatedData.getActiveJudgment().getClaimFeeAmount()).isEqualTo("7000");
            assertThat(updatedData.getActiveJudgment().getCosts()).isEqualTo("10200");
            assertThat(updatedData.getActiveJudgment().getTotalAmount()).isEqualTo("117200");
            assertThat(updatedData.getJoIsLiveJudgmentExists()).isEqualTo(YES);
            assertThat(updatedData.getJoRepaymentSummaryObject())
                .contains("The judgment will order the defendant to pay £1172.00")
                .contains("### Claim amount")
                .contains("£1000.00")
                .contains("### Fixed cost amount")
                .contains("£102.00")
                .contains("### Claim fee amount")
                .contains("£70.00")
                .contains("## Total still owed");
            assertThat(updatedData.getRepaymentSummaryObject()).isEqualTo(updatedData.getJoRepaymentSummaryObject());
            assertThat(updatedData.getTotalInterest()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC.name());
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }

        @Test
        void shouldReturnError_whenCaseStateIsNotJudgmentRequested() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.CASE_ISSUED)
                .activeJudgment(new JudgmentDetails())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Cannot grant default judgment for case in state %s for caseId %d",
                caseData.getCcdState(),
                caseData.getCcdCaseReference()
            ));
        }

        @Test
        void shouldReturnError_whenActiveJudgmentIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.JUDGMENT_REQUESTED)
                .activeJudgment(null)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Active judgment is null for caseId %d",
                caseData.getCcdCaseReference()
            ));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .build());
        }
    }
}
