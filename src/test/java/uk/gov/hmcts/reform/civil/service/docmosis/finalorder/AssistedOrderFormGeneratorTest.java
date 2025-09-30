package uk.gov.hmcts.reform.civil.service.docmosis.finalorder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.AppealOriginTypes;
import uk.gov.hmcts.reform.civil.enums.dq.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.dq.AssistedOrderCostDropdownList;
import uk.gov.hmcts.reform.civil.enums.dq.ClaimantDefendantNotAttendingType;
import uk.gov.hmcts.reform.civil.enums.dq.ClaimantRepresentationType;
import uk.gov.hmcts.reform.civil.enums.dq.DefendantRepresentationType;
import uk.gov.hmcts.reform.civil.enums.dq.FinalOrderConsideredToggle;
import uk.gov.hmcts.reform.civil.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.enums.dq.HeardFromRepresentationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.LengthOfHearing;
import uk.gov.hmcts.reform.civil.enums.dq.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.enums.dq.PermissionToAppealTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.AssistedOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.HearingLength;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AppealTypeChoiceList;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AppealTypeChoices;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderAppealDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderDateHeard;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderGiveReasonsDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderHeardRepresentation;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderMadeDateHeardDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderRecitalRecord;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.ClaimantDefendantRepresentation;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.DetailText;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.DetailTextWithDate;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.HeardClaimantNotAttend;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.HeardDefendantNotAttend;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.HeardDefendantTwoNotAttend;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_ASSISTED_ORDER_FORM_LIP;

@SpringBootTest(classes = {
    AssistedOrderFormGenerator.class
})
class AssistedOrderFormGeneratorTest {

    private static final String RECITAL_RECORDED_TEXT = "It is recorded that";
    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String CLAIMANT_SUMMARILY_ASSESSED_TEXT = "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) " +
        "in the sum of £789.00. " +
        "Such sum shall be paid by 4pm on";
    private static final String DEFENDANT_SUMMARILY_ASSESSED_TEXT = "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) " +
        "in the sum of £789.00. " +
        "Such sum shall be paid by 4pm on";
    private static final String CLAIMANT_DETAILED_INDEMNITY_ASSESSED_TEXT = "The claimant shall pay the defendant's costs to be subject to a detailed assessment " +
        "on the indemnity basis if not agreed";
    private static final String CLAIMANT_DETAILED_STANDARD_ASSESSED_TEXT = "The claimant shall pay the defendant's costs to be subject to a detailed assessment " +
        "on the standard basis if not agreed";
    private static final String DEFENDANT_DETAILED_INDEMNITY_ASSESSED_TEXT = "The defendant shall pay the claimant's costs to be subject to a detailed assessment " +
        "on the indemnity basis if not agreed";
    private static final String DEFENDANT_DETAILED_STANDARD_ASSESSED_TEXT = "The defendant shall pay the claimant's costs to be subject to a detailed assessment " +
        "on the standard basis if not agreed";
    private static final String TEST_TEXT = "Test 123";
    private static final String OTHER_ORIGIN_TEXT = "test other origin text";

    private static final String INTERIM_PAYMENT_TEXT = "An interim payment of £500.00 on account of costs shall be paid by 4pm on";

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private DocmosisService docmosisService;
    @Autowired
    private AssistedOrderFormGenerator generator;

    @Nested
    class CostTextValues {

