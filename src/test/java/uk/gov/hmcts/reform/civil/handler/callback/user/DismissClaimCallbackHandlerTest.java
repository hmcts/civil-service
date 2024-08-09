package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DismissClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private Time time;

    private DismissClaimCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        handler = new DismissClaimCallbackHandler(objectMapper, time, caseDetailsConverter);
    }

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
                .extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsOnly("READY", "DISMISS_CLAIM");

            assertThat(response.getData())
                .containsEntry("claimDismissedDate", localDateTime.format(ISO_DATE_TIME));
        }
    }
}
