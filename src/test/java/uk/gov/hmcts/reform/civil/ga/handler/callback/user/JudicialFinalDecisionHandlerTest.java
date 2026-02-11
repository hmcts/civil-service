package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.ClaimantRepresentationType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.DefendantRepresentationType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoiceList;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoices;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderAppealDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderDateHeard;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderHeardRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderMadeDateHeardDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.ClaimantDefendantRepresentation;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.AssistedOrderFormGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JudicialFinalDecisionHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative, "
        + "any party affected by the order may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String ORDERED_TEXT = "order test";
    private static final LocalDate localDatePlus7days = LocalDate.now().plusDays(7);
    private static final LocalDate localDatePlus14days = LocalDate.now().plusDays(14);
    private static final LocalDate localDatePlus21days = LocalDate.now().plusDays(21);

    @Spy
    private ObjectMapper objMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private JudicialFinalDecisionHandler handler;

    @Mock
    private FreeFormOrderGenerator freeFormOrderGenerator;
    @Mock
    private AssistedOrderFormGenerator assistedOrderFormGenerator;
    @Mock
    private GeneralAppLocationRefDataService locationRefDataService;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    @Mock
    private IdamClient idamClient;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private GaForLipService gaForLipService;

    @BeforeEach
    void setUp() {
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().data(new HashMap<>()).build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenCallRealMethod();
        when(deadlinesCalculator
                 .getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(7)))
            .thenReturn(localDatePlus7days);
        when(idamClient
                 .getUserInfo(any()))
            .thenReturn(UserInfo.builder().givenName("John").familyName("Doe").build());
        when(deadlinesCalculator
                 .getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(14)))
            .thenReturn(localDatePlus14days);
        when(deadlinesCalculator
                 .getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(21)))
            .thenReturn(localDatePlus21days);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(GENERATE_DIRECTIONS_ORDER);
    }

    @Test
    void setCaseName() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .atStateClaimDraft()
            .build().toBuilder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1").build())
            .claimant1PartyName("Mr. John Rambo")
            .defendant1PartyName("Mr. Sole Trader")
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData().get("caseNameHmctsInternal")
                       .toString()).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        assertThat(response.getData().get("judgeTitle")
                       .toString()).isEqualTo("John Doe");
    }

    @Test
    void shouldPopulateFreeFormOrderValues_onMidEventCallback() {
        // Given
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().toBuilder().isMultiParty(NO)
            .generalAppDetailsOfOrder("order test")
            .locationName("County Court Money Centre")
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("Ccmcc")
                                        .region("4")
                                        .siteName("County Court Money Centre").build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-finalOrder-form-values");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionTextArea")
            .isEqualTo(ON_INITIATIVE_SELECTION_TEST);
        assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionDate")
            .isEqualTo(LocalDate.now().plusDays(7).toString());
        assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionTextArea")
            .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
        assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionDate")
            .isEqualTo(LocalDate.now().plusDays(7).toString());

    }

    @Test
    void shouldPopulate_AssistedOrderFormOrderValues_onMidEventCallback() {

        // Given
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                          .build());
        locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                          .build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().toBuilder().locationName("County Court Money Centre")
            .claimant1PartyName("Mr. John Rambo")
            .defendant1PartyName("Mr. Sole Trader")
            .isMultiParty(NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("Ccmcc")
                                        .region("4")
                                        .siteName("County Court Money Centre").build())
            .generalAppDetailsOfOrder("order test").build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-finalOrder-form-values");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getData()).extracting("orderMadeOnOwnInitiative").extracting("detailText")
            .isEqualTo(ON_INITIATIVE_SELECTION_TEST);
        assertThat(response.getData()).extracting("orderMadeOnWithOutNotice").extracting("detailText")
            .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
        assertThat(response.getData()).extracting("assistedOrderMadeDateHeardDetails").extracting("singleDateSelection")
            .isNotNull();

        LocalDate localDatePlus14days = LocalDate.now().plusDays(14);
        LocalDate localDatePlus21days = LocalDate.now().plusDays(21);
        assertThat(response.getData().get("assistedOrderRepresentation")).extracting("claimantDefendantRepresentation").extracting(
                "claimantPartyName")
            .isEqualTo(caseData.getClaimant1PartyName());
        assertThat(response.getData().get("assistedOrderRepresentation")).extracting("claimantDefendantRepresentation").extracting(
                "defendantPartyName")
            .isEqualTo(caseData.getDefendant1PartyName());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderAssessmentThirdDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderCostsFirstDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderCostsFirstDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForGranted").extracting(
                "appealChoiceOptionA")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForGranted").extracting(
                "appealChoiceOptionB")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForRefused").extracting(
                "appealChoiceOptionA")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForRefused").extracting(
                "appealChoiceOptionB")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("alternativeHearingLocation")).get("list_items")).getFirst())
                       .get("label")).isEqualTo("Site Name 1 - Address1 - 18000");
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("hearingLocationList")).get("list_items")).get(0))
                       .get("label")).isEqualTo("County Court Money Centre");
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("hearingLocationList")).get("list_items")).get(1))
                       .get("label")).isEqualTo("Other location");
        assertThat(response.getData()).extracting("assistedOrderOrderedThatText")
            .isEqualTo(ORDERED_TEXT);

    }

    @Test
    void shouldPopulate_AssistedOrderFormOrderValues1v2_onMidEventCallback() {

        // Given
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                          .build());
        locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                          .build());
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().toBuilder().locationName("County Court Money Centre")
            .claimant1PartyName("Mr. John Rambo")
            .defendant1PartyName("Mr. Sole Trader")
            .defendant2PartyName("Mr. Sole Trader Defendant2")
            .isMultiParty(YES)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("Ccmcc")
                                        .region("4")
                                        .siteName("County Court Money Centre").build())
            .generalAppDetailsOfOrder("order test").build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-finalOrder-form-values");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getData()).extracting("orderMadeOnOwnInitiative").extracting("detailText")
            .isEqualTo(ON_INITIATIVE_SELECTION_TEST);
        assertThat(response.getData()).extracting("orderMadeOnWithOutNotice").extracting("detailText")
            .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
        assertThat(response.getData()).extracting("assistedOrderMadeDateHeardDetails").extracting("singleDateSelection")
            .isNotNull();

        LocalDate localDatePlus14days = LocalDate.now().plusDays(14);
        LocalDate localDatePlus21days = LocalDate.now().plusDays(21);
        assertThat(response.getData().get("assistedOrderRepresentation")).extracting("claimantDefendantRepresentation").extracting(
                "claimantPartyName")
            .isEqualTo(caseData.getClaimant1PartyName());
        assertThat(response.getData().get("assistedOrderRepresentation")).extracting("claimantDefendantRepresentation").extracting(
                "defendantPartyName")
            .isEqualTo(caseData.getDefendant1PartyName());
        assertThat(response.getData().get("assistedOrderRepresentation")).extracting("claimantDefendantRepresentation").extracting(
                "defendantTwoPartyName")
            .isEqualTo(caseData.getDefendant2PartyName());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderAssessmentThirdDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderCostsFirstDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData()).extracting("assistedOrderMakeAnOrderForCosts").extracting(
                "assistedOrderCostsFirstDropdownDate")
            .isEqualTo(localDatePlus14days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForGranted").extracting(
                "appealChoiceOptionA")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForGranted").extracting(
                "appealChoiceOptionB")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForRefused").extracting(
                "appealChoiceOptionA")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("appealTypeChoicesForRefused").extracting(
                "appealChoiceOptionB")
            .extracting("appealGrantedRefusedDate").isEqualTo(localDatePlus21days.toString());
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("alternativeHearingLocation")).get("list_items")).getFirst())
                       .get("label")).isEqualTo("Site Name 1 - Address1 - 18000");
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("hearingLocationList")).get("list_items")).get(0))
                       .get("label")).isEqualTo("County Court Money Centre");
        assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("assistedOrderFurtherHearingDetails")))
            .get("hearingLocationList")).get("list_items")).get(1))
                       .get("label")).isEqualTo("Other location");
        assertThat(response.getData()).extracting("assistedOrderOrderedThatText")
            .isEqualTo(ORDERED_TEXT);
        assertThat(response.getData()).extracting("orderMadeOnOwnInitiative").extracting("date")
            .isEqualTo(LocalDate.now().plusDays(7).toString());
        assertThat(response.getData()).extracting("orderMadeOnWithOutNotice").extracting("date")
            .isEqualTo(LocalDate.now().plusDays(7).toString());

    }

    @Test
    void shouldShowError_When_OrderDateIsFutureDate_FinalOrderPreviewDoc_onMidEventCallback() {

        // Given
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(
            CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now().plusDays(1)).build()).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).isNotEmpty();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsTodayDate_FinalOrderPreviewDoc_onMidEventCallback() {

        // Given
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(
            CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsSingleSelection() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder()
            .finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1").build())
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = objMapper.convertValue(
            response.getData(),
            GeneralApplicationCaseData.class
        );
        assertThat(updatedData.getGaFinalOrderDocPreview()).isNotNull();

    }

    @Test
    void shouldShowError_When_OrderDateIsSingleSelectionAfterTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now().plusDays(1)).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_FromIsAfter() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().plusDays(1))
                    .dateRangeTo(LocalDate.now()).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_ToIsAfter() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now())
                    .dateRangeTo(LocalDate.now().plusDays(2)).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldShowError_When_DatesToAvoidIsBeforeTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().minusDays(
                                                                                      2))
                                                                                  .build()).build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Further Hearing may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromClaimantListIsNull() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                             .representationType(CLAIMANT_AND_DEFENDANT)
                                             .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                  .defendantRepresentation(
                                                                                      DefendantRepresentationType.COST_DRAFTSMAN_FOR_THE_DEFENDANT).build())
                                             .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Claimant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromDefendantListIsNull() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                             .representationType(CLAIMANT_AND_DEFENDANT)
                                             .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                  .claimantRepresentation(
                                                                                      ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING).build())
                                             .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Defendant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromDefendantTwoListIsNull() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalOrderApplication()
            .build()
            .toBuilder().isMultiParty(YES).finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                             .representationType(CLAIMANT_AND_DEFENDANT)
                                             .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                  .defendantRepresentation(
                                                                                      DefendantRepresentationType.COUNSEL_FOR_DEFENDANT)
                                                                                  .claimantRepresentation(
                                                                                      ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING).build())
                                             .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Defendant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldNotShowError_When_DatesToAvoidIsAfterTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_assistedOrderCostsFirstDropdownDateAndThirdDropdownDateIsAfterTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsAfterTodayDateAndThirdDropdownDateIsPrevious() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().minusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsPreviousDateAndThirdDropdownDateIsPrevious() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().minusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsPreviousDateAndThirdDropdownDateIsAfterDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderAppealFirstDropdownDateIsPreviousDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build())
            .assistedOrderAppealDetails(AssistedOrderAppealDetails.builder().appealTypeChoicesForGranted(
                    AppealTypeChoices.builder()
                        .appealChoiceOptionA(
                            AppealTypeChoiceList.builder()
                                .appealGrantedRefusedDate(LocalDate.now().minusDays(2))
                                .build()).build())
                                            .appealTypeChoicesForRefused(AppealTypeChoices.builder()
                                                                             .appealChoiceOptionA(
                                                                                 AppealTypeChoiceList.builder()
                                                                                     .appealGrantedRefusedDate(LocalDate.now().minusDays(
                                                                                         2))
                                                                                     .build()).build()).build()).build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Appeal notice date may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderAppealSecondDropdownDateIsPreviousDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .assistedOrderAppealDetails(AssistedOrderAppealDetails.builder().appealTypeChoicesForGranted(
                AppealTypeChoices.builder()
                    .appealChoiceOptionB(
                        AppealTypeChoiceList.builder()
                            .appealGrantedRefusedDate(LocalDate.now().minusDays(2))
                            .build()).build()).build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Appeal notice date may not be before the established date")).isTrue();
    }

    @Test
    void shouldNotShowError_When_assistedOrderAppealSecondDropdownDateIsAfterDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .assistedOrderAppealDetails(AssistedOrderAppealDetails.builder().appealTypeChoicesForGranted(
                AppealTypeChoices.builder()
                    .appealChoiceOptionB(
                        AppealTypeChoiceList.builder()
                            .appealGrantedRefusedDate(LocalDate.now().plusDays(2))
                            .build()).build()).build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_assistedOrderAppealChoicesAreEmpty() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .datesToAvoid(YES)
                                                    .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder()
                                                                                  .datesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  .build()).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCost.builder()
                                                  .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .assistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .makeAnOrderForCostsYesOrNo(NO).build()

            )
            .assistedOrderAppealDetails(AssistedOrderAppealDetails.builder().appealTypeChoicesForGranted(
                AppealTypeChoices.builder()
                    .build()).build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldShowError_When_OrderDateIsDateRange_FromIsAfterDateTo() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(2))
                    .dateRangeTo(LocalDate.now().minusDays(3)).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_FromIsAfterDateTo() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(2))
                    .dateRangeTo(LocalDate.now()).build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_AssistedOrderNotMade_FinalOrderPreviewDoc_onMidEventCallback() {

        // Given
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(
            CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void shouldGenerateFinalOrderPreviewDocumentWhenPopulateFinalOrderPreviewDocIsCalled() {
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(CaseDocument
                                                                           .builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = objMapper.convertValue(
            response.getData(),
            GeneralApplicationCaseData.class
        );
        assertThat(updatedData.getGaFinalOrderDocPreview()).isNotNull();
    }

    @Test
    void shouldGenerateAssistedOrderPreviewDocumentWhenPopulateFinalOrderPreviewDocIsCalled() {
        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(CaseDocument.builder().documentLink(Document.builder().build()).build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .toBuilder().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = objMapper.convertValue(
            response.getData(),
            GeneralApplicationCaseData.class
        );
        assertThat(updatedData.getGaFinalOrderDocPreview()).isNotNull();
    }

    @Nested
    class GetAllPartyNames {
        @Test
        void oneVOne() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().toBuilder()
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            String title = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void oneVTwoSameSol() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YES)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .defendant2PartyName("Mr. John Rambo")
                .build();

            String title = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void oneVTwo() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .defendant2PartyName("Mr. John Rambo")
                .build();

            String title = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader, Mr. John Rambo");
        }
    }

    @Nested
    class AboutToSubmitHandling {

        @Test
        void shouldSetUpReadyBusinessProcess() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().gaFinalOrderDocPreview(Document.builder().build()).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objMapper.convertValue(
                response.getData(),
                GeneralApplicationCaseData.class
            );

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked1v1() {
            String body = "<br/><p>The order has been sent to: </p>%n%n ## Claimant 1 %n%n Mr. John Rambo%n%n "
                + "## Defendant 1 %n%n Mr. Sole Trader";
            String header = "# Your order has been issued %n%n ## Case number %n%n # 1678-3567-4955-5475";
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(header))
                    .confirmationBody(format(body))
                    .build());
        }
    }
}
