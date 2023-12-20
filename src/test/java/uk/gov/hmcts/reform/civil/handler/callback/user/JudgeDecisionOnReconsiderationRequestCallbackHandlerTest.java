package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpholdingPreviousOrderReason;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    JudgeDecisionOnReconsiderationRequestCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class JudgeDecisionOnReconsiderationRequestCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private JudgeDecisionOnReconsiderationRequestCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CONFIRMATION_HEADER = "# Response has been submitted";
    private static final String CONFIRMATION_BODY_YES = "### Upholding previous order \n" +
        "A notification will be sent to the party applying for the request for reconsideration.";
    private static final String CONFIRMATION_BODY_CREATE_SDO = "### Amend previous order and create new SDO \n" +
        "A new SDO task has been created for this case and appears in 'Available tasks' on your dashboard. You will " +
        "need to go there to reselect the case to continue.";
    private static final String CONFIRMATION_BODY_CREATE_GENERAL_ORDER = "### Amend previous order and create a " +
        "general order" +
        " \n" +
        "To make a bespoke order in this claim,select 'General order' from the dropdown menu on the right of the " +
        "screen on your dashboard.";
    private static final String upholdingPreviousOrderReason = "Having read the application for reconsideration of " +
        "the Legal Advisor's order dated %s and the court file \n 1.The application for reconsideration of the order " +
        "is dismissed.";

    private List<Element<CaseDocument>> sdoDocList;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DECISION_ON_RECONSIDERATION_REQUEST);
    }

    @BeforeEach
    void setUp() {
        sdoDocList = new ArrayList<>();
        CaseDocument sdoDoc =
            CaseDocument.builder().documentType(DocumentType.SDO_ORDER).documentLink(Document.builder().documentUrl(
                "test").build()).createdDatetime(LocalDateTime.now().minusDays(10)).build();
        CaseDocument sdoDoc2 =
            CaseDocument.builder().documentType(DocumentType.SDO_ORDER).documentLink(Document.builder().documentUrl(
                "test").build()).createdDatetime(LocalDateTime.now()).build();
        sdoDocList.add(ElementUtils.element(sdoDoc));
        sdoDocList.add(ElementUtils.element(sdoDoc2));
    }

    @Nested
    class AboutToStartCallback {
        @Test
        void shouldPopulateUpholdingPreviousOrderReason() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            String reason = String.format(upholdingPreviousOrderReason, formatLocalDateTime(LocalDateTime.now(), DATE));
            //Then: upholding reason should be set correctly
            assertThat(response.getData()).extracting("upholdingPreviousOrderReason")
                .extracting("reasonForReconsiderationTxtYes")
                .isEqualTo(reason);
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateDecisionOnReconsiderationDetails() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                                      .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("upholdingPreviousOrderReason")
                .extracting("reasonForReconsiderationTxtYes")
                .isEqualTo("Reason1");
            assertThat(response.getData()).extracting("decisionOnRequestReconsiderationOptions")
                .isEqualTo(DecisionOnRequestReconsiderationOptions.YES.name());
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmittedWithYes_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                                      .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.YES).build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_YES);
        }

        @Test
        void whenSubmittedWithCreateSDO_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                                      .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.CREATE_SDO).build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_CREATE_SDO);
        }

        @Test
        void whenSubmittedWithCreateGeneralOrder_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                                      .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.CREATE_GENERAL_ORDER).build();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_CREATE_GENERAL_ORDER);
        }
    }

}
