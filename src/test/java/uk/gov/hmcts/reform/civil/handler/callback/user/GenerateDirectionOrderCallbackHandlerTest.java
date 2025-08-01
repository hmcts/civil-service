package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealChoiceSecondDropdown;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.CaseHearingLengthElement;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrdersComplexityBand;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDate;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDateType;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.BLANK_TEMPLATE_AFTER_HEARING;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.BLANK_TEMPLATE_BEFORE_HEARING;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.FIX_DATE_CCMC;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.FIX_DATE_CMC;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList.CLAIMANT_NOT_ATTENDING;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList.DEFENDANT_NOT_ATTENDING;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_1_V_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_1_V_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.BODY_2_V_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.DATE_FROM_AFTER_DATE_TO_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.FURTHER_HEARING_OTHER_ALT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.FUTURE_DATE_FROM_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.FUTURE_DATE_TO_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.FUTURE_SINGLE_DATE_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.GenerateDirectionOrderCallbackHandler.HEADER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class GenerateDirectionOrderCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private GenerateDirectionOrderCallbackHandler handler;

    @Mock
    private JudgeFinalOrderGenerator judgeFinalOrderGenerator;

    @Mock
    private JudgeOrderDownloadGenerator judgeOrderDownloadGenerator;

    @Mock
    private UserService theUserService;

    @Mock
    private DocumentHearingLocationHelper locationHelper;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    private ObjectMapper mapper;

    private static final String ON_INITIATIVE_SELECTION_TEXT = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";
    public static final String NOT_ALLOWED_FOR_CITIZEN = "This claim involves a LiP. To allocate to Small Claims or Fast Track you must use the"
        + " Standard Direction Order (SDO) otherwise use Not suitable for SDO.";
    public static final String NOT_ALLOWED_FOR_TRACK = "The Make an order event is not available for Small Claims and Fast Track cases until the track has"
        + " been allocated. You must use the Standard Direction Order (SDO) to proceed.";

    @Mock
    private LocationReferenceDataService locationRefDataService;
    public static final CaseDocument finalOrder = CaseDocument.builder()
        .createdBy("Test")
        .documentName("document test name")
        .documentSize(0L)
        .documentType(JUDGE_FINAL_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name.pdf")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final Document uploadedDocument = Document.builder()
        .documentFileName("file-name.docx")
        .uploadTimestamp((LocalDateTime.now()).toString())
        .documentUrl("fake-url")
        .build();

    private static final LocationRefData locationRefDataAfterSdo =   LocationRefData.builder().siteName("SiteName after Sdo")
        .courtAddress("1").postcode("1")
        .courtName("Court Name example").region("Region").regionId("2").courtVenueId("666")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    private static final DynamicList FAST_INT_OPTIONS = fromList(List.of(
        BLANK_TEMPLATE_AFTER_HEARING.getLabel(), BLANK_TEMPLATE_BEFORE_HEARING.getLabel(), FIX_DATE_CMC.getLabel()));

    private static final DynamicList MULTI_OPTIONS = fromList(List.of(
        BLANK_TEMPLATE_AFTER_HEARING.getLabel(), BLANK_TEMPLATE_BEFORE_HEARING.getLabel(),
        FIX_DATE_CCMC.getLabel(), FIX_DATE_CMC.getLabel()));

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        handler = new GenerateDirectionOrderCallbackHandler(locationRefDataService, mapper, judgeFinalOrderGenerator, judgeOrderDownloadGenerator,
                                                            locationHelper, theUserService, workingDayIndicator,
                                                            featureToggleService,
                                                            Optional.of(updateWaCourtLocationsService)
        );
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSdoDisposal()
                .build().toBuilder()
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithClaimantLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1Represented(NO).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactlyInAnyOrder(NOT_ALLOWED_FOR_CITIZEN);
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithRespondentLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .applicant1Represented(NO).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactlyInAnyOrder(NOT_ALLOWED_FOR_CITIZEN);
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedNotInJudicialReferral() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .applicant1Represented(NO)
                .build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("No");
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedWhenNoLips() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("Yes");
            assertThat(response.getData().get("finalOrderTrackToggle")).isNull();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedMintiNotJudicialReferralNotMintiTrack() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .applicant1Represented(NO)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("No");
        }

        @Test
        void shouldNotReturnError_WhenMintiNotEnabledMintiNotJudicialReferralNotMintiTrack() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("No");
        }

        @Test
        void shouldNotReturnError_WhenMintiEnabledNoJudicialReferralApplicantLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .applicant1Represented(NO)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("No");
        }

        @Test
        void shouldNotReturnError_WhenMintiEnabledNoJudicialReferralNoLips() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("No");
        }

        @Test
        void shouldShowTrackAllocationPage_WhenMultiTrack() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("allowOrderTrackAllocation")).isEqualTo("Yes");
            assertThat(response.getData().get("finalOrderTrackToggle")).isNull();
        }

        @Test
        void shouldNullPreviousSubmittedEventSelections_whenInvokedDownloadOrderTemplate() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                     .value(DynamicListElement.builder()
                                                .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                .build()).build()).build();
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("finalOrderSelection")).isNull();
            assertThat(response.getData().get("finalOrderTrackAllocation")).isNull();
            assertThat(response.getData().get("finalOrderAllocateToTrack")).isNull();
            assertThat(response.getData().get("finalOrderIntermediateTrackComplexityBand")).isNull();
            assertThat(response.getData().get("templateOptions")).isNull();
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
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

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedWhenNoLipsAndFlagEnabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .claimantBilingualLanguagePreference("BOTH")
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("bilingualHint")).isEqualTo("Yes");
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedWhenLipsAndFlagEnabledAndRespondentLangExists() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .claimantBilingualLanguagePreference("ENGLISH")
                .caseDataLiP(CaseDataLiP.builder()
                            .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                        .respondent1ResponseLanguage("BOTH").build()).build())
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("bilingualHint")).isEqualTo("Yes");
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedWhenLipsAndFlagEnabledAndClaimantRespondentLangExists() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .claimantBilingualLanguagePreference("BOTH")
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("BOTH").build()).build())
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("bilingualHint")).isEqualTo("Yes");
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedWhenLipsAndFlagEnabledAndClaimantRespondentLangNotExists() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderTrackAllocation(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .claimantBilingualLanguagePreference("ENGLISH")
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("ENGLISH").build()).build())
                .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                               .assignComplexityBand(YES)
                                                               .band(ComplexityBand.BAND_1)
                                                               .build())
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, ABOUT_TO_START, JUDICIAL_REFERRAL);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("bilingualHint")).isNull();
        }
    }

    @Nested
    class MidEventPopulateOrderFields {
        private static final String PAGE_ID = "populate-form-values";

        @Test
        void shouldPopulateFreeFormOrderValues_onMidEventCallback() {
            // Given
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(7));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder().finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER).build();
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
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class)))
                .thenReturn(LocalDate.now())//singleDateSelection.singleDate
                .thenReturn(LocalDate.now().plusDays(7))//datesToAvoidDateDropdown
                .thenReturn(LocalDate.now().plusDays(7))//ownInitiativeDate
                .thenReturn(LocalDate.now().plusDays(7))//withOutNoticeDate
                .thenReturn(LocalDate.now().plusDays(14))//assistedOrderCostsFirstDropdownDate
                .thenReturn(LocalDate.now().plusDays(14))//assistedOrderAssessmentThirdDropdownDate
                .thenReturn(LocalDate.now().plusDays(21))//appealChoiceSecondDropdownA
                .thenReturn(LocalDate.now().plusDays(21))//appealChoiceSecondDropdownB
                .thenReturn(LocalDate.now().plusDays(21))//appealChoiceSecondDropdownA and refused
                .thenReturn(LocalDate.now().plusDays(21)); //appealChoiceSecondDropdownB and refused

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .ccdState(CASE_PROGRESSION)
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER).build();
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
            when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            String advancedDate = LocalDate.now().plusDays(14).toString();
            when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefDataAfterSdo);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDateHeardComplex")
                .extracting("singleDateSelection")
                .extracting("singleDate")
                .isEqualTo(LocalDate.now().toString());
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
            assertThat(response.getData()).extracting("finalOrderFurtherHearingComplex")
                .extracting("hearingLocationList").asString().contains("SiteName after Sdo");
            assertThat(response.getData()).extracting("finalOrderFurtherHearingComplex")
                .extracting("datesToAvoidDateDropdown")
                .extracting("datesToAvoidDates")
                .isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData()).extracting("orderMadeOnDetailsOrderCourt")
                .extracting("ownInitiativeText")
                .isEqualTo(ON_INITIATIVE_SELECTION_TEXT);
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
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("makeAnOrderForCostsList")
                .isEqualTo("CLAIMANT");
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("assistedOrderClaimantDefendantFirstDropdown")
                .isEqualTo("SUBJECT_DETAILED_ASSESSMENT");
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("assistedOrderAssessmentSecondDropdownList1")
                .isEqualTo("STANDARD_BASIS");
            assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts")
                .extracting("assistedOrderAssessmentSecondDropdownList2")
                .isEqualTo("NO");
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
            assertThat(response.getData()).extracting("publicFundingCostsProtection")
                .isEqualTo("No");
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
            when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
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
    class MidEventPopulateTrackToggleAndPopulateDownloadTemplateOptions {
        private static final String PAGE_ID = "assign-track-toggle";

        @Test
        void shouldThrowError_whenFastTrackNotBeingReallocatedToMintiTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderAllocateToTrack(NO).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactlyInAnyOrder(NOT_ALLOWED_FOR_TRACK);
        }

        @Test
        void shouldThrowError_whenSmallTrackNotBeingReallocatedToMintiTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(NO).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactlyInAnyOrder(NOT_ALLOWED_FOR_TRACK);
        }

        @Test
        void shouldNotThrowError_whenSmallTrackBeingReallocatedToMintiTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenSmallTrackNotInJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(NO).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenFastTrackBeingReallocatedToMintiTrack() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenFastTrackBeingReallocatedToMintiTrackMintiEnabledJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenFastTrackBeingReallocatedToMintiTrackNoMintiNoJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenFastTrackBeingReallocatedToMintiTrackMintiEnabledNotJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenFastTrackNotInJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .finalOrderAllocateToTrack(NO).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenMultiTrackInJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .finalOrderAllocateToTrack(NO).build();
            CallbackParams params = callbackParamsOf(caseData.toMap(mapper), caseData, MID, PAGE_ID, JUDICIAL_REFERRAL);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotThrowError_whenMultiTrackAllocateToTrackNoMintiNoJudicialReferral() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                .build().toBuilder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM)
                .finalOrderAllocateToTrack(YES).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }

        @Nested
        class FinalOrderTrackNotAllocated {

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenUnspecClaimIntermediateClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
                    .finalOrderAllocateToTrack(NO).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(3);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(2).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenUnspecClaimMultiClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                    .finalOrderAllocateToTrack(NO).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(4);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(2).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(3).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(3).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenSpecClaimIntermediateClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .responseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name())
                    .finalOrderAllocateToTrack(NO).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(3);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(2).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenSpecClaimMultiClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
                    .finalOrderAllocateToTrack(NO).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(4);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(2).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(3).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(3).getLabel());
            }

            @Test
            void shouldDefaultToSmallClaim_whenSpecClaimDJNoClaimTrackMultiClaimAmount() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .totalClaimAmount(BigDecimal.valueOf(1000000))
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(0);
                assertThat(updatedData.getFinalOrderTrackToggle())
                    .isEqualTo(AllocatedTrack.SMALL_CLAIM.name());
            }
        }

        @Nested
        class FinalOrderTrackAllocated {

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenUnspecClaimAllocatedToIntermediateClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(3);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(2).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenUnspecClaimAllocatedToMultiClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(4);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(2).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(3).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(3).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenSpecClaimAllocatedToIntermediateClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(AllocatedTrack.INTERMEDIATE_CLAIM).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(3);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(FAST_INT_OPTIONS.getListItems().get(2).getLabel());
            }

            @Test
            void shouldPopulateDownloadOrderTemplateValues_whenSpecClaimAllocatedToMultiClaim() {
                // Given
                CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
                    .build().toBuilder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM).build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                // When
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                // Then
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems()).hasSize(4);
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(0).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(0).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(1).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(1).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(2).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(2).getLabel());
                assertThat(updatedData.getFinalOrderDownloadTemplateOptions().getListItems().get(3).getLabel())
                    .isEqualTo(MULTI_OPTIONS.getListItems().get(3).getLabel());
            }
        }

    }

    @Nested
    class MidEventGenerateTemplatesMinti {
        private static final String PAGE_ID = "create-download-template-document";

        @Test
        void shouldNotGenerateAfterHearingTemplateBeforeHearingDate_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used after a hearing")
                                                                  .build())
                                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDownloadTemplateDocument").isNull();
            assertThat(response.getData()).extracting("showOrderAfterHearingDatePage").isEqualTo("Yes");
        }

        @Test
        void shouldGenerateTemplateBeforeHearing_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used before a hearing/box work")
                                                                  .build())
                                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeOrderDownloadGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDownloadTemplateDocument").isNotNull();
            assertThat(response.getData()).extracting("showOrderAfterHearingDatePage").isEqualTo("No");
        }

        @Test
        void shouldGenerateTemplateFixDateForCCMC_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Fix a date for CCMC")
                                                                  .build())
                                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeOrderDownloadGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDownloadTemplateDocument").isNotNull();
            assertThat(response.getData()).extracting("showOrderAfterHearingDatePage").isEqualTo("No");
        }

        @Test
        void shouldGenerateTemplateFixDateForCMC_onMidEventCallback() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Fix a date for CMC")
                                                                  .build())
                                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeOrderDownloadGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDownloadTemplateDocument").isNotNull();
            assertThat(response.getData()).extracting("showOrderAfterHearingDatePage").isEqualTo("No");
        }
    }

    @Nested
    class MidEventGenerateTemplatesAfterHearingMinti {
        private static final String PAGE_ID = "hearing-date-order";

        @Test
        void shouldGenerateAfterHearingTemplateBeforeHearingDate_onMidEventCallback_noDateValidationErrors() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used after a hearing")
                                                                  .build())
                                                       .build())
                .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                           .dateType(OrderAfterHearingDateType.SINGLE_DATE)
                                           .date(LocalDate.now().minusDays(2))
                                           .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            when(judgeOrderDownloadGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderDownloadTemplateDocument").isNotNull();
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnError_whenSingleDateIsInFuture() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used after a hearing")
                                                                  .build())
                                                       .build())
                .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                           .dateType(OrderAfterHearingDateType.SINGLE_DATE)
                                           .date(LocalDate.now().plusDays(2))
                                           .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).hasSize(1).containsExactlyInAnyOrder(FUTURE_SINGLE_DATE_ERROR);
        }

        @Test
        void shouldReturnError_whenDateRangeDatesAreInFuture() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used after a hearing")
                                                                  .build())
                                                       .build())
                .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                           .dateType(OrderAfterHearingDateType.DATE_RANGE)
                                           .fromDate(LocalDate.now().plusDays(2))
                                           .toDate(LocalDate.now().plusDays(3))
                                           .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).hasSize(2).containsExactlyInAnyOrder(FUTURE_DATE_FROM_ERROR, FUTURE_DATE_TO_ERROR);
        }

        @Test
        void shouldReturnError_whenFromDateIsAfterToDate() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label("Blank template to be used after a hearing")
                                                                  .build())
                                                       .build())
                .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                           .dateType(OrderAfterHearingDateType.DATE_RANGE)
                                           .fromDate(LocalDate.now().minusDays(2))
                                           .toDate(LocalDate.now().minusDays(3))
                                           .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getErrors()).hasSize(1).containsExactlyInAnyOrder(DATE_FROM_AFTER_DATE_TO_ERROR);
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
                            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                    .dateToDate(LocalDate.now().minusDays(4))
                                    .listFromDate(LocalDate.now().minusDays(5))
                                    .build()).build()
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
                                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                        .dateToDate(LocalDate.now().minusDays(5))
                                        .listFromDate(LocalDate.now().minusDays(4))
                                        .build()).build(),
                    "The date range in Further hearing may not have a 'from date', that is after the 'date to'"
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
            assertThat(response.getErrors().get(0)).isEqualTo(expectedErrorMessage);
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

        @Test
        void shouldReturnErrorNoAlternateCourtSelected_onMidEventCallback() {
            // Given
            DynamicList hearingLocation = DynamicList.builder().value(DynamicListElement.builder().code("OTHER_LOCATION").build()).build();
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                     .lengthList(HearingLengthFinalOrderList.OTHER)
                                                     .lengthListOther(CaseHearingLengthElement.builder().lengthListOtherDays("one").build())
                                                     .hearingLocationList(hearingLocation)
                                                     .alternativeHearingList(null)
                                                     .build())
                .build();

            // When
            when(judgeFinalOrderGenerator.generate(any(), any())).thenReturn(finalOrder);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, MID, PAGE_ID));
            // Then
            assertThat(response.getErrors().get(0)).isEqualTo(FURTHER_HEARING_OTHER_ALT_LOCATION);

        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldAddDocumentToCollection_onAboutToSubmit() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDocumentCollection(new ArrayList<>())
                .finalOrderDocument(finalOrder)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_Judge Judy" + ".pdf";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
            assertThat(finalCaseDocuments).hasSize(1);

        }

        @Test
        void shouldHideDocumentIfClaimantWelsh_onAboutToSubmit() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .build();
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_Judge Judy" + ".pdf";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection()).hasSize(1);
            assertThat(updatedData.getPreTranslationDocuments().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getPreTranslationDocuments().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
        }

        @Test
        void shouldNotHideDocumentIfWelshDisabled_onAboutToSubmit() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .build();
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_Judge Judy" + ".pdf";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
        }

        @Test
        void shouldHideDocumentIfDefendantWelsh_onAboutToSubmit() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .build();
            caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
                                            .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                        .respondent1ResponseLanguage("WELSH").build()).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_Judge Judy" + ".pdf";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection()).hasSize(1);
            assertThat(updatedData.getPreTranslationDocuments().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getPreTranslationDocuments().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
        }

        @Test
        void shouldAddTemplateDocumentToCollection_onAboutToSubmit() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_AFTER_HEARING.getLabel())
                                                                  .build()).build())
                .finalOrderDocumentCollection(finalCaseDocuments)
                .uploadOrderDocumentFromTemplate(uploadedDocument)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_order.docx";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
        }

        @Test
        void shouldAddTemplateDocumentToCollectionDirectionsOrder_onAboutToSubmit() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .label(BLANK_TEMPLATE_BEFORE_HEARING.getLabel())
                                                                  .build()).build())
                .finalOrderDocumentCollection(finalCaseDocuments)
                .uploadOrderDocumentFromTemplate(uploadedDocument)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            // Then
            String fileName = LocalDate.now() + "_directions order.docx";
            assertThat(response.getData()).extracting("finalOrderDocumentCollection").isNotNull();
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo("caseManagementOrders");
            assertThat(updatedData.getFinalOrderDocumentCollection().get(0)
                           .getValue().getDocumentLink().getDocumentFileName()).isEqualTo(fileName);
        }

        @Test
        void shouldChangeStateToFinalOrder_onAboutToSubmitAndFreeFormOrder() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
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
            assertThat(response.getState()).isEqualTo(null);
        }

        @Test
        void shouldChangeStateToFinalOrder_onAboutToSubmitAndAssistedOrderAndNoFurtherHearing() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
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
            assertThat(response.getState()).isEqualTo(null);
        }

        @Test
        void shouldChangeStateToCaseProgression_onAboutToSubmitAndAssistedOrderWithFurtherHearing() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
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
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
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
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .freeFormHearingNotes("test text hearing notes free form order")
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("test text hearing notes free form order");
            assertThat(toggle).hasSize(1);

        }

        @Test
        void shouldNotRePopulateHearingNotes_whenAssistedHearingNotesDoNotExist() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
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
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                        .forename("Judge")
                                                                        .surname("Judy")
                                                                        .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("hearingNotes").extracting("notes").isEqualTo("preexisting hearing notes");
        }

        @Test
        void shouldMoveToCaseProgressionState_whenPreviousStateWasJudicialReferral() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .finalOrderAllocateToTrack(YES)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, JUDICIAL_REFERRAL);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo(CASE_PROGRESSION.name());
        }

        @Test
        void shouldChangeClaimTrack_whenNewTrackIsAllocatedSpecClaim() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .finalOrderAllocateToTrack(YES)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderAllocateToTrack").isNull();
            assertThat(response.getData()).extracting("finalOrderTrackAllocation").isNull();
            assertThat(response.getData()).extracting("responseClaimTrack").isEqualTo("MULTI_CLAIM");
        }

        @Test
        void shouldChangeClaimTrack_whenNewTrackIsAllocatedUnspecClaim() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(YES)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderAllocateToTrack").isNull();
            assertThat(response.getData()).extracting("finalOrderTrackAllocation").isNull();
            assertThat(response.getData()).extracting("allocatedTrack").isEqualTo("MULTI_CLAIM");
        }

        @Test
        void shouldNotChangeClaimTrack_whenNewTrackIsNotAllocated() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(NO)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("finalOrderAllocateToTrack").isNull();
            assertThat(response.getData()).extracting("finalOrderTrackAllocation").isNull();
            assertThat(response.getData()).extracting("allocatedTrack").isEqualTo("SMALL_CLAIM");
        }

        @Test
        void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());

            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(NO)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());
            assertThat(response.getData()).extracting("caseManagementLocation").isNotNull();
        }

        @Test
        void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());

            handler = new GenerateDirectionOrderCallbackHandler(locationRefDataService, mapper, judgeFinalOrderGenerator,
                                                                judgeOrderDownloadGenerator, locationHelper, theUserService,
                                                                workingDayIndicator, featureToggleService, Optional.empty());

            List<FinalOrderToggle> toggle = new ArrayList<>();
            toggle.add(FinalOrderToggle.SHOW);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder()
                                            .region("2")
                                            .baseLocation("111")
                                            .build())
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(toggle)
                .hearingNotes(HearingNotes.builder().notes("preexisting hearing notes").build())
                .finalOrderDocument(finalOrder)
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .finalOrderAllocateToTrack(NO)
                .finalOrderTrackAllocation(AllocatedTrack.MULTI_CLAIM)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(updateWaCourtLocationsService);
            assertThat(response.getData()).extracting("caseManagementLocation").isNotNull();
        }

        @Test
        void shouldNotChangeState_onAboutToSubmitAndAssistedOrderWithNoFurtherHearingWithCaseEventsEnabled() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(null)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .ccdState(PREPARE_FOR_HEARING_CONDUCT_HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo(null);
        }

        @Test
        void shouldNotChangeState_onAboutToSubmitAndAssistedOrderWithNoFurtherHearingWithCaseEventsEnabledFreeForm() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
                .finalOrderFurtherHearingToggle(null)
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .ccdState(PREPARE_FOR_HEARING_CONDUCT_HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo(null);
        }

        @Test
        void shouldNotChangeState_onAboutToSubmitAndMintiClaimMultiClaim() {
            when(theUserService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                            .forename("Judge")
                                                                            .surname("Judy")
                                                                            .roles(Collections.emptyList()).build());
            // Given
            List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
            finalCaseDocuments.add(element(finalOrder));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
                .finalOrderFurtherHearingToggle(List.of(FinalOrderToggle.SHOW))
                .finalOrderDocumentCollection(finalCaseDocuments)
                .finalOrderDocument(finalOrder)
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .ccdState(PREPARE_FOR_HEARING_CONDUCT_HEARING)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getState()).isEqualTo(null);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void should1v1Text_WhenSubmittedAndCase1v1() {
            // Given
            String confirmationHeader = format(HEADER, 1234);
            String confirmationBody = format(BODY_1_V_1, "Mr. John Rambo", "Mr. Sole Trader");

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
            String confirmationBody = format(BODY_1_V_2, "Mr. John Rambo", "Mr. Sole Trader", "Mr. John Rambo");
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
            String confirmationBody = format(BODY_2_V_1, "Mr. John Rambo", "Mr. Jason Rambo", "Mr. Sole Trader");
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