        @Test
        void shouldReturnText_WhenSelected_CostsReserved() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.COSTS_RESERVED)
                .costReservedDetails(DetailText.builder().detailText(TEST_TEXT).build()).build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnNull_WhenSelected_CostsReserved_detailTextIsEmpty() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.COSTS_RESERVED)
                .build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnValue_WhenSelected_BeSpokeCostOrder() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.BESPOKE_COSTS_ORDER)
                .assistedOrderCostsBespokeGA(BeSpokeCostDetailText.builder().detailText("test").build())
                .build();
            String assistedOrderString = generator.getBespokeCostOrderText(caseData);

            assertThat(assistedOrderString).isEqualTo("\n\n" + "test");
        }

        @Test
        void shouldReturnNull_WhenSelected_NotCostsReserved() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.NO_ORDER_TO_COST)
                .build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_SummarilyAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_SUMMARILY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_SummarilyAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_SUMMARILY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder().build()).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_SubjectSummarilyAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_SummarilyAssessedDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5)).build()).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsIsNull_SummarilyAssessedDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .build()).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsListIsNull_SummarilyAssessedDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5)).build()).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsListDropdownIsNotCosts_SummarilyAssessedDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5)).build()).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_DetailedAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS).build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_DETAILED_INDEMNITY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_StandardDetailedAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.STANDARD_BASIS).build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_DETAILED_STANDARD_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_StandardDetailedAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.STANDARD_BASIS).build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_DETAILED_STANDARD_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_IndemnityDetailedAssessed() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS).build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_DETAILED_INDEMNITY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_Detailed_Assessment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder().build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_Detailed_Assessment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsDetailed_Assessment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_InterimPayment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder().build()).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_InterimPayment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPayment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPayment_No() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.NO).build()).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_InterimPayment() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)
                                                      .assistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.YES)
                                                      .assistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                                                          50000)).build()).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).contains(INTERIM_PAYMENT_TEXT);
        }

        @Test
        void shouldReturnDate_WhenSelected_Defendant_InterimPaymentDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .assistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)
                                                      .assistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.YES)
                                                      .assistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                                                          50000))
                                                      .assistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(
                                                          10)).build()).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isEqualTo(LocalDate.now().plusDays(10));
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_InterimPaymentDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder().build()).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_InterimPaymentDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPaymentDate() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnTrueWhenQocsProtectionEnabled() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .makeAnOrderForCostsYesOrNo(YesOrNo.YES)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isTrue();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .makeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled_YesOrNoIsNull() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                      .makeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .assistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build()).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled_MakeAnOrderForCostsIsNull() {
            CaseData caseData = CaseData.builder().assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder().build()).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }
    }

    @Nested
    class FurtherHearing {

        private final List<FinalOrderShowToggle> furtherHearingShowOption = new ArrayList<>();

        @Test
        void shouldReturnNull_When_FurtherHearing_NotSelected() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingToggle(null)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_FurtherHearingOption_Null() {
            furtherHearingShowOption.add(null);
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_FurtherHearingOption_NotShow() {
            furtherHearingShowOption.add(FinalOrderShowToggle.HIDE);
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_FurtherHearingOption_Show() {
            furtherHearingShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnYes_When_FurtherHearing_CheckListToDateExists() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().listToDate(LocalDate.now().minusDays(5)).build())
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_CheckListToDateDoesNotExists() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().listFromDate(LocalDate.now().minusDays(5)).build())
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_ListToDateDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().build())
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldReturnYes_When_FurtherHearing_ListToDateExists() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().listToDate(LocalDate.now().minusDays(5)).build())
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnListToDate_When_FurtherHearing_ListToDateDoesNotExists() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().listFromDate(LocalDate.now().minusDays(5)).build())
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isNull();
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_DetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().build())
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isNull();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_ListFromDate() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder().listFromDate(LocalDate.now().minusDays(5)).build())
                .build();
            LocalDate getListFromDate = generator.getFurtherHearingListFromDate(caseData);
            assertThat(getListFromDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_ListFromDateDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            LocalDate getListFromDate = generator.getFurtherHearingListFromDate(caseData);
            assertThat(getListFromDate).isNull();
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingMethodExists() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            String hearingMethodText = generator.getFurtherHearingMethod(caseData);
            assertThat(hearingMethodText).contains("TELEPHONE");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_HearingMethodDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            String hearingMethodText = generator.getFurtherHearingMethod(caseData);
            assertThat(hearingMethodText).isNull();
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingDurationExists_Hours() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.HOURS_2)
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).contains("2 hours");
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingDurationExists_Other() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("2 days 5 hours 30 minutes");

            caseData = CaseData.builder()
                    .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                            .listFromDate(LocalDate.now().minusDays(5))
                            .lengthOfNewHearing(LengthOfHearing.OTHER)
                            .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(0)
                                    .lengthListOtherHours(5)
                                    .lengthListOtherMinutes(30).build())
                            .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                    .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 hours 30 minutes");

            caseData = CaseData.builder()
                    .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                            .listFromDate(LocalDate.now().minusDays(5))
                            .lengthOfNewHearing(LengthOfHearing.OTHER)
                            .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(0)
                                    .lengthListOtherHours(5)
                                    .lengthListOtherMinutes(0).build())
                            .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                    .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 hours");

            caseData = CaseData.builder()
                    .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                            .listFromDate(LocalDate.now().minusDays(5))
                            .lengthOfNewHearing(LengthOfHearing.OTHER)
                            .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(5)
                                    .lengthListOtherHours(0)
                                    .lengthListOtherMinutes(0).build())
                            .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                    .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 days");

            caseData = CaseData.builder()
                    .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                            .listFromDate(LocalDate.now().minusDays(5))
                            .lengthOfNewHearing(LengthOfHearing.OTHER)
                            .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(0)
                                    .lengthListOtherHours(0)
                                    .lengthListOtherMinutes(30).build())
                            .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                    .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("30 minutes");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_HearingDurationDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isNull();
        }

        @Test
        void shouldReturnTrue_When_FurtherHearing_checkDatesToAvoidIsYes() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.YES)
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isTrue();
        }

        @Test
        void shouldReturnTrue_When_FurtherHearing_checkDatesToAvoidIsNo() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.NO)
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isFalse();
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_checkDatesToAvoidDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isFalse();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingDatesToAvoidIsYes() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.YES)
                                                        .datesToAvoidDateDropdown(AssistedOrderDateHeard.builder().datesToAvoidDates(LocalDate.now().plusDays(7))
                                                                                      .build())
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isEqualTo(LocalDate.now().plusDays(7));
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingDatesToAvoidIsNo() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.NO)
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isNull();
        }

        @Test
        void shouldReturnNull_When_FurtherHearing_DatesToAvoidDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isNull();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingLocationIsOtherLocation() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.NO)
                                                        .hearingLocationList(DynamicList.builder().value(
                                                            DynamicListElement.builder().label("Other location").build()).build())
                                                        .alternativeHearingLocation(DynamicList.builder().value(
                                                            DynamicListElement.builder().label("Site Name 2 - Address2 - 28000").build()).build())
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            String hearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(hearingLocation).contains("Site Name 2 - Address2 - 28000");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingLocationIsCcmcc() {
            CaseData caseData = CaseData.builder()
                .locationName("ccmcc location")
                .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                        .listFromDate(LocalDate.now().minusDays(5))
                                                        .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                                  .lengthListOtherHours(5)
                                                                                  .lengthListOtherMinutes(30).build())
                                                        .datesToAvoid(YesOrNo.NO)
                                                        .hearingLocationList(DynamicList.builder().value(
                                                            DynamicListElement.builder().label("ccmcc location").build()).build())
                                                        .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
                .build();
            String hearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(hearingLocation).contains("ccmcc location");
        }

        @Test
        void shouldReturnNull_When_FurtherHearing_LocationDetailsNotFound() {
            CaseData caseData = CaseData.builder()
                .assistedOrderFurtherHearingDetails(null).build();
            String furtherHearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(furtherHearingLocation).isNull();
        }
    }

    @Nested
    class Recitals {
        @Test
        void shouldReturnNull_When_Recitals_NotSelected() {
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitals(null)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_RecitalOption_Null() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(null);
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_RecitalOption_NotShow() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.HIDE);
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_RecitalOption_Show() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnNull_When_recitalsRecordedIsNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitalsRecorded(null)
                .build();
            String assistedOrderString = generator.getRecitalRecordedText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnText_When_recitalsRecordedIsNotNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderRecitalsRecorded(AssistedOrderRecitalRecord.builder().text(RECITAL_RECORDED_TEXT).build())
                .build();
            String assistedOrderString = generator.getRecitalRecordedText(caseData);
            assertThat(assistedOrderString).contains(RECITAL_RECORDED_TEXT);
        }
    }

    @Nested
    class JudgeHeardFrom {

        private final List<FinalOrderShowToggle> judgeHeardFromShowOption = new ArrayList<>();

        @Test
        void shouldReturnNull_When_judgeHeardFromShowOption_NotSelected() {
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(null)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_judgeHeardFromShowOptionOption_Null() {
            judgeHeardFromShowOption.add(null);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_judgeHeardFromShowOption_NotShow() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.HIDE);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_judgeHeardFromShowOption_Show() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnValue_When_RepresentationTypeIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .build())
                .build();
            String representationType = generator.getJudgeHeardFromRepresentation(caseData);
            assertThat(representationType).contains("Claimant and Defendant");
        }

        @Test
        void shouldReturnNull_When_RepresentationTypeIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getJudgeHeardFromRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_ClaimantRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            String claimantRepresentationType = generator.getClaimantRepresentation(caseData);
            assertThat(claimantRepresentationType).contains("Counsel for claimant");
        }

        @Test
        void shouldReturnNull_When_ClaimantRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getClaimantRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            String defendantRepresentationType = generator.getDefendantRepresentation(caseData);
            assertThat(defendantRepresentationType).contains("Solicitor for defendant");
        }

        @Test
        void shouldReturnNull_When_DefendantRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getDefendantRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantTwoRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .defendantTwoRepresentation(DefendantRepresentationType.COST_DRAFTSMAN_FOR_THE_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            String defendantRepresentationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(defendantRepresentationType).contains("Cost draftsman for the defendant");
        }

        @Test
        void shouldReturnValue_When_DefendantTwoRepresentationIsNotNull_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            String defendantRepresentationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(defendantRepresentationType).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnTrue_When_applicationIs_1v2() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            Boolean multipartyFlag = generator.checkIsMultiparty(caseData);
            assertThat(multipartyFlag).isTrue();
        }

        @Test
        void shouldReturnFalse_When_applicationIs_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT).build())
                                                 .build())
                .build();
            Boolean multipartyFlag = generator.checkIsMultiparty(caseData);
            assertThat(multipartyFlag).isFalse();
        }

        @Test
        void shouldReturnValue_When_DefendantNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.SOLICITOR_FOR_CLAIMANT)
                                                                                      .heardFromDefendantNotAttend(
                                                                                          HeardDefendantNotAttend.builder()
                                                                                                  .listDef(ClaimantDefendantNotAttendingType.NOT_GIVEN_NOTICE_OF_APPLICATION)
                                                                                              .build()).build())
                                                 .build())
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).contains("The defendant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_DefendantNotAttending_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION).build())
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantTwoNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantTwoRepresentation(DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT)
                                                                                      .heardFromDefendantTwoNotAttend(HeardDefendantTwoNotAttend.builder()
                                                                                              .listDefTwo(ClaimantDefendantNotAttendingType.NOT_GIVEN_NOTICE_OF_APPLICATION)
                                                                                                                          .build())
                                                                                      .build())
                                                 .build())
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).contains("The defendant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttending_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION).build())
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttending_OtherRepresentation_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION).build())
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnValue_When_ClaimantNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .claimantDefendantRepresentation(ClaimantDefendantRepresentation.builder()
                                                                                      .defendantRepresentation(DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .claimantRepresentation(ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING)
                                                                                      .heardFromClaimantNotAttend(
                                                                                          HeardClaimantNotAttend.builder()
                                                                                                  .listClaim(ClaimantDefendantNotAttendingType
                                                                                                                 .NOT_GIVEN_NOTICE_OF_APPLICATION_CLAIMANT)
                                                                                              .build()).build())
                                                 .build())
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).contains("The claimant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION).build())
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_ClaimantNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).isNull();
        }

        @Test
        void shouldReturnTrue_When_SelectionIs_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION).build())
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isTrue();
        }

        @Test
        void shouldReturnFalse_When_SelectionIs_ClaimantOrDefendantRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT).build())
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isFalse();
        }

        @Test
        void shouldReturnFalse_When_SelectionDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isFalse();
        }

        @Test
        void shouldReturnText_When_SelectionIs_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .otherRepresentation(DetailText.builder().detailText(OTHER_ORIGIN_TEXT).build()).build())
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).contains(OTHER_ORIGIN_TEXT);
        }

        @Test
        void shouldReturnNull_When_SelectionIs_ClaimantOrDefendantRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT).build())
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).isNull();
        }

        @Test
        void shouldReturnNull_When_SelectionDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).isNull();
        }

        @Test
        void shouldReturnTrue_When_JudgeConsideredPapers() {
            List<FinalOrderConsideredToggle> judgeConsideredPapers = new ArrayList<>();
            judgeConsideredPapers.add(FinalOrderConsideredToggle.CONSIDERED);
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .typeRepresentationJudgePapersList(judgeConsideredPapers)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()

                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .otherRepresentation(DetailText.builder().detailText(OTHER_ORIGIN_TEXT).build()).build())
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isTrue();
        }

        @Test
        void shouldReturnFalse_When_JudgeNotConsideredPapers() {
            List<FinalOrderConsideredToggle> judgeConsideredPapers = new ArrayList<>();
            judgeConsideredPapers.add(FinalOrderConsideredToggle.NOT_CONSIDERED);
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .typeRepresentationJudgePapersList(judgeConsideredPapers)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .otherRepresentation(DetailText.builder().detailText(OTHER_ORIGIN_TEXT).build()).build())
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }

        @Test
        void shouldReturnFalse_When_JudgePapersListIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                                 .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .otherRepresentation(DetailText.builder().detailText(OTHER_ORIGIN_TEXT).build()).build())
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }

        @Test
        void shouldReturnFalse_When_JudgePapersListDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            CaseData caseData = CaseData.builder()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }
    }

    @Nested
    class OrderMadeOn {

        @Test
        void shouldReturnTrueWhenOrderMadeWithInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isTrue();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeWithWithoutNotice() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeWithOherTypeExceptWithOutNoticeOrInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.NONE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isFalse();
        }

        @Test
        void shouldReturnTrueWhen_OrderMadeWithInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            Boolean checkInitiative = generator.checkInitiative(caseData);
            assertThat(checkInitiative).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeWithOherTypeExceptInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiative(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isFalse();
        }

        @Test
        void shouldReturnTextWhen_OrderMadeWithInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnTextWhen_OrderMadeWithWithoutNotice() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnNullWhen_OrderMadeWithNone() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.NONE)
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains("");
        }

        @Test
        void shouldReturnInitiativeDateWhen_OrderMadeWithInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtInitiativeDate(caseData);
            assertThat(orderMadeDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnInitiativeDateNullWhen_OrderMadeWithoutNotice() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtInitiativeDate(caseData);
            assertThat(orderMadeDate).isNull();
        }

        @Test
        void shouldReturnWithoutNoticeDateWhen_OrderMadeWithoutNotice() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtWithOutNoticeDate(caseData);
            assertThat(orderMadeDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnWithoutNoticeDateNull_When_OrderMadeWithInitiative() {
            CaseData caseData = CaseData.builder()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                              .detailText(TEST_TEXT)
                                              .date(LocalDate.now())
                                              .build())
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtWithOutNoticeDate(caseData);
            assertThat(orderMadeDate).isNull();
        }
    }

    @Nested
    class AppealSection {

        @Test
        void shouldReturnNull_When_OrderAppeal_NotSelected() {
            CaseData caseData = CaseData.builder()
                .assistedOrderAppealToggle(null)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_OrderAppealOption_Null() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(null);
            CaseData caseData = CaseData.builder()
                .assistedOrderAppealToggle(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_OrderAppealOption_NotShow() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.HIDE);
            CaseData caseData = CaseData.builder()
                .assistedOrderAppealToggle(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Claimant() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.CLAIMANT).build()).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("claimant");
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Defendant() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT).build()).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("defendant");
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Other() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.OTHER)
                                                                                  .otherOriginText("test other origin text").build()).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains(OTHER_ORIGIN_TEXT);
        }

        @Test
        void shouldReturnNull_When_AppealDetails_Null() {
            CaseData caseData = CaseData.builder().build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("");
        }

        @Test
        void shouldReturnNull_When_AppealOrigin_isNull() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .build()).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("");
        }

        @Test
        void shouldReturnTrueWhenIsAppealGranted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.GRANTED).build()).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isTrue();
        }

        @Test
        void shouldReturnFalseWhenAppealIsRefused() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED).build()).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isFalse();
        }

        @Test
        void shouldReturnFalseWhenIsAppealNotGranted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED)
                                                                                  .build()).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isFalse();
        }

        @Test
        void shouldReturnText_WhentableAorBIsSelected_Granted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.GRANTED)
                                                                                  .appealTypeChoicesForGranted(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                                                                                          .build()).build()).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("A");
        }

        @Test
        void shouldReturnText_WhentableAorBIsSelected_Refused() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED)
                                                                                  .appealTypeChoicesForRefused(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelectionRefuse(
                                                                                              PermissionToAppealTypes.CIRCUIT_COURT_JUDGE).build()).build()).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("A");
        }

        @Test
        void shouldReturnText_WhentableBIsSelected_Refused() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED)
                                                                                  .appealTypeChoicesForRefused(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelectionRefuse(
                                                                                              PermissionToAppealTypes.HIGH_COURT_JUDGE)
                                                                                          .build()).build()).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("B");
        }

        @Test
        void shouldReturnText_WhentableBIsSelected_Granted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.GRANTED)
                                                                                  .appealTypeChoicesForGranted(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelection(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                                                                                          .build()).build()).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("B");
        }

        @Test
        void shouldReturnAppealDate_WhentableA_Granted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.GRANTED)
                                                                                  .appealTypeChoicesForGranted(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                                                                                          .appealChoiceOptionA(
                                                                                              AppealTypeChoiceList.builder()
                                                                                                  .appealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                                                                                  .build()).build()).build()).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableB_Granted() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.GRANTED)
                                                                                  .appealTypeChoicesForGranted(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelection(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                                                                                          .appealChoiceOptionB(
                                                                                              AppealTypeChoiceList.builder()
                                                                                                  .appealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                                                                                  .build()).build()).build()).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableA_Refused() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED)
                                                                                  .appealTypeChoicesForRefused(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelectionRefuse(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                                                                                          .appealChoiceOptionA(
                                                                                              AppealTypeChoiceList.builder()
                                                                                                  .appealGrantedRefusedDate(LocalDate.now().plusDays(14)).build())
                                                                                          .build()).build()).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableB_Refused() {
            CaseData caseData = CaseData.builder().assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                                                                  .appealOrigin(AppealOriginTypes.DEFENDANT)
                                                                                  .permissionToAppeal(PermissionToAppealTypes.REFUSED)
                                                                                  .appealTypeChoicesForRefused(
                                                                                      AppealTypeChoices.builder()
                                                                                          .assistedOrderAppealJudgeSelectionRefuse(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                                                                                          .appealChoiceOptionB(
                                                                                              AppealTypeChoiceList.builder()
                                                                                                  .appealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                                                                                  .build())
                                                                                          .build()).build()).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnNullAppealDate_WhenAssistedOrderAppealDetailsAreNull() {
            CaseData caseData = CaseData.builder().build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isNull();
        }
    }

    @Nested
    class OrderMadeDate {

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsSingleDate() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                    AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotSingleDate() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(10)).build()).build())
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExist() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnSingleDateWhenOrderMadeSelectionIsSingleDate() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                    AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnNullWhenOrderMadeSelectionIsNo() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnNullWhenOrderMadeHeardsDetailsAreNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().build())
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(10))
                        .dateRangeTo(LocalDate.now().minusDays(5)).build()).build())
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                    AssistedOrderDateHeard.builder().singleDate(LocalDate.now().minusDays(10)).build()).build())
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExistForDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnDateRangeFrom_WhenOrderMadeSelectionIsDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(10))
                        .dateRangeTo(LocalDate.now().minusDays(5)).build()).build())
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isEqualTo(LocalDate.now().minusDays(10));
        }

        @Test
        void shouldNotReturnDateRangeFrom_WhenOrderMadeSelectionIsDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder()
                        .dateRangeTo(LocalDate.now().minusDays(5)).build()).build())
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldNotReturnDateRangeFrom_WhenOrderMadeSelectionIsNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldReturnDateRangeTo_WhenOrderMadeSelectionIsDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder().dateRangeFrom(LocalDate.now().minusDays(10))
                        .dateRangeTo(LocalDate.now().minusDays(5)).build()).build())
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldNotReturnDateRangeTo_WhenOrderMadeSelectionIsDateRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().dateRangeSelection(
                    AssistedOrderDateHeard.builder()
                        .dateRangeFrom(LocalDate.now().minusDays(5)).build()).build())
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldNotReturnDateRangeTo_WhenOrderMadeSelectionIsNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsBeSpokeRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().beSpokeRangeSelection(
                    AssistedOrderDateHeard.builder().beSpokeRangeText("beSpoke text").build()).build())
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotBeSpokeRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                    AssistedOrderDateHeard.builder().singleDate(LocalDate.now().minusDays(10)).build()).build())
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExistForBeSpokeRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isFalse();
        }

        @Test
        void shouldReturnBeSpokeTextWhenOrderMadeSelectionIsBeSpokeRange() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().beSpokeRangeSelection(
                    AssistedOrderDateHeard.builder().beSpokeRangeText("beSpoke text").build()).build())
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).contains("beSpoke text");
        }

        @Test
        void shouldNotReturnBeSpokeTextWhenOrderMadeSelectionIsNo() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).isNull();
        }

        @Test
        void shouldReturnNullWhenOrderMadeSelectionIsBeSpokeRangeAndIsNull() {
            CaseData caseData = CaseData.builder()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                    AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).isNull();
        }
    }

    @Nested
    class ReasonText {

        @Test
        void shouldReturnNull_When_GiveReasons_Null() {
            CaseData caseData = CaseData.builder()
                .assistedOrderGiveReasonsYesNo(null)
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnNull_When_GiveReasons_SelectedOption_No() {
            CaseData caseData = CaseData.builder()
                .assistedOrderGiveReasonsYesNo(YesOrNo.NO)
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnNull_When_GiveReasonsText_Null() {
            CaseData caseData = CaseData.builder()
                .assistedOrderGiveReasonsYesNo(YesOrNo.YES)
                .assistedOrderGiveReasonsDetails(AssistedOrderGiveReasonsDetails.builder().build())
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnText_When_GiveReasonsText() {
            CaseData caseData = CaseData.builder()
                .assistedOrderGiveReasonsYesNo(YesOrNo.YES)
                .assistedOrderGiveReasonsDetails(AssistedOrderGiveReasonsDetails
                                                     .builder()
                                                     .reasonsText(TEST_TEXT)
                                                     .build())
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNotNull(assistedOrderString);
        }
    }

    @Test
    void test_getCaseNumberFormatted() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1644495739087775L).build();
        String formattedCaseNumber = generator.getCaseNumberFormatted(caseData);
        assertThat(formattedCaseNumber).isEqualTo("1644-4957-3908-7775");
    }

    @Test
    void test_getFileName() {
        String name = generator.getFileName(DocmosisTemplates.ASSISTED_ORDER_FORM);
        assertThat(name).startsWith("General_order_for_application_");
        assertThat(name).endsWith(".pdf");
    }

    @Test
    void test_getTemplate() {
        assertThat(generator.getTemplate(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE)).isEqualTo(DocmosisTemplates.ASSISTED_ORDER_FORM);
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        CaseData caseData = getSampleGeneralApplicationCaseData(NO).toBuilder()
            .gaCaseManagementLocation(GACaseLocation.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("9")
                                        .postcode("BA 117").build())
            .claimant2PartyName(null).build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_FORM.getDocumentTitle(), bytes));
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());
        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> generator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldGenerateAssistedOrderDocument() {
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder()
                            .epimmsId("2")
                            .externalShortName("Reading")
                            .build());
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_FORM.getDocumentTitle(), bytes));

        CaseData caseData = getSampleGeneralApplicationCaseData(NO).toBuilder()
            .gaCaseManagementLocation(GACaseLocation.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .judgeTitle("John Doe")
            .claimant2PartyName(null).build();

        generator.generate(caseData, BEARER_TOKEN);

        verify(documentGeneratorService).generateDocmosisDocument(any(AssistedOrderForm.class),
                                                                  eq(ASSISTED_ORDER_FORM));
        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_ORDER));

        var templateData = generator.getTemplateData(null, caseData, BEARER_TOKEN, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
        assertThat(templateData.getAddress()).isEqualTo("london court");
        assertThat(templateData.getSiteName()).isEqualTo("testing");
        assertThat(templateData.getPostcode()).isEqualTo("BA 117");
        assertThat(templateData.getCourtLocation()).isEqualTo("Reading");
        assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
        assertThat(templateData.getClaimant2Name()).isNull();
        assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
        assertThat(templateData.getDefendant2Name()).isNull();
        assertThat(templateData.getJudgeNameTitle()).isEqualTo("John Doe");
    }

    @Test
    void shouldGenerateAssistedOrderDocument_1v2() {

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_FORM.getDocumentTitle(), bytes));

        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder()
                            .epimmsId("2")
                            .externalShortName("London")
                            .build());
        CaseData caseData = getSampleGeneralApplicationCaseData(YES).toBuilder()
            .gaCaseManagementLocation(GACaseLocation.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .judgeTitle("John Doe")
            .claimant2PartyName(null).build();
        generator.generate(caseData, BEARER_TOKEN);

        verify(documentGeneratorService).generateDocmosisDocument(any(AssistedOrderForm.class),
                                                                  eq(ASSISTED_ORDER_FORM));
        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_ORDER));

        var templateData = generator.getTemplateData(null, caseData, BEARER_TOKEN, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
        assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
        assertThat(templateData.getClaimant2Name()).isEqualTo(caseData.getClaimant2PartyName());
        assertThat(templateData.getIsMultiParty()).isEqualTo(YES);
        assertThat(templateData.getCourtLocation()).isEqualTo("London");
        assertThat(templateData.getJudgeNameTitle()).isEqualTo(caseData.getJudgeTitle());
        assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
        assertThat(templateData.getDefendant2Name()).isEqualTo(caseData.getDefendant2PartyName());
    }

    private CaseData getSampleGeneralApplicationCaseData(YesOrNo isMultiparty) {
        List<FinalOrderShowToggle> judgeHeardFromShowOption = new ArrayList<>();
        judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
        recitalsOrderShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> furtherHearingShowOption = new ArrayList<>();
        furtherHearingShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> appealShowOption = new ArrayList<>();
        appealShowOption.add(FinalOrderShowToggle.SHOW);
        return CaseData.builder()
            .ccdCaseReference(1644495739087775L)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1644495739087775").build())
            .claimant1PartyName("ClaimantName")
            .defendant1PartyName("defendant1PartyName")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .isMultiParty(isMultiparty)
            .locationName("ccmcc")
            .gaCaseManagementLocation(GACaseLocation.builder().siteName("testing")
                                        .address("london court")
                                        .postcode("BA 117").build())
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(AssistedOrderMadeDateHeardDetails.builder().singleDateSelection(
                AssistedOrderDateHeard.builder().singleDate(LocalDate.now()).build()).build())
            .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
            .assistedOrderRepresentation(AssistedOrderHeardRepresentation.builder()
                                             .representationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                             .otherRepresentation(DetailText.builder().detailText(OTHER_ORIGIN_TEXT).build()).build())
            .assistedOrderRecitals(recitalsOrderShowOption)
            .assistedOrderRecitalsRecorded(AssistedOrderRecitalRecord.builder().text(RECITAL_RECORDED_TEXT).build())
            .assistedOrderOrderedThatText(TEST_TEXT)
            .assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCostsGA(AssistedOrderCost.builder()
                                                  .makeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                  .assistedOrderCostsMakeAnOrderTopList(
                                                      AssistedOrderCostDropdownList.COSTS)
                                                  .assistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)).build())
            .publicFundingCostsProtection(YES)
            .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
            .assistedOrderFurtherHearingDetails(AssistedOrderFurtherHearingDetails.builder()
                                                    .listFromDate(LocalDate.now().minusDays(5))
                                                    .lengthOfNewHearing(LengthOfHearing.OTHER)
                                                    .lengthOfHearingOther(HearingLength.builder().lengthListOtherDays(2)
                                                                              .lengthListOtherHours(5)
                                                                              .lengthListOtherMinutes(30).build())
                                                    .datesToAvoid(NO)
                                                    .hearingLocationList(DynamicList.builder().value(
                                                        DynamicListElement.builder().label("Other location").build()).build())
                                                    .alternativeHearingLocation(DynamicList.builder().value(
                                                        DynamicListElement.builder().label("Site Name 2 - Address2 - 28000").build()).build())
                                                    .hearingMethods(GAJudicialHearingType.TELEPHONE).build())
            .assistedOrderAppealToggle(appealShowOption)
            .assistedOrderAppealDetails(AssistedOrderAppealDetails.builder()
                                            .appealOrigin(AppealOriginTypes.DEFENDANT)
                                            .permissionToAppeal(PermissionToAppealTypes.GRANTED)
                                            .appealTypeChoicesForGranted(
                                                AppealTypeChoices.builder()
                                                    .assistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                                                    .appealChoiceOptionA(
                                                        AppealTypeChoiceList.builder()
                                                            .appealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                                            .build()).build()).build())
            .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnOwnInitiative(DetailTextWithDate.builder()
                                          .detailText(TEST_TEXT)
                                          .date(LocalDate.now())
                                          .build())
            .assistedOrderGiveReasonsYesNo(YES)
            .assistedOrderGiveReasonsDetails(AssistedOrderGiveReasonsDetails
                                                 .builder()
                                                 .reasonsText(TEST_TEXT)
                                                 .build())
            .build();

    }

    @Nested
    class GetTemplateDataLip {

        @Test
        void test_getTemplate() {
            assertThat(generator.getTemplate(FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT)).isEqualTo(DocmosisTemplates.POST_JUDGE_ASSISTED_ORDER_FORM_LIP);
        }

        @Test
        void shouldGenerateAssistedOrderDocument() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder()
                                .epimmsId("2")
                                .externalShortName("Reading")
                                .build());
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_ASSISTED_ORDER_FORM_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_ASSISTED_ORDER_FORM_LIP.getDocumentTitle(), bytes));

            CaseData caseData = getSampleGeneralApplicationCaseData(NO).toBuilder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .gaCaseManagementLocation(GACaseLocation.builder().siteName("testing")
                                            .address("london court")
                                            .baseLocation("1")
                                            .postcode("BA 117").build())
                .judgeTitle("John Doe")
                .claimant2PartyName(null).build();

            generator.generate(CaseDataBuilder.builder().getCivilCaseData(), caseData, BEARER_TOKEN, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            verify(documentGeneratorService).generateDocmosisDocument(any(AssistedOrderForm.class),
                                                                      eq(POST_JUDGE_ASSISTED_ORDER_FORM_LIP));
            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.GENERAL_ORDER));

            var templateData = generator
                .getTemplateData(CaseDataBuilder.builder().getCivilCaseData(), caseData, BEARER_TOKEN, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
            assertThat(templateData.getAddress()).isEqualTo("london court");
            assertThat(templateData.getSiteName()).isEqualTo("testing");
            assertThat(templateData.getPostcode()).isEqualTo("BA 117");
            assertThat(templateData.getCourtLocation()).isEqualTo("Reading");
            assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
            assertThat(templateData.getClaimant2Name()).isNull();
            assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
            assertThat(templateData.getDefendant2Name()).isNull();
            assertThat(templateData.getJudgeNameTitle()).isEqualTo("John Doe");
            assertEquals("respondent1 partyname", templateData.getPartyName());
            assertEquals("respondent1address1", templateData.getPartyAddressAddressLine1());
            assertEquals("respondent1address2", templateData.getPartyAddressAddressLine2());
            assertEquals("respondent1address3", templateData.getPartyAddressAddressLine3());
            assertEquals("respondent1posttown", templateData.getPartyAddressPostTown());
            assertEquals("respondent1postcode", templateData.getPartyAddressPostCode());
        }
    }
}
