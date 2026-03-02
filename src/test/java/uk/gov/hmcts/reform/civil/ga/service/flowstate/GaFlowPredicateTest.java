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
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeNotMadeDismissalOrder_decisionRequestMoreInfo() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  )
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeNotMadeDismissalOrder_approveOrEdit() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  )
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testJudgeMadeDismissalOrder() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  )
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                  .setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)).build();

        boolean result = GaFlowPredicate.judgeMadeDismissalOrder.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_dismissalOrder() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                  )
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION)).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_ListForHearing() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.LIST_FOR_A_HEARING)
                                  )
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setJudgeHearingCourtLocationText1("test")
                                        .setJudgeHearingTimeEstimateText1("test")
                                        .setHearingPreferencesPreferredTypeLabel1("test")
                                        .setJudgeHearingSupportReqText1("test")).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_RequestForMoreInfo() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  )
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION)).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsWelshJudgeDecision_JudgeUncloaksApplication() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .judicialDecision(new GAJudicialDecision()
                                  .setDecision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                  )
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY)).build();

        boolean result = GaFlowPredicate.isWelshJudgeDecision.test(caseData);

        assertThat(result).isFalse();
    }

    @Test
    public void testIsFreeApplication() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppType(GAApplicationType.builder().types(Collections.singletonList(GeneralApplicationTypes.ADJOURN_HEARING)).build())
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                        .setPaymentDetails(new PaymentDetails()
                                                            .setStatus(PaymentStatus.SUCCESS))
                                        .setFee(new Fee().setCode("FREE")))
            .applicantBilingualLanguagePreference(YesOrNo.YES).build();

        boolean result = GaFlowPredicate.isFreeFeeWelshApplication.test(caseData);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsNotFreeApplication() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppType(GAApplicationType.builder().types(Collections.singletonList(GeneralApplicationTypes.ADJOURN_HEARING)).build())
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setPaymentDetails(new PaymentDetails()
                                                          .setStatus(PaymentStatus.SUCCESS))
                                      .setFee(new Fee().setCode("Not_Free")))
            .applicantBilingualLanguagePreference(YesOrNo.YES).build();

        boolean result = GaFlowPredicate.isFreeFeeWelshApplication.test(caseData);

        assertThat(result).isFalse();
    }
}
