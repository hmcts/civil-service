package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.COSTS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.INDEMNITY_BASIS;

@ExtendWith(SpringExtension.class)
public class CostsDetailsGroupTest {

    @InjectMocks
    private CostDetailsPopulator costsDetailsGroup;

    @Test
    void shouldPopulateCostsDetails_WhenAllFieldsArePresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsYesOrNo(YES)
                                                  .setAssistedOrderAssessmentSecondDropdownList1(INDEMNITY_BASIS)
                                                  .setAssistedOrderAssessmentSecondDropdownList2(CostEnums.YES)
                                                  .setMakeAnOrderForCostsList(CLAIMANT)
                                                  .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L)) // £100
                                                  .setAssistedOrderCostsFirstDropdownDate(LocalDate.of(2023, 12, 31))
                                                  .setAssistedOrderClaimantDefendantFirstDropdown(COSTS))
            .publicFundingCostsProtection(YES)
            .build();

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = costsDetailsGroup.populateCostsDetails(builder, caseData);

        Assertions.assertNull(builder.build().getCostsReservedText());
        Assertions.assertEquals(
            "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) "
                             + "in the sum of £100.00. Such sum shall be paid by 4pm on",
            builder.build().getSummarilyAssessed());
        Assertions.assertEquals(LocalDate.of(2023, 12, 31), builder.build().getSummarilyAssessedDate());
    }

    @Test
    void shouldReturnNull_WhenSummarilyAssessedCostsAreNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderCostList(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsYesOrNo(YES))
                                                  .publicFundingCostsProtection(YES).build();

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = costsDetailsGroup.populateCostsDetails(builder, caseData);

        Assertions.assertNull(builder.build().getSummarilyAssessed());
        Assertions.assertNull(builder.build().getSummarilyAssessedDate());
    }

    @Test
    void testPopulateInterimPaymentText() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setAssistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                    10000L)))
            .build();
        String response = costsDetailsGroup.populateInterimPaymentText(caseData);
        assertEquals(format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownAmount())), response);
    }

    @Test
    void testPopulateSummarilyAssessedText() {
        CaseData caseDataClaimant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsList(CLAIMANT)
                                                  .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L)))
            .build();
        CaseData caseDataDefendant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setMakeAnOrderForCostsList(DEFENDANT)
                                                  .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(10000L)))
            .build();
        String responseClaimant = costsDetailsGroup.populateSummarilyAssessedText(caseDataClaimant);
        String responseDefendant = costsDetailsGroup.populateSummarilyAssessedText(caseDataDefendant);
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
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setAssistedOrderAssessmentSecondDropdownList1(INDEMNITY_BASIS)
                                                  .setMakeAnOrderForCostsList(CLAIMANT))
            .build();
        CaseData caseDataDefendant = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCostDetails()
                                                  .setAssistedOrderAssessmentSecondDropdownList1(COSTS)
                                                  .setMakeAnOrderForCostsList(DEFENDANT))
            .build();
        String responseClaimant = costsDetailsGroup.populateDetailedAssessmentText(caseDataClaimant);
        String responseDefendant = costsDetailsGroup.populateDetailedAssessmentText(caseDataDefendant);
        assertEquals("The claimant shall pay the defendant's costs to be subject to a "
                         + "detailed assessment on the indemnity basis if not agreed", responseClaimant);
        assertEquals("The defendant shall pay the claimant's costs to be subject to"
                         + " a detailed assessment on the standard basis if not agreed", responseDefendant);
    }
}
