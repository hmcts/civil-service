package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.ShowConditionFlagsCaseUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;

@ExtendWith(MockitoExtension.class)
public class ShowConditionFlagsCaseUpdaterTest {

    @InjectMocks
    private ShowConditionFlagsCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldAddWhy2DoesNotPayImmediatelyWhenRespondent2DoesNotPayImmediately() {
        CaseData caseData = CaseData.builder()
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
    }

    @Test
    void shouldAddRepaymentPlan2WhenRepaymentPlanForRespondent2() {
        CaseData caseData = CaseData.builder()
                .respondentResponseIsSame(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(REPAYMENT_PLAN_2);
    }

    @Test
    void shouldNotAddAnyFlagsWhenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder().build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).isEmpty();
    }

    @Test
    void shouldAddNeedFinancialDetails1WhenRespondent1ConditionsAreMet() {
        CaseData caseData = CaseData.builder()
                .isRespondent1(YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .specDefenceAdmittedRequired(YesOrNo.NO)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION)
                .sameSolicitorSameResponse(YES)
                .defendantSingleResponseToBothClaimants(YES)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(NEED_FINANCIAL_DETAILS_1);
    }

    @Test
    void shouldAddWhy1DoesNotPayImmediatelyWhenRespondent1DoesNotPayImmediately() {
        CaseData caseData = CaseData.builder()
                .isRespondent1(YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .specDefenceAdmittedRequired(YesOrNo.NO)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION)
                .sameSolicitorSameResponse(YES)
                .defendantSingleResponseToBothClaimants(YES)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
    }

    @Test
    void shouldNotAddFinancialDetails1WhenRespondent1ConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .isRespondent1(YES)
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .specDefenceFullAdmittedRequired(YesOrNo.YES)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE)
                .sameSolicitorSameResponse(NO)
                .defendantSingleResponseToBothClaimants(NO)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();

        assertThat(updatedCaseData.getShowConditionFlags()).isNotEmpty();
        assertThat(updatedCaseData.getShowConditionFlags()).doesNotContain(NEED_FINANCIAL_DETAILS_1);
    }

    @Test
    void shouldAddWhenWillClaimBePaidWhenConditionsAreMetForRespondent2() {
        CaseData caseData = CaseData.builder()
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .specDefenceAdmitted2Required(YesOrNo.NO)
                .specDefenceFullAdmitted2Required(YesOrNo.NO)
                .respondentResponseIsSame(NO)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE)
                .sameSolicitorSameResponse(NO)
                .defendantSingleResponseToBothClaimants(NO)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(WHEN_WILL_CLAIM_BE_PAID);
    }

    @Test
    void shouldAddWhenWillClaimBePaidWhenRespondent2ConditionsMetViaRespondentResponseIsSame() {
        CaseData caseData = CaseData.builder()
                .respondentResponseIsSame(YES)
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .specDefenceAdmittedRequired(YesOrNo.NO)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .specDefenceAdmitted2Required(YesOrNo.NO)
                .specDefenceFullAdmitted2Required(YesOrNo.NO)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .sameSolicitorSameResponse(YES)
                .defendantSingleResponseToBothClaimants(YES)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).contains(
                REPAYMENT_PLAN_2,
                WHEN_WILL_CLAIM_BE_PAID
        );
    }

    @Test
    void shouldNotAddRepaymentPlan2WhenRespondentResponseIsNotSameAndNoRepaymentPlanSet() {
        CaseData caseData = CaseData.builder()
                .respondentResponseIsSame(NO)
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();

        assertThat(updatedCaseData.getShowConditionFlags()).isNotEmpty();
        assertThat(updatedCaseData.getShowConditionFlags()).doesNotContain(REPAYMENT_PLAN_2);
    }

    @Test
    void shouldRemoveOldFlagsBeforeAddingNewOnes() {
        CaseData caseData = CaseData.builder()
                .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1,
                        DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY
                ))
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getShowConditionFlags()).doesNotContain(
                DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1
        ).contains(
                DefendantResponseShowTag.REPAYMENT_PLAN_2
        );
    }

    @Test
    void shouldNotAddWhenWillClaimBePaidWhenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .isRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .specDefenceAdmitted2Required(YesOrNo.YES)
                .specDefenceFullAdmitted2Required(YesOrNo.YES)
                .respondentResponseIsSame(NO)
                .respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE)
                .sameSolicitorSameResponse(NO)
                .defendantSingleResponseToBothClaimants(NO)
                .showConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();

        assertThat(updatedCaseData.getShowConditionFlags()).isNotEmpty();
        assertThat(updatedCaseData.getShowConditionFlags()).doesNotContain(WHEN_WILL_CLAIM_BE_PAID);
    }
}
