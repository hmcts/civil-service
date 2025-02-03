package uk.gov.hmcts.reform.civil.handler.callback.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_SETTLEMENT_AGREEMENT_DEADLINE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SetSettlementAgreementDeadlineCallbackHandlerTest extends BaseCallbackHandlerTest {

    private SetSettlementAgreementDeadlineCallbackHandler handler;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new SetSettlementAgreementDeadlineCallbackHandler(deadlinesCalculator, objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SET_SETTLEMENT_AGREEMENT_DEADLINE);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetSettlementAgreementDeadline() {
            // Given
            LocalDateTime expectedDateTime = LocalDateTime.parse("2024-01-22T13:12:34");
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                    .applicant1LiPResponse(ClaimantLiPResponse.builder()
                        .applicant1SignedSettlementAgreement(YesOrNo.YES).build()
                    )
                    .build())
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.getRespondToSettlementAgreementDeadline(any())).thenReturn(expectedDateTime);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1RespondToSettlementAgreementDeadline")
                .isEqualTo(Arrays.asList(2024, 1, 22, 13, 12, 34));
        }

        @Test
        void shouldNotSetSettlementAgreementDeadlineIfNotBilingual() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                    .applicant1LiPResponse(ClaimantLiPResponse.builder()
                        .applicant1SignedSettlementAgreement(YesOrNo.YES).build()
                    )
                    .build())
                .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1RespondToSettlementAgreementDeadline")
                .isEqualTo(null);
        }

        @Test
        void shouldNotSetSettlementAgreementDeadlineIfClaimantNotSigned() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1RespondToSettlementAgreementDeadline")
                .isEqualTo(null);
        }

        @Test
        void shouldSetSettlementAgreementDeadline_WhenCourtDecisionInFavourClaimantImmediateRepayment() {
            // Given
            LocalDateTime expectedDateTime = LocalDateTime.parse("2024-05-20T13:12:34");
            CaseData caseData = CaseDataBuilder.builder()
                    .build().toBuilder()
                    .caseDataLiP(CaseDataLiP.builder()
                            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                    .applicant1SignedSettlementAgreement(YesOrNo.YES)
                                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                                    .build()
                            )
                            .build())
                    .claimantBilingualLanguagePreference(Language.BOTH.toString())
                    .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(deadlinesCalculator.getRespondentToImmediateSettlementAgreement(any())).thenReturn(expectedDateTime);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                    .extracting("respondent1RespondToSettlementAgreementDeadline")
                    .isNotNull();
        }
    }
}
