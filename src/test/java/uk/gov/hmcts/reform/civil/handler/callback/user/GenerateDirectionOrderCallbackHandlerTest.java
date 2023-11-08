package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingNotes;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealChoiceSecondDropdown;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

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
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList.CLAIMANT_NOT_ATTENDING;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList.DEFENDANT_NOT_ATTENDING;

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

    @MockBean
    private DocumentHearingLocationHelper locationHelper;

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

    private static LocationRefData locationRefDataAfterSdo =   LocationRefData.builder().siteName("SiteName after Sdo")
        .courtAddress("1").postcode("1")
        .courtName("Court Name example").region("Region").regionId("2").courtVenueId("666")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    private static LocationRefData locationRefDataBeforeSdo =   LocationRefData.builder().siteName("SiteName before Sdo")
        .courtAddress("1").postcode("1")
        .courtName("Court Name Ccmc").region("Region").regionId("4").courtVenueId("000")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNullPreviousSubmittedEventSelections_whenInvokedFreeForm() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .freeFormRecordedTextArea("text")
                .freeFormOrderedTextArea("text")
                .orderOnCourtsList(OrderOnCourtsList.ORDER_ON_COURT_INITIATIVE)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("finalOrderSelection")).isNull();
            assertThat(response.getData().get("freeFormRecordedTextArea")).isNull();
            assertThat(response.getData().get("freeFormOrderedTextArea")).isNull();
            assertThat(response.getData().get("orderOnCourtsList")).isNull();
        }

        @Test
        void shouldNullPreviousSubmittedEventSelections_whenInvokedAssisted() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderMadeSelection(YES).finalOrderDateHeardComplex(OrderMade.builder().build())
                .finalOrderJudgePapers(List.of(FinalOrdersJudgePapers.CONSIDERED))
                .finalOrderJudgeHeardFrom(List.of(FinalOrderToggle.SHOW)).finalOrderRepresentation(FinalOrderRepresentation.builder().build())
                .finalOrderRecitals(List.of(FinalOrderToggle.SHOW)).finalOrderRecitalsRecorded(FinalOrderRecitalsRecorded.builder().text("text").build())
                .finalOrderOrderedThatText("text")
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW)).finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().build())
                .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).assistedOrderCostsReserved(AssistedOrderCostDetails.builder().build())
                .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder().build()).assistedOrderCostsBespoke(AssistedOrderCostDetails.builder().build())
                .finalOrderAppealToggle(List.of(FinalOrderToggle.SHOW)).finalOrderAppealComplex(FinalOrderAppeal.builder().build())
                .orderMadeOnDetailsList(OrderMadeOnTypes.WITHOUT_NOTICE)
                .finalOrderGiveReasonsComplex(AssistedOrderReasons.builder().reasonsText("text").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("finalOrderSelection")).isNull();
            assertThat(response.getData().get("finalOrderMadeSelection")).isNull();
            assertThat(response.getData().get("finalOrderDateHeardComplex")).isNull();
            assertThat(response.getData().get("finalOrderJudgePapers")).isNull();
            assertThat(response.getData().get("finalOrderJudgeHeardFrom")).isNull();
            assertThat(response.getData().get("finalOrderRepresentation")).isNull();
            assertThat(response.getData().get("finalOrderRecitals")).isNull();
            assertThat(response.getData().get("finalOrderRecitalsRecorded")).isNull();
            assertThat(response.getData().get("finalOrderOrderedThatText")).isNull();
            assertThat(response.getData().get("finalOrderFurtherHearingToggle")).isNull();
            assertThat(response.getData().get("finalOrderFurtherHearingComplex")).isNull();
            assertThat(response.getData().get("assistedOrderCostList")).isNull();
            assertThat(response.getData().get("assistedOrderCostsReserved")).isNull();
            assertThat(response.getData().get("assistedOrderMakeAnOrderForCosts")).isNull();
            assertThat(response.getData().get("assistedOrderCostsBespoke")).isNull();
            assertThat(response.getData().get("finalOrderAppealToggle")).isNull();
            assertThat(response.getData().get("finalOrderAppealComplex")).isNull();
            assertThat(response.getData().get("orderMadeOnDetailsList")).isNull();
            assertThat(response.getData().get("finalOrderGiveReasonsComplex")).isNull();
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
                .isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionTextArea")
                .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionDate")
                .isEqualTo(LocalDate.now().plusDays(7).toString());

        }

        @Test
        void shouldPopulateFields_whenIsCalledAfterSdo() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .ccdState(CASE_PROGRESSION)
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String advancedDate = LocalDate.now().plusDays(14).toString();
            when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefDataAfterSdo);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderCourt")
                .extracting("ownInitiativeText")
                .isEqualTo(ON_INITIATIVE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationClaimantOneDynamic")
                .isEqualTo("Mr. John Rambo");
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationDefendantOneDynamic")
                .isEqualTo("Mr. Sole Trader");
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationDefendantTwoDynamic")
                .isEqualTo("Mr. John Rambo");
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderCourt")
                .extracting("ownInitiativeDate")
                .isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderWithoutNotice")
                .extracting("withOutNoticeText")
                .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderWithoutNotice")
                .extracting("withOutNoticeDate")
                .isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("assistedOrderCostsFirstDropdownDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("assistedOrderAssessmentThirdDropdownDate")
                .isEqualTo(advancedDate);
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("makeAnOrderForCostsQOCSYesOrNo")
                .isEqualTo("No");
            assertThat(response.getData()).extracting("publicFundingCostsProtection")
                .isEqualTo("No");
            assertThat(response.getData()).extracting("finalOrderAppealComplex")
                .extracting("appealGrantedDropdown")
                .extracting("appealChoiceSecondDropdownA")
                .extracting("appealGrantedRefusedDate")
                .isEqualTo(LocalDate.now().plusDays(21).toString());
            assertThat(response.getData()).extracting("finalOrderAppealComplex")
                .extracting("appealGrantedDropdown")
                .extracting("appealChoiceSecondDropdownB")
                .extracting("appealGrantedRefusedDate")
                .isEqualTo(LocalDate.now().plusDays(21).toString());
            assertThat(response.getData()).extracting("finalOrderAppealComplex")
                .extracting("appealRefusedDropdown")
                .extracting("appealChoiceSecondDropdownA")
                .extracting("appealGrantedRefusedDate")
                .isEqualTo(LocalDate.now().plusDays(21).toString());
            assertThat(response.getData()).extracting("finalOrderAppealComplex")
                .extracting("appealRefusedDropdown")
                .extracting("appealChoiceSecondDropdownB")
                .extracting("appealGrantedRefusedDate")
                .isEqualTo(LocalDate.now().plusDays(21).toString());
            assertThat(response.getData()).extracting("finalOrderFurtherHearingComplex")
                .extracting("hearingLocationList").asString().contains("SiteName after Sdo");
        }

        @Test
        void shouldPopulateFields_whenIsCalledAfterSdoDiffSol() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(NO)
                .ccdState(CASE_PROGRESSION)
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String advancedDate = LocalDate.now().plusDays(14).toString();
            when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefDataAfterSdo);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationClaimantOneDynamic")
                .isEqualTo("Mr. John Rambo");
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationDefendantOneDynamic")
                .isEqualTo("Mr. Sole Trader");
            assertThat(response.getData()).extracting("finalOrderRepresentation")
                .extracting("typeRepresentationComplex")
                .extracting("typeRepresentationDefendantTwoDynamic")
                .isEqualTo("Mr. John Rambo");

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
            assertThat(response.getData()).extracting("finalOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAssistedOrder_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                        .singleDate(LocalDate.now().plusDays(2))
                                                                                        .build())
                                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then    ** Modify when Assisted Order Document Generation is developed
            assertThat(response.getData()).extracting("finalOrderDocument").isNotNull();
        }

        @ParameterizedTest
        @MethodSource("invalidAssistedOrderDates")
        void validateAssistedOrderInvalidDates(CaseData caseData, String expectedErrorMessage) {
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, MID, PAGE_ID));
            // Then
            assertThat(response.getErrors().get(0)).isEqualTo(expectedErrorMessage);
        }

        @ParameterizedTest
        @MethodSource("validAssistedOrderDates")
        void validateAssistedOrderValidDates(CaseData caseData) {
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, MID, PAGE_ID));
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        static Stream<Arguments> validAssistedOrderDates() {
            return Stream.of(
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                                .singleDate(LocalDate.now().minusDays(2))
                                                                                                .build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                               .dateRangeFrom(LocalDate.now().minusDays(5))
                                                                                               .dateRangeTo(LocalDate.now().minusDays(4))
                                                                                               .build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                             .datesToAvoidDateDropdown(DatesFinalOrders.builder()
                                                                                           .datesToAvoidDates(LocalDate.now().plusDays(2))
                                                                                           .build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder().build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                              .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(14))
                                                              .build())
                        .build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                              .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(14))
                                                              .build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder().build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                                       .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                                        .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                                                        .build()).build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                                       .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                                        .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                                                        .build()).build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                                .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                                 .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                                                 .build()).build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                                .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                                 .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                                                 .build()).build()).build()).build()
                )
            );
        }

        static Stream<Arguments> invalidAssistedOrderDates() {
            return Stream.of(
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                                .singleDate(LocalDate.now().plusDays(2))
                                                                                                .build()).build()).build(),
                    "The date in Order made may not be later than the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                               .dateRangeFrom(LocalDate.now().plusDays(2))
                                                                                               .dateRangeTo(LocalDate.now().minusDays(4))
                                                                                               .build()).build()).build(),
                    "The date in Order made 'date from' may not be later than the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                                                                               .dateRangeTo(LocalDate.now().plusDays(2))
                                                                                               .build()).build()).build(),
                    "The date in Order made 'date to' may not be later than the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                               .dateRangeFrom(LocalDate.now().minusDays(20))
                                                                                               .dateRangeTo(LocalDate.now().minusDays(30))
                                                                                               .build()).build()).build(),
                    "The date range in Order made may not have a 'from date', that is after the 'date to'"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                             .datesToAvoidDateDropdown(DatesFinalOrders.builder()
                                                                                           .datesToAvoidDates(LocalDate.now().minusDays(2))
                                                                                           .build()).build()).build(),
                    "The date in Further hearing may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                              .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(2))
                                                              .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(14))
                                                              .build()).build(),
                    "The date in Make an order for detailed/summary costs may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                              .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(14))
                                                              .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().minusDays(2))
                                                              .build()).build(),
                    "The date in Make an order for detailed/summary costs may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                                       .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                                        .appealGrantedRefusedDate(LocalDate.now().minusDays(1))
                                                                                                                        .build()).build()).build()).build(),
                    "The date in Appeal notice date may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                                       .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                                        .appealGrantedRefusedDate(LocalDate.now().minusDays(1))
                                                                                                                        .build()).build()).build()).build(),
                    "The date in Appeal notice date may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                                .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                                 .appealGrantedRefusedDate(LocalDate.now().minusDays(1))
                                                                                                                 .build()).build()).build()).build(),
                    "The date in Appeal notice date may not be before the established date"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                                     .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                                .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                                 .appealGrantedRefusedDate(LocalDate.now().minusDays(1))
                                                                                                                 .build()).build()).build()).build(),
                    "The date in Appeal notice date may not be before the established date"
                ),
                Arguments.of(
                        CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                                .finalOrderFurtherHearingComplex(
                                        FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                                                .build()).build(),
                        "Further hearing, Length of new hearing, Other is empty"
                )
            );
        }

        @ParameterizedTest
        @MethodSource("validJudgeHeardFrom")
        void validateJudgeHeardFromSection_shouldReturnNoError(CaseData caseData) {
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, MID, PAGE_ID));
            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        static Stream<Arguments> validJudgeHeardFrom() {
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            return Stream.of(
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .addRespondent2(YES)
                        .respondent2(PartyBuilder.builder().individual().build())
                        .respondent2SameLegalRepresentative(NO)
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationDefendantTwoDynamic("defendant two")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantTwoList(DEFENDANT_NOT_ATTENDING)
                                                                                     .build()).build()).build()
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .addApplicant2(YES)
                        .applicant2(PartyBuilder.builder().individual().build())
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantTwoDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .typeRepresentationClaimantListTwo(CLAIMANT_NOT_ATTENDING)
                                                                                     .build()).build()).build()
                )
            );
        }

        @ParameterizedTest
        @MethodSource("invalidJudgeHeardFrom")
        void validateJudgeHeardFromSection_shouldReturnErrors(CaseData caseData, String expectedErrorMessage) {
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, MID, PAGE_ID));
            // Then
            assertThat(response.getErrors().get(0)).isEqualTo(expectedErrorMessage);;
        }

        static Stream<Arguments> invalidJudgeHeardFrom() {
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            return Stream.of(
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(null)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .build()).build()).build(),
                    "Judge Heard from: 'Claimant(s) and defendant(s)' section for claimant, requires a selection to be made"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantList(null)
                                                                                     .build()).build()).build(),
                    "Judge Heard from: 'Claimant(s) and defendant(s)' section for defendant, requires a selection to be made"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .addRespondent2(YES)
                        .respondent2(PartyBuilder.builder().individual().build())
                        .respondent2SameLegalRepresentative(NO)
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationDefendantTwoDynamic("defendant two")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .typeRepresentationDefendantTwoList(null)
                                                                                     .build()).build()).build(),
                    "Judge Heard from: 'Claimant(s) and defendant(s)' section for second defendant, requires a selection to be made"
                ),
                Arguments.of(
                    CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                        .addApplicant2(YES)
                        .applicant2(PartyBuilder.builder().individual().build())
                        .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                        .finalOrderJudgeHeardFrom(toggle)
                        .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                                      .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                                      .typeRepresentationComplex(ClaimantAndDefendantHeard.builder()
                                                                                     .typeRepresentationDefendantOneDynamic("defendant one")
                                                                                     .typeRepresentationClaimantOneDynamic("claimant one")
                                                                                     .typeRepresentationClaimantTwoDynamic("claimant one")
                                                                                     .typeRepresentationClaimantList(CLAIMANT_NOT_ATTENDING)
                                                                                     .typeRepresentationClaimantListTwo(null)
                                                                                     .typeRepresentationDefendantList(DEFENDANT_NOT_ATTENDING)
                                                                                     .build()).build()).build(),
                    "Judge Heard from: 'Claimant(s) and defendant(s)' section for second claimant, requires a selection to be made"
                )
            );
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
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("finalOrders");
        }

        @Test
        void shouldChangeStateToFinalOrder_onAboutToSubmitAndFreeFormOrder() {
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo("All_FINAL_ORDERS_ISSUED");
        }

        @Test
        void shouldChangeStateToFinalOrder_onAboutToSubmitAndAssistedOrderAndNoFurtherHearing() {
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(null)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo("All_FINAL_ORDERS_ISSUED");
        }

        @Test
        void shouldChangeStateToCaseProgression_onAboutToSubmitAndAssistedOrderWithFurtherHearing() {
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo("CASE_PROGRESSION");
        }

        @Test
        void shouldRePopulateHearingNotes_whenAssistedHearingNotesExist() {
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .finalOrderDocument(finalOrder)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                     .hearingNotesText("test text hearing notes assisted order").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("test text hearing notes assisted order");
        }

        @Test
        void shouldRePopulateHearingNotes_whenFreeFormHearingNotesExist() {
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .freeFormHearingNotes("test text hearing notes free form order")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("test text hearing notes free form order");
        }

        @Test
        void shouldNotRePopulateHearingNotes_whenAssistedHearingNotesDoNotExist() {
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .finalOrderDocument(finalOrder)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("preexisting hearing notes");
        }

        @Test
        void shouldNotRePopulateHearingNotes_whenFreeFormHearingNotesDoNotExist() {
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("preexisting hearing notes");
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
