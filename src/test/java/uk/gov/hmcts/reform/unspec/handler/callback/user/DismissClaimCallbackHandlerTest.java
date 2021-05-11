package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.Time;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    DismissClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
})
class DismissClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private DismissClaimCallbackHandler handler;

    @Nested
    class AboutToSubmit {

        private CallbackParams params;
        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = CallbackParamsBuilder.builder().of(CallbackType.ABOUT_TO_SUBMIT, caseData).build();
            localDateTime = LocalDateTime.now();
            when(time.now()).thenReturn(localDateTime);
        }

        @Test
        void shouldUpdateBusinessProcessToReadyWithEvent_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("businessProcess", Map.of(
                    "status", "READY",
                    "camundaEvent", "DISMISS_CLAIM"
                ))
                .containsEntry("claimDismissedDate", localDateTime.format(ISO_DATE_TIME));
        }
    }
}
