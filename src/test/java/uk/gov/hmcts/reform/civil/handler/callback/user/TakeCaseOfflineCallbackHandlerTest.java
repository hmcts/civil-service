package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    TakeCaseOfflineCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class TakeCaseOfflineCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private TakeCaseOfflineCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime now;

        @BeforeEach
        void setup() {
            now = LocalDateTime.now();
            when(time.now()).thenReturn(now);
        }

        @Test
        void shouldReturnAddTakenOfflineDate_WhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("takenOfflineDate", now.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(TAKE_CASE_OFFLINE.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }
    }
}
