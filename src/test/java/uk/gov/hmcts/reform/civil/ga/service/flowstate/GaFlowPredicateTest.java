package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class GaFlowPredicateTest {

    @Test
    public void testJudgeNotMadeDismissalOrder_noJudicialDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeNotMadeDismissalOrder_decisionRequestMoreInfo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  .build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)
                                           .build()).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeNotMadeDismissalOrder_approveOrEdit() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  .build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                           .build()).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeMadeDismissalOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  .build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                  .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)
                                  .build()).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_dismissalOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  .build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)
                                           .build()).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_ListForHearing() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING)
                                  .build())
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .judgeHearingCourtLocationText1("test")
                                        .judgeHearingTimeEstimateText1("test")
                                        .hearingPreferencesPreferredTypeLabel1("test")
                                        .judgeHearingSupportReqText1("test")
                                        .build()).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_RequestForMoreInfo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  .build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).build()).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_JudgeUncloaksApplication() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  .build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY).build()).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testIsFreeApplication() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppType(new GAApplicationType(Collections.singletonList(GeneralApplicationTypes.ADJOURN_HEARING)))
            .generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                        .paymentDetails(PaymentDetails.builder()
                                                            .status(PaymentStatus.SUCCESS).build())
                                        .fee(Fee.builder().code("FREE").build()).build())
            .applicantBilingualLanguagePreference(YesOrNo.YES).build();

        boolean result = GaFlowPredicate.isFreeFeeWelshApplication.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsNotFreeApplication() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppType(new GAApplicationType(Collections.singletonList(GeneralApplicationTypes.ADJOURN_HEARING)))
            .generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("Not_Free").build()).build())
            .applicantBilingualLanguagePreference(YesOrNo.YES).build();

        boolean result = GaFlowPredicate.isFreeFeeWelshApplication.test(caseData);

        assertThat(result).isFalse();
    }
}
