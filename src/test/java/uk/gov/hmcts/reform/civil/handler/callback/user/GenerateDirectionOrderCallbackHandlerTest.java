package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_1v1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_1v2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_2v1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.HEADER;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionOrderCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class GenerateDirectionOrderCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateDirectionOrderCallbackHandler handler;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private JudgeFinalOrderGenerator judgeFinalOrderGenerator;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ON_INITIATIVE_SELECTION_TEXT = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";

    @MockBean
    private LocationRefDataService locationRefDataService;
    public static final CaseDocument finalOrder = CaseDocument.builder()
        .createdBy("Test")
        .documentName("document test name")
        .documentSize(0L)
        .documentType(JUDGE_FINAL_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

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
        private static final String PAGE_ID = "populate-form-values";

        @Test
        void shouldPopulateFreeFormOrderValues_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting(
                    "onInitiativeSelectionTextArea")
                .isEqualTo(ON_INITIATIVE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionDate")
                .isEqualTo(LocalDate.now().toString());
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionTextArea")
                .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionDate")
                .isEqualTo(LocalDate.now().toString());

        }

        @Test
        void shouldPopulateFields_whenIsCalled() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String advancedDate = LocalDate.now().plusDays(14).toString();
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("assistedOrderCostsDefendantPaySub")
                .extracting("defendantCostStandardDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("assistedOrderCostsClaimantPaySub")
                .extracting("claimantCostStandardDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("assistedOrderCostsDefendantSum")
                .extracting("defendantCostSummarilyDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("assistedOrderCostsClaimantSum")
                .extracting("claimantCostSummarilyDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderCourt")
                .extracting("ownInitiativeText")
                .isEqualTo(ON_INITIATIVE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderCourt")
                .extracting("ownInitiativeDate")
                .isEqualTo(LocalDate.now().toString());
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderWithoutNotice")
                .extracting("withOutNoticeText")
                .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderWithoutNotice")
                .extracting("withOutNoticeDate")
                .isEqualTo(LocalDate.now().toString());
            assertThat(response.getData()).extracting("finalOrderAppealComplex")
                .extracting("appealGranted")
                .extracting("appealDate")
                .isEqualTo(LocalDate.now().plusDays(21).toString());
        }
    }

    @Nested
    class MidEventValidateAndGenerateOrderDocumentPreview {
        private static final String PAGE_ID = "validate-and-generate-document";

        @Test
        void shouldGenerateFreeFormOrder_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("freeFormOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAssistedOrder_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then    ** Modify when Assisted Order Document Generation is developed
            //assertThat(response.getData()).extracting("assistedOrderDocument").isNotNull();
        }

        @Test
        void shouldValidateAssistedOrderDate_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDateHeardComplex(OrderMade.builder().date(LocalDate.now().minusDays(2)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors())
                .containsExactly("The date in Order Made may not be earlier than the established date");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldAddDocumentToCollection_onAboutToSubmit() {
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID().equals("finalOrders"));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void should1v1Text_WhenSubmittedAndCase1v1() {
            // Given
            String confirmationHeader = format(HEADER, 1234);
            String confirmationBody = format(BODY_1v1, "Mr. John Rambo", "Mr. Sole Trader");

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .ccdCaseReference(1234L)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(confirmationHeader)
                                                                          .confirmationBody(confirmationBody)
                                                                          .build());
        }

        @Test
        void should1v2Text_WhenSubmittedAndCase1v2() {
            // Given
            String confirmationHeader = format(HEADER, 1234);
            String confirmationBody = format(BODY_1v2, "Mr. John Rambo", "Mr. Sole Trader", "Mr. John Rambo");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build().toBuilder()
                .ccdCaseReference(1234L)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(confirmationHeader)
                                                                          .confirmationBody(confirmationBody)
                                                                          .build());
        }

        @Test
        void should2v1Text_WhenSubmittedAndCase2v1() {
            // Given
            String confirmationHeader = format(HEADER, 1234);
            String confirmationBody = format(BODY_2v1, "Mr. John Rambo", "Mr. Jason Rambo", "Mr. Sole Trader");
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build().toBuilder()
                .ccdCaseReference(1234L)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(confirmationHeader)
                                                                          .confirmationBody(confirmationBody)
                                                                          .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(GENERATE_DIRECTIONS_ORDER);
    }
}
