package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

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
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.model.finalorders.TrialNoticeProcedure;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.COSTS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.INDEMNITY_BASIS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.SUBJECT_DETAILED_ASSESSMENT;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel.IN_PERSON;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JudgeFinalOrderGenerator.class,
    JacksonAutoConfiguration.class
})
public class JudgeFinalOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String fileFreeForm = format(FREE_FORM_ORDER_PDF.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final String assistedForm = format(ASSISTED_ORDER_PDF.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
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
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private IdamClient idamClient;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;
    @Autowired
    private JudgeFinalOrderGenerator generator;

    private static LocationRefData locationRefData = LocationRefData.builder().siteName("SiteName")
        .courtAddress("1").postcode("1")
        .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @BeforeEach
    public void setUp() throws JsonProcessingException {

        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));
        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefData);
        when(locationRefDataService.getCcmccLocation(any())).thenReturn(locationRefData);
    }

    @Test
    void shouldGenerateFreeFormOrder_whenNoneSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderOnCourtInitiativeSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .orderOnCourtInitiative(FreeFormOrderValues.builder().onInitiativeSelectionTextArea("test").onInitiativeSelectionDate(
                LocalDate.now()).build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderWithoutNoticeIsSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .ccdState(CaseState.CASE_PROGRESSION)
            .orderWithoutNotice(FreeFormOrderValues.builder().withoutNoticeSelectionTextArea("test without notice")
                                    .withoutNoticeSelectionDate(LocalDate.now()).build())
            .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                             .partyID("app-2-party-id")
                             .partyName("Applicant2")
                             .build())
            .applicant2(PartyBuilder.builder().soleTrader().build().toBuilder()
                            .partyID("res-2-party-id")
                            .partyName("Respondent2")
                            .build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenOptionalSectionsNotPresent() {
        //Given: case data without recitals selected
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

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
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder().makeAnOrderForCostsList(null).build())
            .publicFundingCostsProtection(NO)
            // Appeal section
            .finalOrderAppealToggle(null)
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder()
                                              .ownInitiativeText("own initiative test")
                                              .ownInitiativeDate(LocalDate.now())
                                              .build())
            .finalOrderGiveReasonsYesNo(NO)
            .build();

        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenOtherOptions() {
        //Given: case data without recitals selected
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            // Order made section
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(
                LocalDate.now()).build()).build())
            // Papers considered
            .finalOrderJudgePapers(null)
            // judge heard from section
            .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                          .typeRepresentationList(FinalOrderRepresentationList.OTHER_REPRESENTATION)
                                          .typeRepresentationOtherComplex(ClaimantAndDefendantHeard
                                                                              .builder().detailsRepresentationText("Test").build()).build())
            // Order made on court's own initiative section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
            // Further hearing section
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                 .alternativeHearingList(null)
                                                 .hearingMethodList(IN_PERSON).build())
            // Costs section
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .makeAnOrderForCostsYesOrNo(YES)
                                                  .assistedOrderAssessmentSecondDropdownList1(INDEMNITY_BASIS)
                                                  .assistedOrderAssessmentSecondDropdownList2(CostEnums.YES)
                                                  .makeAnOrderForCostsList(COSTS)
                                                  .assistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(10000L))
                                                  .makeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                  .assistedOrderClaimantDefendantFirstDropdown(SUBJECT_DETAILED_ASSESSMENT).build())
            .publicFundingCostsProtection(YES)
            // Appeal section
            .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                         .applicationList(ApplicationAppealList.REFUSED)
                                         .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                    .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                    .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                     .build()).build()).build())
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder()
                                              .ownInitiativeText("own initiative test")
                                              .ownInitiativeDate(LocalDate.now())
                                              .build())
            .build();

        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_withAllDetails() {
        //Given: Case data with all fields for docmosis
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);
        DynamicListElement dynamicListElement = DynamicListElement.builder().label("test_label").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(Collections.singletonList(dynamicListElement))
            .value(dynamicListElement)
            .build();
        List<FinalOrdersJudgePapers> finalOrdersJudgePapersList =
            new ArrayList<>(Arrays.asList(FinalOrdersJudgePapers.CONSIDERED));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            // Order made section
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(
                LocalDate.now()).build()).build())
            //Papers considered
            .finalOrderJudgePapers(
                finalOrdersJudgePapersList)
            // judge heard from section
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .applicant2(PartyBuilder.builder().individual().build())
            .addApplicant2(YES)
            .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                          .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                          .typeRepresentationComplex(ClaimantAndDefendantHeard.builder().build()).build())
            // recitals section
            .finalOrderRecitals(toggleList)
            .finalOrderRecitalsRecorded(FinalOrderRecitalsRecorded.builder().text("Test").build())
            // further hearing section
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                 .alternativeHearingList(dynamicList)
                                                 .hearingMethodList(IN_PERSON)
                                                 .hearingNotesText("test hearing notes")
                                                 .datesToAvoidDateDropdown(DatesFinalOrders.builder().datesToAvoidDates(LocalDate.now())
                                                                               .build()).build())
            // Costs section
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .makeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                  .assistedOrderAssessmentSecondDropdownList2(CostEnums.NO)
                                                  .makeAnOrderForCostsList(COSTS)
                                                  .assistedOrderClaimantDefendantFirstDropdown(COSTS)
                                                  .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L))
                                                  .makeAnOrderForCostsYesOrNo(YesOrNo.YES).build())
            .assistedOrderCostsReserved(AssistedOrderCostDetails.builder().detailsRepresentationText("Test").build())
            .finalOrderGiveReasonsComplex(AssistedOrderReasons.builder().reasonsText("Test").build())
            .assistedOrderCostsBespoke(AssistedOrderCostDetails.builder().besPokeCostDetailsText("Test").build())
            .publicFundingCostsProtection(YES)
            // Appeal section
            .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                         .applicationList(ApplicationAppealList.GRANTED)
                                         .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                    .circuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                    .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                     .build()).build()).build())
            // initiative or without notice section
            .orderMadeOnDetailsList(OrderMadeOnTypes.WITHOUT_NOTICE)
            .orderMadeOnDetailsOrderWithoutNotice(OrderMadeOnDetailsOrderWithoutNotice.builder()
                                                      .withOutNoticeText("without notice test")
                                                      .withOutNoticeDate(LocalDate.now())
                                                      .build())
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void testDefendantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(
            FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            String response = generator.defendantAttendsOrRepresentedTextBuilder(caseData, false);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT -> assertEquals(format("Counsel for %s, the defendant.", name), response);
                case SOLICITOR_FOR_DEFENDANT -> assertEquals(format("Solicitor for %s, the defendant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT -> assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                case THE_DEFENDANT_IN_PERSON -> assertEquals(format("%s, the defendant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT -> assertEquals(format("A lay representative for %s, the defendant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(
            FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantTwoList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            String response = generator.defendantAttendsOrRepresentedTextBuilder(caseData, true);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT -> assertEquals(format("Counsel for %s, the defendant.", name), response);
                case SOLICITOR_FOR_DEFENDANT -> assertEquals(format("Solicitor for %s, the defendant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT -> assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                case THE_DEFENDANT_IN_PERSON -> assertEquals(format("%s, the defendant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT -> assertEquals(format("A lay representative for %s, the defendant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testClaimantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(
            FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationClaimantList(
                        finalOrdersClaimantRepresentationList).build()).build())
                .build();
            String name = caseData.getApplicant1().getPartyName();
            String response = generator.claimantAttendsOrRepresentedTextBuilder(caseData, false);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT -> assertEquals(format("Counsel for %s, the claimant.", name), response);
                case SOLICITOR_FOR_CLAIMANT -> assertEquals(format("Solicitor for %s, the claimant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT -> assertEquals(format("Costs draftsman for %s, the claimant.", name), response);
                case THE_CLAIMANT_IN_PERSON -> assertEquals(format("%s, the claimant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT -> assertEquals(format("A lay representative for %s, the claimant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testClaimantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(
            FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationClaimantListTwo(
                        finalOrdersClaimantRepresentationList).build()).build())
                .build();
            String name = caseData.getApplicant2().getPartyName();
            String response = generator.claimantAttendsOrRepresentedTextBuilder(caseData, true);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT -> assertEquals(format("Counsel for %s, the claimant.", name), response);
                case SOLICITOR_FOR_CLAIMANT -> assertEquals(format("Solicitor for %s, the claimant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT -> assertEquals(format("Costs draftsman for %s, the claimant.", name), response);
                case THE_CLAIMANT_IN_PERSON -> assertEquals(format("%s, the claimant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT -> assertEquals(format("A lay representative for %s, the claimant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantOneNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureComplex(TrialNoticeProcedure.builder().listDef(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            String response = generator.defendantNotAttendingText(caseData, false, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(
                    format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantTwoNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureDefTwoComplex(TrialNoticeProcedure.builder().listDefTwo(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            String response = generator.defendantNotAttendingText(caseData, true, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(
                    format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetClaimantOneNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureClaimantComplex(TrialNoticeProcedure.builder().list(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getApplicant1().getPartyName();
            String response = generator.claimantNotAttendingText(caseData, false, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the claimant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(format(
                    "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                        + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetClaimantTwoNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedClaimTwoComplex(TrialNoticeProcedure.builder().listClaimTwo(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getApplicant2().getPartyName();
            String response = generator.claimantNotAttendingText(caseData, true, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the claimant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(format(
                    "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                        + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetFurtherHearingLength() {
        for (HearingLengthFinalOrderList hearingLengthFinalOrderList : List.of(HearingLengthFinalOrderList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(
                        hearingLengthFinalOrderList)
                                                     .lengthListOther(CaseHearingLengthElement.builder()
                                                                          .lengthListOtherDays("12")
                                                                          .lengthListOtherHours("1")
                                                                          .lengthListOtherMinutes("30")
                                                                          .build()).build()).build();
            String response = generator.getFurtherHearingLength(caseData);
            switch (hearingLengthFinalOrderList) {
                case MINUTES_15 -> assertEquals("15 minutes", response);
                case MINUTES_30 -> assertEquals("30 minutes", response);
                case HOUR_1 -> assertEquals("1 hour", response);
                case HOUR_1_5 -> assertEquals("1.5 hours", response);
                case HOUR_2 -> assertEquals("2 hours", response);
                case OTHER -> assertEquals("12 days 1 hours 30 minutes", response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetFurtherHearingLengthOther() {
        CaseData minCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                        .lengthListOther(CaseHearingLengthElement.builder()
                                //.lengthListOtherDays("12")
                                //.lengthListOtherHours("1")
                                .lengthListOtherMinutes("30")
                                .build()).build()).build();
        String response = generator.getFurtherHearingLength(minCaseData);
        assertEquals("30 minutes", response);

        CaseData hourCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                        .lengthListOther(CaseHearingLengthElement.builder()
                                //.lengthListOtherDays("12")
                                .lengthListOtherHours("1")
                                //.lengthListOtherMinutes("30")
                                .build()).build()).build();
        response = generator.getFurtherHearingLength(hourCaseData);
        assertEquals("1 hours ", response);

        CaseData dayCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                        .lengthListOther(CaseHearingLengthElement.builder()
                                .lengthListOtherDays("12")
                                //.lengthListOtherHours("1")
                                //.lengthListOtherMinutes("30")
                                .build()).build()).build();
        response = generator.getFurtherHearingLength(dayCaseData);
        assertEquals("12 days ", response);
    }

    @Test
    void testGetFurtherHearingLengthWhenNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(null).build();

        String response = generator.getFurtherHearingLength(caseData);
        assertEquals("", response);
    }

    @Test
    void testGetFurtherHearingFromDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().listFromDate(LocalDate.of(2022, 12,
                                                                                                          12
            )).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, true);
        assertEquals(LocalDate.of(2022, 12,
                                  12
        ), response);
    }

    @Test
    void testGetFurtherHearingToDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().dateToDate(LocalDate.of(2022, 12,
                                                                                                        12
            )).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, false);
        assertEquals(LocalDate.of(2022, 12,
                                  12
        ), response);
    }

    @Test
    void testGetAppealFor() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().list(AppealList.CLAIMANT).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals(AppealList.CLAIMANT.name().toLowerCase() + "'s", response);
    }

    @Test
    void testGetAppealForOthers() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().otherText("test").list(AppealList.OTHER).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals("test", response);
    }

    @ParameterizedTest
    @MethodSource("testData")
    void orderMadeDateBuilder(CaseData caseData, String expectedResponse) {
        String response = generator.orderMadeDateBuilder(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                            .singleDate(LocalDate.of(
                                                                                                2023,
                                                                                                9,
                                                                                                15
                                                                                            ))
                                                                                            .build()).build()).build(),
                "on 15 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                           .dateRangeFrom(LocalDate.of(
                                                                                               2023,
                                                                                               9,
                                                                                               13
                                                                                           ))
                                                                                           .dateRangeTo(LocalDate.of(
                                                                                               2023,
                                                                                               9,
                                                                                               14
                                                                                           ))
                                                                                           .build()).build()).build(),
                "between 13 September 2023 and 14 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().bespokeRangeSelection(DatesFinalOrders.builder()
                                                                                              .bespokeRangeTextArea(
                                                                                                  "date between 12 feb 2023, and 14 feb 2023")
                                                                                              .build()).build()).build(),
                "on date between 12 feb 2023, and 14 feb 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().bespokeRangeSelection(null)
                                                                                              .build()).build(),
                null
            )
        );
    }

    @Test
    void testPopulateInterimPaymentText() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails
                                                  .builder().assistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                    10000L)).build())
            .build();
        String response = generator.populateInterimPaymentText(caseData);
        assertEquals(format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownAmount())), response);
    }

    @Test
    void testPopulateSummarilyAssessedText() {
        CaseData caseDataClaimant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .makeAnOrderForCostsList(CLAIMANT)
                                                  .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L)).build())
            .build();
        CaseData caseDataDefendant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .makeAnOrderForCostsList(DEFENDANT)
                                                  .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L)).build())
            .build();
        String responseClaimant = generator.populateSummarilyAssessedText(caseDataClaimant);
        String responseDefendant = generator.populateSummarilyAssessedText(caseDataDefendant);
        assertEquals(format(
            "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) "
                + "in the sum of £%s. Such sum shall be paid by 4pm on",
            MonetaryConversions.penniesToPounds(caseDataClaimant
                                                    .getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount())), responseClaimant);
        assertEquals(format(
            "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) "
                + "in the sum of £%s. Such sum shall be paid by 4pm on",
            MonetaryConversions.penniesToPounds(caseDataDefendant
                                                    .getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount())), responseDefendant);
    }

    @Test
    void testPopulateDetailedAssessmentText() {
        CaseData caseDataClaimant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .assistedOrderAssessmentSecondDropdownList1(INDEMNITY_BASIS)
                                                  .makeAnOrderForCostsList(CLAIMANT).build())
            .build();
        CaseData caseDataDefendant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .assistedOrderAssessmentSecondDropdownList1(COSTS)
                                                  .makeAnOrderForCostsList(DEFENDANT).build())
            .build();
        String responseClaimant = generator.populateDetailedAssessmentText(caseDataClaimant);
        String responseDefendant = generator.populateDetailedAssessmentText(caseDataDefendant);
        assertEquals("The claimant shall pay the defendant's costs to be subject to a "
                         + "detailed assessment on the indemnity basis if not agreed", responseClaimant);
        assertEquals("The defendant shall pay the claimant's costs to be subject to"
                         + " a detailed assessment on the standard basis if not agreed", responseDefendant);
    }

    @Test
    void testIsDefaultCourt() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing
                                                 .builder().hearingLocationList(DynamicList
                                                                                    .builder().value(DynamicListElement
                                                                                                         .builder()
                                                                                                         .code("LOCATION_LIST")
                                                                                                         .build())
                                                                                    .build()).build())
            .build();
        CaseData caseDataWhenFalse = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing
                                                 .builder().hearingLocationList(DynamicList
                                                                                    .builder().value(DynamicListElement
                                                                                                         .builder()
                                                                                                         .code("OTHER_LOCATION")
                                                                                                         .build())
                                                                                    .build()).build())
            .build();
        Boolean response = generator.isDefaultCourt(caseData);
        Boolean responseFalse = generator.isDefaultCourt(caseDataWhenFalse);
        assertEquals(true, response);
        assertEquals(false, responseFalse);

    }

    @ParameterizedTest
    @MethodSource("testCircuitOrHighCourtData")
    void testCircuitOrHighCourt(CaseData caseData, String expectedResponse) {
        String response = generator.circuitOrHighCourt(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testCircuitOrHighCourtData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .build()).build()).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .build()).build()).build(),
                "a"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.HIGH_COURT)
                                                                            .build()).build()).build(),
                "b"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                            .build()).build()).build(),
                "b"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetAppealDateData")
    void testGetAppealDate(CaseData caseData, LocalDate expectedResponse) {
        LocalDate response = generator.getAppealDate(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testGetAppealDateData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(1))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(1)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.CIRCUIT_COURT)
                                                                            .appealChoiceSecondDropdownA(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(10))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(10)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.REFUSED)
                                                 .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtListRefuse(ApplicationAppealList.HIGH_COURT)
                                                                            .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(5))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(5)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAppealComplex(FinalOrderAppeal
                                                 .builder().applicationList(ApplicationAppealList.GRANTED)
                                                 .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                            .circuitOrHighCourtList(ApplicationAppealList.HIGH_COURT)
                                                                            .appealChoiceSecondDropdownB(AppealChoiceSecondDropdown.builder()
                                                                                                             .appealGrantedRefusedDate(LocalDate.now().plusDays(5))
                                                                                                             .build()).build()).build()).build(),
                LocalDate.now().plusDays(5)
            )
        );
    }

    @Test
    void testGetInitiativeTextWithoutNotice() {
        CaseData caseDataInitiative = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeText("test initiative text").build())
            .build();
        String responseInitiative = generator.getInitiativeOrWithoutNotice(caseDataInitiative);
        assertEquals("test initiative text", responseInitiative);

        CaseData caseDataWithoutNotice = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderMadeOnDetailsList(OrderMadeOnTypes.WITHOUT_NOTICE)
            .orderMadeOnDetailsOrderWithoutNotice(OrderMadeOnDetailsOrderWithoutNotice.builder().withOutNoticeText("test without notice text").build())
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
