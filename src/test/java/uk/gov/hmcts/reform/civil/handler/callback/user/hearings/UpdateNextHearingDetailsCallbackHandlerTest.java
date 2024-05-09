package uk.gov.hmcts.reform.civil.handler.callback.user.hearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;

@SpringBootTest(classes = {
    UpdateNextHearingDetailsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class UpdateNextHearingDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UpdateNextHearingDetailsCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @Nested
    class AboutToStart {

        @Nested
        class UpdateNextHearingDetails {
            @Test
            void shouldSetNextHearingDetails() {
                CaseData caseData = CaseDataBuilder.builder().build();
                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_INFO,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID("HER12345")
                    .hearingDateTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0)).build();
                assertEquals(expected, updatedData.getNextHearingDetails());
            }
        }

        @Nested
        class UPDATE_NEXT_HEARING_INFO {
            @Test
            void shouldSetNextHearingDetails() {
                CaseData caseData = CaseDataBuilder.builder().build();
                CallbackParams params = callbackParamsOf(
                    caseData,
                    CaseEvent.UPDATE_NEXT_HEARING_INFO,
                    ABOUT_TO_START
                );

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                NextHearingDetails expected = NextHearingDetails.builder()
                    .hearingID("HER12345")
                    .hearingDateTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0)).build();
                assertEquals(expected, updatedData.getNextHearingDetails());
            }
        }

        @Test
        void shouldSetNextHearingDetails() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            NextHearingDetails expected = NextHearingDetails.builder()
                .hearingID("HER12345")
                .hearingDateTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0)).build();
            assertEquals(expected, updatedData.getNextHearingDetails());
        }
    }
}
