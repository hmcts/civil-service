package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private ObjectMapper objMapper = ObjectMapperFactory.instance();

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

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(GENERATE_DIRECTIONS_ORDER);
    }

    @Test
    void setCaseName() {
        when(idamClient.getUserInfo(any()))
            .thenReturn(UserInfo.builder().givenName("John").familyName("Doe").build());
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .atStateClaimDraft()
            .build().copy()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
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
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(7)))
            .thenReturn(localDatePlus7days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(14)))
            .thenReturn(localDatePlus14days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(21)))
            .thenReturn(localDatePlus21days);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().copy().isMultiParty(NO)
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
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(7)))
            .thenReturn(localDatePlus7days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(14)))
            .thenReturn(localDatePlus14days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(21)))
            .thenReturn(localDatePlus21days);
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(new LocationRefData().setSiteName("Site Name 1").setCourtAddress("Address1").setPostcode("18000"));
        locations.add(new LocationRefData().setSiteName("Site Name 2").setCourtAddress("Address2").setPostcode("28000"));
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().copy().locationName("County Court Money Centre")
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
                "defendantOnePartyName")
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
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownGranted").extracting(
                "assistedOrderAppealFirstOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownGranted").extracting(
                "assistedOrderAppealSecondOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownRefused").extracting(
                "assistedOrderAppealFirstOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownRefused").extracting(
                "assistedOrderAppealSecondOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
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
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(7)))
            .thenReturn(localDatePlus7days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(14)))
            .thenReturn(localDatePlus14days);
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), eq(21)))
            .thenReturn(localDatePlus21days);
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(new LocationRefData().setSiteName("Site Name 1").setCourtAddress("Address1").setPostcode("18000"));
        locations.add(new LocationRefData().setSiteName("Site Name 2").setCourtAddress("Address2").setPostcode("28000"));
        when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft()
            .build().copy().locationName("County Court Money Centre")
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
                "defendantOnePartyName")
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
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownGranted").extracting(
                "assistedOrderAppealFirstOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownGranted").extracting(
                "assistedOrderAppealSecondOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownRefused").extracting(
                "assistedOrderAppealFirstOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
        assertThat(response.getData().get("assistedOrderAppealDetails")).extracting("assistedOrderAppealDropdownRefused").extracting(
                "assistedOrderAppealSecondOption")
            .extracting("assistedOrderAppealDate").isEqualTo(localDatePlus21days.toString());
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
            new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now().plusDays(1))))
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
            new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
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
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy()
            .finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now()))).build();
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

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now().plusDays(1)))).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_FromIsAfter() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().plusDays(1))
                    .setDateRangeTo(LocalDate.now()))).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();

    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_ToIsAfter() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now())
                    .setDateRangeTo(LocalDate.now().plusDays(2)))).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldShowError_When_DatesToAvoidIsBeforeTodayDate() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().minusDays(
                                                                                      2))
                                                                                  ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Further Hearing may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromClaimantListIsNull() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                             .setRepresentationType(CLAIMANT_AND_DEFENDANT)
                                             .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                  .setDefendantRepresentation(
                                                                                      DefendantRepresentationType.COST_DRAFTSMAN_FOR_THE_DEFENDANT))
                                             )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Claimant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromDefendantListIsNull() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                             .setRepresentationType(CLAIMANT_AND_DEFENDANT)
                                             .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                  .setClaimantRepresentation(
                                                                                      ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING))
                                             )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Defendant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldShowError_When_JudgeHeardFromDefendantTwoListIsNull() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalOrderApplication()
            .build()
            .copy().isMultiParty(YES).finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                             .setRepresentationType(CLAIMANT_AND_DEFENDANT)
                                             .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                  .setDefendantRepresentation(
                                                                                      DefendantRepresentationType.COUNSEL_FOR_DEFENDANT)
                                                                                  .setClaimantRepresentation(
                                                                                      ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING))
                                             )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "Judge Heard from: 'Claimant(s) and defendant(s)' section for Defendant, requires a selection to be made")).isTrue();
    }

    @Test
    void shouldNotShowError_When_DatesToAvoidIsAfterTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_assistedOrderCostsFirstDropdownDateAndThirdDropdownDateIsAfterTodayDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsAfterTodayDateAndThirdDropdownDateIsPrevious() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().minusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsPreviousDateAndThirdDropdownDateIsPrevious() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().minusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderCostsFirstDropdownDateIsPreviousDateAndThirdDropdownDateIsAfterDate() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains(
            "The date in Make an order for detailed/summary costs may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderAppealFirstDropdownDateIsPreviousDate() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO))
            .assistedOrderAppealDetails(new AssistedOrderAppealDetails().setAppealTypeChoicesForGranted(
                    new AppealTypeChoices()
                        .setAppealChoiceOptionA(
                            new AppealTypeChoiceList()
                                .setAppealGrantedRefusedDate(LocalDate.now().minusDays(2))
                                ))
                                            .setAppealTypeChoicesForRefused(new AppealTypeChoices()
                                                                             .setAppealChoiceOptionA(
                                                                                 new AppealTypeChoiceList()
                                                                                     .setAppealGrantedRefusedDate(LocalDate.now().minusDays(
                                                                                         2))
                                                                                     ))).build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Appeal notice date may not be before the established date")).isTrue();
    }

    @Test
    void shouldShowError_When_assistedOrderAppealSecondDropdownDateIsPreviousDate() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .assistedOrderAppealDetails(new AssistedOrderAppealDetails().setAppealTypeChoicesForGranted(
                new AppealTypeChoices()
                    .setAppealChoiceOptionB(
                        new AppealTypeChoiceList()
                            .setAppealGrantedRefusedDate(LocalDate.now().minusDays(2))
                            )))
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors().contains("The date in Appeal notice date may not be before the established date")).isTrue();
    }

    @Test
    void shouldNotShowError_When_assistedOrderAppealSecondDropdownDateIsAfterDate() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .assistedOrderAppealDetails(new AssistedOrderAppealDetails().setAppealTypeChoicesForGranted(
                new AppealTypeChoices()
                    .setAppealChoiceOptionB(
                        new AppealTypeChoiceList()
                            .setAppealGrantedRefusedDate(LocalDate.now().plusDays(2))
                            )))
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_assistedOrderAppealChoicesAreEmpty() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(NO)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setDatesToAvoid(YES)
                                                    .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard()
                                                                                  .setDatesToAvoidDates(LocalDate.now().plusDays(
                                                                                      2))
                                                                                  ))
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(2))
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().plusDays(2))
                                                  .setMakeAnOrderForCostsYesOrNo(NO)

            )
            .assistedOrderAppealDetails(new AssistedOrderAppealDetails().setAppealTypeChoicesForGranted(
                new AppealTypeChoices()
                    ))
            .build();

        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldShowError_When_OrderDateIsDateRange_FromIsAfterDateTo() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(2))
                    .setDateRangeTo(LocalDate.now().minusDays(3)))).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldNotShowError_When_OrderDateIsDateRange_FromIsAfterDateTo() {

        when(assistedOrderFormGenerator.generate(any(), any()))
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(2))
                    .setDateRangeTo(LocalDate.now()))).build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotShowError_When_AssistedOrderNotMade_FinalOrderPreviewDoc_onMidEventCallback() {

        // Given
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(
            new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy()
            .finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER)
            .generalAppDetailsOfOrder("order test")
            .assistedOrderMadeSelection(YesOrNo.NO)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void shouldGenerateFinalOrderPreviewDocumentWhenPopulateFinalOrderPreviewDocIsCalled() {
        when(freeFormOrderGenerator.generate(any(), any())).thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.FREE_FORM_ORDER).build();
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
            .thenReturn(new CaseDocument().setDocumentLink(new Document()));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build()
            .copy().finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER).build();
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
                .build().copy()
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
                .build().copy()
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
                .build().copy()
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
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().gaFinalOrderDocPreview(new Document()).build();

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
                .build().copy()
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
