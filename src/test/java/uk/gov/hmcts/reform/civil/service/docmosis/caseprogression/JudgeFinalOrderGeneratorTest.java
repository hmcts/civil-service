package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
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
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.ClaimantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.DefendantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.AppealInitiativePopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.AttendeesRepresentationPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.CaseInfoPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.CostDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.HearingDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.JudgeCourtDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.JudgeFinalOrderFormPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers.OrderDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.COSTS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.INDEMNITY_BASIS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.SUBJECT_DETAILED_ASSESSMENT;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel.IN_PERSON;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@SpringBootTest(classes = {
    JudgeFinalOrderGenerator.class,
    ClaimantAttendsOrRepresentedTextBuilder.class,
    DefendantAttendsOrRepresentedTextBuilder.class,
    AppealInitiativePopulator.class,
    AttendeesRepresentationPopulator.class,
    CaseInfoPopulator.class,
    CostDetailsPopulator.class,
    HearingDetailsPopulator.class,
    JudgeCourtDetailsPopulator.class,
    OrderDetailsPopulator.class,
    JudgeFinalOrderFormPopulator.class,
    JacksonAutoConfiguration.class})
class JudgeFinalOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String fileFreeForm = format(FREE_FORM_ORDER_PDF.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final String assistedForm = format(ASSISTED_ORDER_PDF.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final CaseLocationCivil caseManagementLocation = new CaseLocationCivil().setBaseLocation("000000");
    List<FinalOrderToggle> toggleList = new ArrayList<FinalOrderToggle>(Arrays.asList(FinalOrderToggle.SHOW));
    private static final CaseDocument FREE_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(fileFreeForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    private static final CaseDocument ASSISTED_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(assistedForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private UserService userService;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;
    @Qualifier("judgeFinalOrderGenerator")
    @Autowired
    private JudgeFinalOrderGenerator generator;
    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    private DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;
    @MockBean
    private ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;
    @MockBean
    private AppealInitiativePopulator appealInitiativePopulator;
    @MockBean
    private AttendeesRepresentationPopulator attendeesRepresentationPopulator;
    @MockBean
    private CaseInfoPopulator caseInfoPopulator;
    @MockBean
    private CostDetailsPopulator costsDetailsGroup;
    @MockBean
    private HearingDetailsPopulator hearingDetailsPopulator;
    @MockBean
    private JudgeCourtDetailsPopulator judgeCourtDetailsPopulator;
    @MockBean
    private OrderDetailsPopulator orderDetailsPopulator;

    private static LocationRefData locationRefData = LocationRefData.builder().siteName("SiteName")
        .courtAddress("1").postcode("1")
        .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
        .externalShortName("ExternalShortName")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @BeforeEach
    public void setUp() throws JsonProcessingException {

        when(userService.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));
        when(userService.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefData);
        when(locationRefDataService.getCcmccLocation(any())).thenReturn(locationRefData);
        when(locationRefDataService.getCnbcLocation(any())).thenReturn(locationRefData);
        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(List.of(locationRefData));
    }

    @Test
    void shouldThrowException_whenBaseCourtLocationNotFound() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenThrow(IllegalArgumentException.class);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("00000888"))
            .build();

        assertThrows(IllegalArgumentException.class, () -> generator.generate(caseData, BEARER_TOKEN));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenNoneSelected() {
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(orderDetailsPopulator.populateOrderDetails(any(), any())).thenReturn(new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(orderDetailsPopulator.populateOrderDetails(any(), any())).thenReturn(new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderOnCourtInitiativeSelected() {
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(orderDetailsPopulator.populateOrderDetails(any(), any())).thenReturn(new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .orderOnCourtInitiative(new FreeFormOrderValues().setOnInitiativeSelectionTextArea("test")
                                     .setOnInitiativeSelectionDate(LocalDate.now()))
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderWithoutNoticeIsSelected() {
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(orderDetailsPopulator.populateOrderDetails(any(), any())).thenReturn(new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .ccdState(CaseState.CASE_PROGRESSION)
            .orderWithoutNotice(new FreeFormOrderValues().setWithoutNoticeSelectionTextArea("test without notice")
                                .setWithoutNoticeSelectionDate(LocalDate.now()))
            .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                             .partyID("app-2-party-id")
                             .partyName("Applicant2")
                             .build())
            .applicant2(PartyBuilder.builder().soleTrader().build().toBuilder()
                            .partyID("res-2-party-id")
                            .partyName("Respondent2")
                            .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenHearingLocationExists() {
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(orderDetailsPopulator.populateOrderDetails(any(), any())).thenReturn(new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .caseManagementLocation(caseManagementLocation)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .ccdState(CaseState.CASE_PROGRESSION)
            .orderWithoutNotice(new FreeFormOrderValues().setWithoutNoticeSelectionTextArea("test without notice")
                                .setWithoutNoticeSelectionDate(LocalDate.now()))
            .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                             .partyID("app-2-party-id")
                             .partyName("Applicant2")
                             .build())
            .applicant2(PartyBuilder.builder().soleTrader().build().toBuilder()
                            .partyID("res-2-party-id")
                            .partyName("Respondent2")
                            .build())
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.dynamicElement("hearing-location")).build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenOptionalSectionsNotPresent() {
        //Given: case data without recitals selected
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(appealInitiativePopulator.populateInitiativeOrWithoutNoticeDetails(any(), any())).thenReturn(
            new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            // Order made section
            .finalOrderMadeSelection(NO)
            // judge heard from section
            .finalOrderJudgeHeardFrom(null)
            // recitals section
            .finalOrderRecitals(null)
            // ordered section
            .finalOrderOrderedThatText("order text")
            // Further hearing section
            .finalOrderFurtherHearingToggle(null)
            .finalOrderFurtherHearingComplex(null)
            // Costs section
            .assistedOrderCostList(AssistedCostTypesList.COSTS_IN_THE_CASE)
            .assistedOrderMakeAnOrderForCosts(null)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails())
            .publicFundingCostsProtection(NO)
            // Appeal section
            .finalOrderAppealToggle(null)
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(new OrderMadeOnDetails()
                                              .setOwnInitiativeText("own initiative test")
                                              .setOwnInitiativeDate(LocalDate.now()))
            .finalOrderGiveReasonsYesNo(NO)
            .caseManagementLocation(caseManagementLocation)
            .build();

        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenOtherOptions() {
        //Given: case data without recitals selected
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(appealInitiativePopulator.populateInitiativeOrWithoutNoticeDetails(any(), any())).thenReturn(
            new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            // Order made section
            .finalOrderDateHeardComplex(new OrderMade().setSingleDateSelection(new DatesFinalOrders().setSingleDate(
                LocalDate.now())))
            // Papers considered
            .finalOrderJudgePapers(null)
            // judge heard from section
            .finalOrderRepresentation(new FinalOrderRepresentation()
                                          .setTypeRepresentationList(FinalOrderRepresentationList.OTHER_REPRESENTATION)
                                          .setTypeRepresentationOtherComplex(new ClaimantAndDefendantHeard()
                                                                              .setDetailsRepresentationText("Test")))
            // Order made on court's own initiative section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(new OrderMadeOnDetails().setOwnInitiativeDate(LocalDate.now()))
            // Further hearing section
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(new FinalOrderFurtherHearing()
                                                 .setAlternativeHearingList(null)
                                                 .setHearingMethodList(IN_PERSON))
            // Costs section
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsYesOrNo(YES)
                                                  .setAssistedOrderAssessmentSecondDropdownList1(INDEMNITY_BASIS)
                                                  .setAssistedOrderAssessmentSecondDropdownList2(CostEnums.YES)
                                                  .setMakeAnOrderForCostsList(COSTS)
                                                  .setAssistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(10000L))
                                                  .setMakeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                  .setAssistedOrderClaimantDefendantFirstDropdown(SUBJECT_DETAILED_ASSESSMENT))
            .publicFundingCostsProtection(YES)
            // Appeal section
            .finalOrderAppealComplex(new FinalOrderAppeal()
                                         .setApplicationList(ApplicationAppealList.REFUSED)
                                         .setAppealRefusedDropdown(new AppealGrantedRefused()
                                                                    .setCircuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                    .setAppealChoiceSecondDropdownA(new AppealChoiceSecondDropdown())))
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(new OrderMadeOnDetails()
                                              .setOwnInitiativeText("own initiative test")
                                              .setOwnInitiativeDate(LocalDate.now()))
            .caseManagementLocation(caseManagementLocation)
            .build();

        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenHearingLocationExists() {
        //Given: Case data with all fields for docmosis
        LocalDate appealDate = LocalDate.now();
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(appealInitiativePopulator.populateInitiativeOrWithoutNoticeDetails(any(), any())).thenReturn(
            new JudgeFinalOrderForm().setInitiativeDate(appealDate));

        DynamicListElement dynamicListElement = DynamicListElement.builder().label("test_label").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(Collections.singletonList(dynamicListElement))
            .value(dynamicListElement)
            .build();
        List<FinalOrdersJudgePapers> finalOrdersJudgePapersList =
            new ArrayList<>(Arrays.asList(FinalOrdersJudgePapers.CONSIDERED));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .caseManagementLocation(caseManagementLocation)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            // Order made section
            .finalOrderDateHeardComplex(new OrderMade().setSingleDateSelection(new DatesFinalOrders().setSingleDate(
                LocalDate.now())))
            //Papers considered
            .finalOrderJudgePapers(
                finalOrdersJudgePapersList)
            // judge heard from section
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .applicant2(PartyBuilder.builder().individual().build())
            .addApplicant2(YES)
            .finalOrderRepresentation(new FinalOrderRepresentation()
                                          .setTypeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                          .setTypeRepresentationComplex(new ClaimantAndDefendantHeard()))
            // recitals section
            .finalOrderRecitals(toggleList)
            .finalOrderRecitalsRecorded(new FinalOrderRecitalsRecorded().setText("Test"))
            // further hearing section
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(new FinalOrderFurtherHearing()
                                                 .setAlternativeHearingList(dynamicList)
                                                 .setHearingMethodList(IN_PERSON)
                                                 .setHearingNotesText("test hearing notes")
                                                 .setDatesToAvoidDateDropdown(new DatesFinalOrders().setDatesToAvoidDates(LocalDate.now())))
            // Costs section
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                  .setAssistedOrderAssessmentSecondDropdownList2(CostEnums.NO)
                                                  .setMakeAnOrderForCostsList(COSTS)
                                                  .setAssistedOrderClaimantDefendantFirstDropdown(COSTS)
                                                  .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L))
                                                  .setMakeAnOrderForCostsYesOrNo(YesOrNo.YES))
            .assistedOrderCostsReserved(new AssistedOrderCostDetails().setDetailsRepresentationText("Test"))
            .finalOrderGiveReasonsComplex(new AssistedOrderReasons().setReasonsText("Test"))
            .assistedOrderCostsBespoke(new AssistedOrderCostDetails().setBesPokeCostDetailsText("Test"))
            .publicFundingCostsProtection(YES)
            // Appeal section
            .finalOrderAppealComplex(new FinalOrderAppeal()
                                         .setApplicationList(ApplicationAppealList.GRANTED)
                                         .setAppealGrantedDropdown(new AppealGrantedRefused()
                                                                    .setCircuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                    .setAppealChoiceSecondDropdownB(new AppealChoiceSecondDropdown())))
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.WITHOUT_NOTICE)
            .orderMadeOnDetailsOrderWithoutNotice(new OrderMadeOnDetailsOrderWithoutNotice()
                                                      .setWithOutNoticeText("without notice test")
                                                      .setWithOutNoticeDate(LocalDate.now()))
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.dynamicElement("hearing-location")).build())
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void testGetInitiativeTextWithoutNotice() {
        CaseData caseDataInitiative = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(new OrderMadeOnDetails().setOwnInitiativeText("test initiative text"))
            .build();
        String responseInitiative = generator.getInitiativeOrWithoutNotice(caseDataInitiative);
        assertEquals("test initiative text", responseInitiative);

        CaseData caseDataWithoutNotice = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderMadeOnDetailsList(OrderMadeOnTypes.WITHOUT_NOTICE)
            .orderMadeOnDetailsOrderWithoutNotice(new OrderMadeOnDetailsOrderWithoutNotice().setWithOutNoticeText("test without notice text"))
            .build();
        String responseWithoutNotice = generator.getInitiativeOrWithoutNotice(caseDataWithoutNotice);
        assertEquals("test without notice text", responseWithoutNotice);

        CaseData caseDataWhenNone = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderMadeOnDetailsList(OrderMadeOnTypes.NONE)
            .build();
        String responseWhenNone = generator.getInitiativeOrWithoutNotice(caseDataWhenNone);
        assertNull(responseWhenNone);
    }

}
