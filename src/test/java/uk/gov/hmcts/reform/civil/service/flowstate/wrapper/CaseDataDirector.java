package uk.gov.hmcts.reform.civil.service.flowstate.wrapper;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.ClainDetailsNotificationPastDeadlineHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.CounterClaimHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.DivergentRespondentResponseHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.DraftHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.FullAdmissionRespondentResponseHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.Handler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.HearingReadinessHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.InMediationHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.MediationUnsuccessfulHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.PartAdmissionRespondentResponseHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.PrepareForHearingHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.ProceedHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.RespondentDQHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.ShapeAndCategoryHandler;
import uk.gov.hmcts.reform.civil.service.flowstate.wrapper.handler.StateHandler;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseDataDirector {

    MultiPartyScenario party;
    CaseCategory category;

    public CaseDataDirector() {
        this.party = MultiPartyScenario.ONE_V_ONE;
        this.category = CaseCategory.UNSPEC_CLAIM;
    }

    public void party(MultiPartyScenario party) {
        this.party = party;
    }

    public void category(CaseCategory category) {
        this.category = category;
    }

    public CaseData buildCaseData(FlowState.Main state) {
        CaseDataBuilder builder = new CaseDataBuilder();

        Handler handler = createHandler(state);
        handler.setNext(new ShapeAndCategoryHandler()); // re-apply after calling CaseDataBuilder
        handler.handle(builder, party, category);

        return builder.build();
    }

    @SuppressWarnings("checkstyle:Indentation")
    private Handler createHandler(FlowState.Main state) {

        // For states not explicitly handled, ensure basic data is set
        Handler handler = new StateHandler(state);
        handler.setNext(new ShapeAndCategoryHandler());

        return switch (state) {
            case AWAITING_RESPONSES_FULL_ADMIT_RECEIVED,
                 FULL_ADMIT_REJECT_REPAYMENT,
                 FULL_ADMIT_PAY_IMMEDIATELY,
                 FULL_ADMIT_JUDGMENT_ADMISSION,
                 FULL_ADMIT_AGREE_REPAYMENT,
                 FULL_ADMISSION -> handler.setNext(
                new FullAdmissionRespondentResponseHandler()
            );
            case AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED,
                 PART_ADMISSION, PART_ADMIT_AGREE_REPAYMENT,
                 PART_ADMIT_AGREE_SETTLE,
                 PART_ADMIT_NOT_SETTLED_NO_MEDIATION,
                 PART_ADMIT_PAY_IMMEDIATELY,
                 PART_ADMIT_REJECT_REPAYMENT -> handler.setNext(
                new PartAdmissionRespondentResponseHandler()
            );
            case CASE_STAYED -> handler.setNext(
                new RespondentDQHandler()
            );
            case COUNTER_CLAIM -> handler.setNext(
                new CounterClaimHandler()
            );
            case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE -> handler.setNext(
                new DivergentRespondentResponseHandler().setNext(
                    new RespondentDQHandler()
                )
            );
            case DIVERGENT_RESPOND_GO_OFFLINE -> handler.setNext(
                new DivergentRespondentResponseHandler()
            );
            case IN_HEARING_READINESS -> handler.setNext(new HearingReadinessHandler());
            case IN_MEDIATION -> handler.setNext(new InMediationHandler());
            case MEDIATION_UNSUCCESSFUL_PROCEED -> handler.setNext(new MediationUnsuccessfulHandler());
            case PART_ADMIT_NOT_PROCEED -> handler.setNext(
                new PartAdmissionRespondentResponseHandler().setNext(
                    new ProceedHandler(NO)
                ));
            case PART_ADMIT_PROCEED -> handler.setNext(
                new PartAdmissionRespondentResponseHandler().setNext(
                    new ProceedHandler(YES)
                ));
            case PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA -> handler.setNext(
                new ClainDetailsNotificationPastDeadlineHandler()
            );
            case PREPARE_FOR_HEARING_CONDUCT_HEARING -> handler.setNext(new PrepareForHearingHandler());
            case SPEC_DRAFT -> handler.setNext(
                new DraftHandler()
            );
            default -> handler;
        };
    }

}
