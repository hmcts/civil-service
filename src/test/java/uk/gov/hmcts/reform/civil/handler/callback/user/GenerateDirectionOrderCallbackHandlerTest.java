package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionOrderCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class GenerateDirectionOrderCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateDirectionOrderCallbackHandler handler;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventPopulateOrderFields {
        private static final String PAGE_ID = "order";

        @Test
        void shouldPopulateFields_whenIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("assistedOrderCostsDefendantPaySub")).isNotNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnNoError_WhenSubmittedIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationBody()).isNull();
        }
    }
}
