package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DismissClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private Time time;
    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();
    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private DismissClaimCallbackHandler handler;

    @Nested
    class AboutToSubmit {

        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            localDateTime = LocalDateTime.now();
            when(time.now()).thenReturn(localDateTime);
        }

        @Test
        void shouldUpdateBusinessProcessToReadyWithEvent_whenInvoked() {
            when(toggleService.isJudgmentBufferEnabled()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsOnly("READY", "DISMISS_CLAIM");

            assertThat(response.getData())
                .containsEntry("claimDismissedDate", localDateTime.format(ISO_DATE_TIME));
        }

        @Test
        void shouldClearJoCaseDataWhenJudgmentBufferEnabledAndJoRequested() {
            when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setIsJoRequested(YES);
            caseData.setJoRepaymentSummaryObject("summary");
            caseData.setJoIsDisplayInJudgmentTab(YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("joRepaymentSummaryObject", "joIsDisplayInJudgmentTab", "isJoRequested")
                .containsOnly(null, null, null);
        }

        @Test
        void shouldNotClearJoCaseDataWhenJoNotRequested() {
            when(toggleService.isJudgmentBufferEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setIsJoRequested(NO);
            caseData.setJoRepaymentSummaryObject("summary");
            caseData.setJoIsDisplayInJudgmentTab(YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("joRepaymentSummaryObject", "joIsDisplayInJudgmentTab")
                .containsOnly("summary", "Yes");
        }

        @Test
        void shouldNotClearJoCaseDataWhenJudgmentBufferDisabled() {
            when(toggleService.isJudgmentBufferEnabled()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setIsJoRequested(YES);
            caseData.setJoRepaymentSummaryObject("summary");
            caseData.setJoIsDisplayInJudgmentTab(YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("joRepaymentSummaryObject", "joIsDisplayInJudgmentTab")
                .containsOnly("summary", "Yes");
        }
    }
}
