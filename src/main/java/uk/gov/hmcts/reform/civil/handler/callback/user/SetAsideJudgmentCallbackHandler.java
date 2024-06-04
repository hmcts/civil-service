package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.SetAsideJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_SET_ASIDE_JUDGMENT;

@Service
@RequiredArgsConstructor
public class SetAsideJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(SET_ASIDE_JUDGMENT);
    protected final ObjectMapper objectMapper;
    private final SetAsideJudgmentOnlineMapper setAsideJudgmentOnlineMapper;
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date must be in the past";
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validate-set-aside-dates"), this::validateDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateDates(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoSetAsideOrderType().equals(
            JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION) ? caseData.getJoSetAsideOrderDate() : caseData.getJoSetAsideDefenceReceivedDate())) {
            errors.add(ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST);
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Judgment set aside")
            .confirmationBody("The judgment has been set aside")
            .build();
    }

    private CallbackResponse saveJudgmentDetails(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        caseData.setJoIsLiveJudgmentExists(YesOrNo.NO);
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGMENT_ERROR) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(NOTIFY_SET_ASIDE_JUDGMENT));
        }

        String nextState;
        if (Objects.nonNull(caseData.getJoSetAsideOrderType()) && caseData.getJoSetAsideOrderType().equals(
            JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION)) {
            nextState = CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
            caseDataBuilder.respondent1ResponseDeadline(deadlinesCalculator.plus28DaysAt4pmDeadline(
                caseData.getJoSetAsideOrderDate().atTime(0, 0)));
        } else {
            nextState = CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(nextState)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
