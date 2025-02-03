package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.RequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(MockitoExtension.class)
class JudgeDecisionOnReconsiderationRequestCallbackHandlerTest extends BaseCallbackHandlerTest {

    private JudgeDecisionOnReconsiderationRequestCallbackHandler handler;

    private ObjectMapper mapper;

    @Mock
    private RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;

    @Mock
    private FeatureToggleService featureToggleService;

    private static final String CONFIRMATION_HEADER = "# Response has been submitted";
    private static final String CONFIRMATION_BODY_YES = "### Upholding previous order \n" +
        "A notification will be sent to the party applying for the request for reconsideration.";
    private static final String CONFIRMATION_BODY_CREATE_SDO = "### Amend previous order and create new SDO \n" +
        "A new SDO task has been created for this case and appears in 'Available tasks' on your dashboard. You will " +
        "need to go there to reselect the case to continue.";
    private static final String CONFIRMATION_BODY_CREATE_GENERAL_ORDER = "### Amend previous order and create a " +
        "general order" +
        " \n" +
        "To make a bespoke order in this claim, select 'General order' from the dropdown menu on the right of the " +
        "screen on your dashboard.";
    private static final String CONFIRMATION_BODY_CREATE_MAKE_AN_ORDER = "### Amend previous order and create a " +
        "general order" +
        " \n" +
        "To make a bespoke order in this claim, select 'Make an order' from the dropdown menu on the right of the " +
        "screen on your dashboard.";
    private static final String UPHOLDING_PREVIOUS_ORDER_REASON = "Having read the application for reconsideration of " +
        "the Legal Advisor's order dated %s and the court file \n 1.The application for reconsideration of the order " +
        "is dismissed.";

    private List<Element<CaseDocument>> sdoDocList;
    private static final CaseDocument document = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(DECISION_MADE_ON_APPLICATIONS)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(DECISION_ON_RECONSIDERATION_REQUEST);
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        AssignCategoryId assignCategoryId = new AssignCategoryId();
        handler = new JudgeDecisionOnReconsiderationRequestCallbackHandler(mapper, requestReconsiderationGeneratorService,
                                                                           assignCategoryId, featureToggleService
        );
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

            String reason = String.format(UPHOLDING_PREVIOUS_ORDER_REASON, formatLocalDateTime(LocalDateTime.now(), DATE));
            //Then: upholding reason should be set correctly
            assertThat(response.getData()).extracting("upholdingPreviousOrderReason")
                .extracting("reasonForReconsiderationTxtYes")
                .isEqualTo(reason);
        }
    }

    @Nested
    class MidCallback {
        @Test
        void shouldPopulateDecisionOnReconsiderationDoc() {
            //Given : Casedata
            String pageId = "generate-judge-decision-order";
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                                      .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.YES).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            when(requestReconsiderationGeneratorService.generate(any(CaseData.class), anyString())).thenReturn(document);
            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: should generate doc and start business process
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDecisionOnReconsiderationDocument()).isNotNull();
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

        @Test
        void shouldGenerateDocAndCallBusinessProcessIfDecisionUpheld() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(new ArrayList<>()).decisionOnReconsiderationDocument(document)
                .upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                  .reasonForReconsiderationTxtYes("Reason1").build())
                .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.YES).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: should generate doc and start business process
            assertThat(response.getData()).extracting("upholdingPreviousOrderReason")
                .extracting("reasonForReconsiderationTxtYes")
                .isEqualTo("Reason1");
            assertThat(response.getData()).extracting("decisionOnRequestReconsiderationOptions")
                .isEqualTo(DecisionOnRequestReconsiderationOptions.YES.name());
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isOne();
            assertThat(updatedData.getDecisionOnReconsiderationDocument()).isNull();
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(DECISION_ON_RECONSIDERATION_REQUEST.name(), "READY");
        }

        @Test
        void shouldNotGenerateDocAndCallBusinessProcessIfDecisionUpheld() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(null).decisionOnReconsiderationDocument(null)
                .upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                  .reasonForReconsiderationTxtYes("Reason1").build())
                .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_GENERAL_ORDER).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: should generate doc and start business process
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments()).isNull();
            assertThat(response.getData())
                .extracting("businessProcess").isNull();
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
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(
                    UpholdingPreviousOrderReason.builder()
                        .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.CREATE_GENERAL_ORDER).build();
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_CREATE_GENERAL_ORDER);
        }

        @Test
        void whenSubmittedWithCreateGeneralOrderCP_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder().systemGeneratedCaseDocuments(sdoDocList).upholdingPreviousOrderReason(
                    UpholdingPreviousOrderReason.builder()
                        .reasonForReconsiderationTxtYes("Reason1").build()).decisionOnRequestReconsiderationOptions(
                    DecisionOnRequestReconsiderationOptions.CREATE_GENERAL_ORDER).build();
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(CONFIRMATION_HEADER);
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY_CREATE_MAKE_AN_ORDER);
        }
    }

}
