package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.CaseHearingLengthElement;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.TrialNoticeProcedure;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Autowired
    private JudgeFinalOrderGenerator generator;

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
            .orderWithoutNotice(FreeFormOrderValues.builder().withoutNoticeSelectionTextArea("test without notice")
                                    .withoutNoticeSelectionDate(LocalDate.now()).build())
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
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
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
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
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
            .finalOrderDateHeardComplex(OrderMade.builder().date(LocalDate.now()).build())
            .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationJudgePapersList(finalOrdersJudgePapersList)
                                          .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT).typeRepresentationOtherComplex(
                ClaimantAndDefendantHeard.builder().detailsRepresentationText("Test").build()).build())
            .finalOrderRecitals(toggleList)
            .finalOrderRecitalsRecorded(FinalOrderRecitalsRecorded.builder().text("Test").build())
            .assistedOrderCostsReserved(AssistedOrderCostDetails.builder().detailsRepresentationText("Test").build())
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().alternativeHearingList(dynamicList).build())
            .finalOrderAppealComplex(FinalOrderAppeal.builder().applicationList(ApplicationAppealList.GRANTED)
                                         .appealGranted(AppealGrantedRefused.builder().reasonsText("Test").build()).build())
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
    void testGetCostAmount() {
        for (AssistedCostTypesList assistedCostTypesList : List.of(AssistedCostTypesList.values())) {
            if (assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderRecitals(null)
                    .assistedOrderCostList(assistedCostTypesList)
                    .assistedOrderCostsClaimantPaySub(AssistedOrderCostDetails.builder().claimantCostStandardText(
                        "12.12").claimantCostStandardDate(LocalDate.of(2022, 1, 1)).build())
                    .assistedOrderCostsClaimantSum(AssistedOrderCostDetails.builder().claimantCostSummarilyText("12.12").build())
                    .assistedOrderCostsDefendantSum(AssistedOrderCostDetails.builder().defendantCostSummarilyText(
                        "12.12").build())
                    .assistedOrderCostsDefendantPaySub(AssistedOrderCostDetails.builder().defendantCostStandardText(
                        "12.12").build()).build();
                String response = generator.getCostAmount(caseData);
                assertEquals("12.12", response);
            }
        }
    }

    @Test
    void testGetCostProtection() {
        for (AssistedCostTypesList assistedCostTypesList : List.of(AssistedCostTypesList.values())) {
            if (assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderRecitals(null)
                    .assistedOrderCostList(assistedCostTypesList)
                    .assistedOrderCostsClaimantPaySub(AssistedOrderCostDetails.builder().claimantCostStandardProtectionOption(
                        YesOrNo.NO).claimantCostStandardDate(LocalDate.of(2022, 1, 1)).build())
                    .assistedOrderCostsClaimantSum(AssistedOrderCostDetails.builder().claimantCostSummarilyProtectionOption(
                        YesOrNo.NO).build())
                    .assistedOrderCostsDefendantSum(AssistedOrderCostDetails.builder().defendantCostSummarilyProtectionOption(
                        YesOrNo.NO).build())
                    .assistedOrderCostsDefendantPaySub(AssistedOrderCostDetails.builder().defendantCostStandardProtectionOption(
                        YesOrNo.NO).build()).build();
                YesOrNo response = generator.getCostProtection(caseData);
                assertEquals(YesOrNo.NO, response);
            }
        }
    }

    @Test
    void testGetPaidByDate() {
        for (AssistedCostTypesList assistedCostTypesList : List.of(AssistedCostTypesList.values())) {
            if (assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_STANDARD_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.DEFENDANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_SUMMARILY_BASE)
                || assistedCostTypesList.equals(AssistedCostTypesList.CLAIMANT_COST_STANDARD_BASE)) {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderRecitals(null)
                    .assistedOrderCostList(assistedCostTypesList)
                    .assistedOrderCostsClaimantPaySub(AssistedOrderCostDetails.builder().claimantCostStandardDate(
                        LocalDate.of(2022, 1, 1)).build())
                    .assistedOrderCostsClaimantSum(AssistedOrderCostDetails.builder().claimantCostSummarilyDate(
                        LocalDate.of(2022, 1, 1)).build())
                    .assistedOrderCostsDefendantSum(AssistedOrderCostDetails.builder().defendantCostSummarilyDate(
                        LocalDate.of(2022, 1, 1)).build())
                    .assistedOrderCostsDefendantPaySub(AssistedOrderCostDetails.builder().defendantCostStandardDate(
                        LocalDate.of(2022, 1, 1)).build()).build();
                LocalDate response = generator.getPaidByDate(caseData);
                assertEquals(LocalDate.of(2022, 1, 1), response);
            }
        }
    }

    @Test
    void testGetAppealReasonGranted() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealToggle(toggleList)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().applicationList(ApplicationAppealList.GRANTED).appealGranted(
                AppealGrantedRefused.builder().reasonsText("test").build()).build())
            .build();
        String response = generator.getAppealReason(caseData);
        assertEquals("test", response);
    }

    @Test
    void testGetAppealReasonNotGranted() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealToggle(toggleList)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().applicationList(ApplicationAppealList.REFUSED).appealRefused(
                AppealGrantedRefused.builder().refusedText("test").build()).build())
            .build();
        String response = generator.getAppealReason(caseData);
        assertEquals("test", response);
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
                                                                      .lengthListOtherDays(new BigDecimal(12))
                                                                      .lengthListOtherHours(new BigDecimal(1))
                                                                      .lengthListOtherMinutes(new BigDecimal(30)).build()).build()).build();
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

