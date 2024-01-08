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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RequestForReconsiderationCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class RequestForReconsiderationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RequestForReconsiderationCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
        "You should receive an update on your request for determination after 10 days, please monitor" +
        " your notifications/dashboard for an update.";

    private static final String ERROR_MESSAGE_DEADLINE_EXPIRED
        = "You can no longer request a reconsideration because the deadline has expired";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(REQUEST_FOR_RECONSIDERATION);
    }

    @Nested
    class AboutToStartCallback {
        @Test
        void shouldAllowRequestIfLessThan7DaysElapsed() {
            //Given : Casedata containing an SDO order created 6 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                  .element(CaseDocument.builder()
                                                               .documentType(DocumentType.SDO_ORDER)
                                                               .createdDatetime(LocalDateTime.now().minusDays(6))
                                                               .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors().isEmpty());
        }

        @Test
        void shouldAllowRequestIfLessThan7DaysElapsedForLatestSDO() {
            //Given : Casedata containing two SDO order and latest created 6 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .systemGeneratedCaseDocuments(Arrays.asList(
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(10))
                                             .build()),
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(6))
                                             .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors().isEmpty());
        }

        @Test
        void shouldSendErrorMessageIf7DaysElapsedForLatestSDO() {
            //Given : Casedata containing two SDO order and latest created 7 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .systemGeneratedCaseDocuments(Arrays.asList(
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(10))
                                             .build()),
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(7))
                                             .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: The error should be displayed
            assertThat(response.getErrors().contains(ERROR_MESSAGE_DEADLINE_EXPIRED));
        }

        @Test
        void shouldSendErrorMessageIf7DaysElapsed() {
            //Given : Casedata containing an SDO order created 7 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                          .element(CaseDocument.builder()
                                                                       .documentType(DocumentType.SDO_ORDER)
                                                                       .createdDatetime(LocalDateTime.now().minusDays(7))
                                                                       .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: The error should be displayed
            assertThat(response.getErrors().contains(ERROR_MESSAGE_DEADLINE_EXPIRED));
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateSetAsideOrderDate() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsideration(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsideration")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo("# Your request has been submitted");
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY);
        }
    }

}
