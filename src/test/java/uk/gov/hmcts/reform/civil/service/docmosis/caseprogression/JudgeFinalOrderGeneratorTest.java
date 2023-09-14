package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

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
import uk.gov.hmcts.reform.civil.model.finalorders.TrialNoticeProcedure;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
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
    private static final String fileFreeForm = String.format(FREE_FORM_ORDER_PDF.getDocumentTitle(), LocalDate.now());
    private static final String assistedForm = String.format(ASSISTED_ORDER_PDF.getDocumentTitle(), LocalDate.now());
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

    private static LocationRefData locationRefData =   LocationRefData.builder().siteName("SiteName")
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
            .ccdState(CaseState.JUDICIAL_REFERRAL)
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
    void shouldGenerateAssistedFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        //Given: case data with claimant and defendant ref not added
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(LocalDate.now()).build()).build())
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenRecitalsNotSelected() {
        //Given: case data without recitals selected
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(LocalDate.now()).build()).build())
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
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
            .finalOrderRecitals(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(LocalDate.now()).build()).build())
            .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationJudgePapersList(finalOrdersJudgePapersList)
                                          .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT).typeRepresentationOtherComplex(
                ClaimantAndDefendantHeard.builder().detailsRepresentationText("Test").build()).build())
            .finalOrderRecitals(toggleList)
            .finalOrderRecitalsRecorded(FinalOrderRecitalsRecorded.builder().text("Test").build())
            .assistedOrderCostsReserved(AssistedOrderCostDetails.builder().detailsRepresentationText("Test").build())
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().alternativeHearingList(dynamicList).build())
            .finalOrderGiveReasonsComplex(AssistedOrderReasons.builder().reasonsText("Test").build())
            .assistedOrderCostsBespoke(AssistedOrderCostDetails.builder().besPokeCostDetailsText("Test").build())
            .finalOrderAppealToggle(toggleList)
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void testGetRepresentedDefendant() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String response = generator.getRepresentedDefendant(caseData);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT:
                    assertEquals("counsel for defendant", response);
                    break;
                case SOLICITOR_FOR_DEFENDANT:
                    assertEquals("solicitor for defendant", response);
                    break;
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                    assertEquals("costs draftsman for the defendant", response);
                    break;
                case THE_DEFENDANT_IN_PERSON:
                    assertEquals("the defendant in person", response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                    assertEquals("lay representative for the defendant", response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetRepresentedClaimant() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationClaimantList(
                        finalOrdersClaimantRepresentationList).build()).build())
                .build();
            String response = generator.getRepresentedClaimant(caseData);
            switch (finalOrdersClaimantRepresentationList) {
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                    assertEquals("costs draftsman for the claimant", response);
                    break;
                case COUNSEL_FOR_CLAIMANT:
                    assertEquals("counsel for claimant", response);
                    break;
                case SOLICITOR_FOR_CLAIMANT:
                    assertEquals("solicitor for claimant", response);
                    break;
                case THE_CLAIMANT_IN_PERSON:
                    assertEquals("the claimant in person", response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                    assertEquals("lay representative for the claimant", response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetDefendantNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureComplex(TrialNoticeProcedure.builder().listDef(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String response = generator.getDefendantNotAttendedText(caseData);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals("The defendant did not attend the trial, but the" +
                                     " Judge was not satisfied that they had received notice" +
                                     " of the hearing and it was not reasonable to proceed in their absence", response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals("The defendant did not attend the trial and whilst the Judge was satisfied " +
                        "that they had received notice of the trial it was not reasonable to proceed in their absence",
                        response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals(
                        "The defendant did not attend the trial," +
                            " but the Judge was satisfied that they had received notice of the trial and it was " +
                            "reasonable to proceed in their absence",
                        response
                    );
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetClaimantNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureClaimantComplex(TrialNoticeProcedure.builder().list(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String response = generator.getClaimantNotAttendedText(caseData);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals("The claimant did not attend the trial, " +
                        "but the Judge was not satisfied that they had received notice of the hearing and it was not " +
                                 "reasonable to proceed in their absence", response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals("The claimant did not attend the trial and whilst the Judge was satisfied that they had " +
                                     "received notice of the trial it was not reasonable to proceed in their absence",
                        response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals("The claimant did not attend the trial, but the Judge was satisfied that they had " +
                                     "received notice of the trial and it was reasonable to proceed in their absence",
                                 response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetFurtherHearingLength() {
        for (HearingLengthFinalOrderList hearingLengthFinalOrderList : List.of(HearingLengthFinalOrderList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(
                    hearingLengthFinalOrderList).build()).build();
            String response = generator.getFurtherHearingLength(caseData);
            switch (hearingLengthFinalOrderList) {
                case MINUTES_15:
                    assertEquals("15 minutes", response);
                    break;
                case MINUTES_30:
                    assertEquals("30 minutes", response);
                    break;
                case HOUR_1:
                    assertEquals("1 hour", response);
                    break;
                case HOUR_1_5:
                    assertEquals("1.5 hours", response);
                    break;
                case HOUR_2:
                    assertEquals("2 hours", response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetFurtherHearingLengthForOther() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                 .lengthListOther(CaseHearingLengthElement.builder()
                                                                      .lengthListOtherDays("12")
                                                                      .lengthListOtherHours("1")
                                                                      .lengthListOtherMinutes("30").build()).build()).build();
        String response = generator.getFurtherHearingLength(caseData);
        assertEquals("12 days 1 hours 30 minutes", response);
    }

    @Test
    void testGetIfAttended() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                ClaimantAndDefendantHeard.builder().typeRepresentationDefendantList(FinalOrdersDefendantRepresentationList.THE_DEFENDANT_IN_PERSON).build()).build()).build();
        boolean response = generator.getIfAttended(caseData, true);
        assertEquals(true, response);

        CaseData caseDataClaimant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                ClaimantAndDefendantHeard.builder().typeRepresentationClaimantList(FinalOrdersClaimantRepresentationList.THE_CLAIMANT_IN_PERSON).build()).build()).build();
        response = generator.getIfAttended(caseDataClaimant, false);
        assertEquals(true, response);
    }

    @Test
    void testGetFurtherHearingFromDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().listFromDate(LocalDate.of(2022, 12,
                                                                                                          12)).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, true);
        assertEquals(LocalDate.of(2022, 12,
                                  12), response);
    }

    @Test
    void testGetFurtherHearingToDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().dateToDate(LocalDate.of(2022, 12,
                                                                                                          12)).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, false);
        assertEquals(LocalDate.of(2022, 12,
                                  12), response);
    }

    @Test
    void testGetAppealFor() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().list(AppealList.CLAIMANT).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals(AppealList.CLAIMANT.name().toLowerCase(), response);
    }

    @Test
    void testGetAppealForOthers() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().otherText("test").list(AppealList.OTHER).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals("test", response);
    }
}

