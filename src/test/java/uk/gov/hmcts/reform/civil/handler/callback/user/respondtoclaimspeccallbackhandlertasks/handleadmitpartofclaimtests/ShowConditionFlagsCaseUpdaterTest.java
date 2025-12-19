package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.ShowConditionFlagsCaseUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

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
class ShowConditionFlagsCaseUpdaterTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ShowConditionFlagsCaseUpdater updater;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldAddWhy2DoesNotPayImmediatelyWhenRespondent2DoesNotPayImmediately() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
    }

    @Test
    void shouldAddRepaymentPlan2WhenRepaymentPlanForRespondent2() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondentResponseIsSame(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(REPAYMENT_PLAN_2);
    }

    @Test
    void shouldNotAddAnyFlagsWhenConditionsAreNotMet() {
        CaseData caseData = CaseDataBuilder.builder().build();

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).isEmpty();
    }

    @Test
    void shouldAddNeedFinancialDetails1WhenRespondent1ConditionsAreMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSameSolicitorSameResponse(YES);
        caseData.setDefendantSingleResponseToBothClaimants(YES);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(NEED_FINANCIAL_DETAILS_1);
    }

    @Test
    void shouldAddWhy1DoesNotPayImmediatelyWhenRespondent1DoesNotPayImmediately() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setSameSolicitorSameResponse(YES);
        caseData.setDefendantSingleResponseToBothClaimants(YES);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
    }

    @Test
    void shouldNotAddFinancialDetails1WhenRespondent1ConditionsAreNotMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.YES);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setSameSolicitorSameResponse(NO);
        caseData.setDefendantSingleResponseToBothClaimants(NO);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_1));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).isNotEmpty();
        assertThat(caseData.getShowConditionFlags()).doesNotContain(NEED_FINANCIAL_DETAILS_1);
    }

    @Test
    void shouldAddWhenWillClaimBePaidWhenConditionsAreMetForRespondent2() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentResponseIsSame(NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setSameSolicitorSameResponse(NO);
        caseData.setDefendantSingleResponseToBothClaimants(NO);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(WHEN_WILL_CLAIM_BE_PAID);
    }

    @Test
    void shouldAddWhenWillClaimBePaidWhenRespondent2ConditionsMetViaRespondentResponseIsSame() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondentResponseIsSame(YES);
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setSpecDefenceAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceFullAdmittedRequired(YesOrNo.NO);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.NO);
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.COUNTER_CLAIM);
        caseData.setSameSolicitorSameResponse(YES);
        caseData.setDefendantSingleResponseToBothClaimants(YES);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).contains(
                REPAYMENT_PLAN_2,
                WHEN_WILL_CLAIM_BE_PAID
        );
    }

    @Test
    void shouldNotAddRepaymentPlan2WhenRespondentResponseIsNotSameAndNoRepaymentPlanSet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondentResponseIsSame(NO);
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).isNotEmpty();
        assertThat(caseData.getShowConditionFlags()).doesNotContain(REPAYMENT_PLAN_2);
    }

    @Test
    void shouldRemoveOldFlagsBeforeAddingNewOnes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setShowConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1,
                        DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY
                ));
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).doesNotContain(
                DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1
        ).contains(
                DefendantResponseShowTag.REPAYMENT_PLAN_2
        );
    }

    @Test
    void shouldNotAddWhenWillClaimBePaidWhenConditionsAreNotMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent2(party);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setSpecDefenceAdmitted2Required(YesOrNo.YES);
        caseData.setSpecDefenceFullAdmitted2Required(YesOrNo.YES);
        caseData.setRespondentResponseIsSame(NO);
        caseData.setRespondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setSameSolicitorSameResponse(NO);
        caseData.setDefendantSingleResponseToBothClaimants(NO);
        caseData.setShowConditionFlags(EnumSet.of(CAN_ANSWER_RESPONDENT_2));

        updater.update(caseData);

        assertThat(caseData.getShowConditionFlags()).isNotEmpty();
        assertThat(caseData.getShowConditionFlags()).doesNotContain(WHEN_WILL_CLAIM_BE_PAID);
    }
}
