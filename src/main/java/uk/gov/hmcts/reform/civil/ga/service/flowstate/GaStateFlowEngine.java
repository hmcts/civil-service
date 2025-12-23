package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlowBuilder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isFreeFeeWelshApplication;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isLipApplication;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isLipRespondent;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isVaryJudgementAppByResp;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isWelshApplicant;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.isWelshJudgeDecision;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeMadeDecision;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeMadeDirections;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeMadeListingForHearing;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeMadeOrder;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeMadeWrittenRep;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.judgeRequestAdditionalInfo;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.paymentSuccess;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.withNoticeApplication;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowPredicate.withOutNoticeApplication;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.LISTED_FOR_HEARING;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.PROCEED_GENERAL_APPLICATION;

@Component
@RequiredArgsConstructor
public class GaStateFlowEngine {

    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    public GaStateFlow build() {
        return GaStateFlowBuilder.<GaFlowState.Main>flow(FLOW_NAME)
            .initial(DRAFT)
            .transitionTo(APPLICATION_SUBMITTED)
                .onlyIf((withNoticeApplication.or(withOutNoticeApplication)))
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                    flags.put(FlowFlag.VARY_JUDGE_GA_BY_RESP.name(), isVaryJudgementAppByResp.test(c));
                    flags.put(FlowFlag.FREE_FEE_WELSH_APPLICATION.name(), isFreeFeeWelshApplication.test(c));
                })
            .state(APPLICATION_SUBMITTED)
                .transitionTo(PROCEED_GENERAL_APPLICATION)
                    .onlyIf(paymentSuccess)
                    .set((c, flags) -> {
                        flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                        flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                        flags.put(FlowFlag.VARY_JUDGE_GA_BY_RESP.name(), isVaryJudgementAppByResp.test(c));
                        flags.put(
                            FlowFlag.WELSH_ENABLED.name(),
                            featureToggleService.isGaForWelshEnabled() && isWelshApplicant.test(c)
                        );
                    })
            .state(PROCEED_GENERAL_APPLICATION)
                .transitionTo(APPLICATION_SUBMITTED_JUDICIAL_DECISION)
                    .onlyIf(judgeMadeDecision)
                    .set((c, flags) -> {
                        flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                        flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                        flags.put(FlowFlag.VARY_JUDGE_GA_BY_RESP.name(), isVaryJudgementAppByResp.test(c));
                        flags.put(
                            FlowFlag.WELSH_ENABLED_FOR_JUDGE_DECISION.name(),
                            featureToggleService.isGaForWelshEnabled() && isWelshJudgeDecision.test(c)
                        );
                    })
            .state(APPLICATION_SUBMITTED_JUDICIAL_DECISION)
                .transitionTo(LISTED_FOR_HEARING).onlyIf(judgeMadeListingForHearing)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                })
                .transitionTo(ADDITIONAL_INFO).onlyIf(judgeRequestAdditionalInfo)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                })
                .transitionTo(JUDGE_DIRECTIONS).onlyIf(judgeMadeDirections)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                })
                .transitionTo(JUDGE_WRITTEN_REPRESENTATION).onlyIf(judgeMadeWrittenRep)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                })
                .transitionTo(ORDER_MADE).onlyIf(judgeMadeOrder)
                .set((c, flags) -> {
                    flags.put(FlowFlag.LIP_APPLICANT.name(), isLipApplication.test(c));
                    flags.put(FlowFlag.LIP_RESPONDENT.name(), isLipRespondent.test(c));
                })
            .state(LISTED_FOR_HEARING)
            .state(ADDITIONAL_INFO)
            .state(JUDGE_DIRECTIONS)
            .state(JUDGE_WRITTEN_REPRESENTATION)
            .state(ORDER_MADE)
            .build();
    }

    public GaStateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails));
    }

    public GaStateFlow evaluate(GeneralApplicationCaseData caseData) {
        return build().evaluate(caseData);
    }

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream().map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }

}
