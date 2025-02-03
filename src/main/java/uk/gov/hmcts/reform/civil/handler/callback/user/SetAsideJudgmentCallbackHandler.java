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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.SetAsideJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;

@Service
@RequiredArgsConstructor
public class SetAsideJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(SET_ASIDE_JUDGMENT);

    protected final ObjectMapper objectMapper;
    private final SetAsideJudgmentOnlineMapper setAsideJudgmentOnlineMapper;
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date must be in the past";
    private static final String ERROR_MESSAGE_APPLICATION_DATE =
        "Application date to set aside judgment must be on or before the date of the order setting aside judgment";
    private static final String ERROR_MESSAGE_DEFENCE_DATE =
        "Date the defence was received must be on or before the date of the order setting aside judgment";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validate-set-aside-dates"), this::validateDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDates(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (JudgmentsOnlineHelper.validateIfFutureDate(caseData.getJoSetAsideOrderDate())) {
            errors.add(ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST);
        }
        if (JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION.equals(caseData.getJoSetAsideOrderType())
            && caseData.getJoSetAsideApplicationDate().isAfter(caseData.getJoSetAsideOrderDate())) {
            errors.add(ERROR_MESSAGE_APPLICATION_DATE);
        }
        if (JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE.equals(caseData.getJoSetAsideOrderType())
            && caseData.getJoSetAsideDefenceReceivedDate().isAfter(caseData.getJoSetAsideOrderDate())) {
            errors.add(ERROR_MESSAGE_DEFENCE_DATE);
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
        caseData.setJoIsDisplayInJudgmentTab(YesOrNo.NO);
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(SET_ASIDE_JUDGMENT))
            .joSetAsideCreatedDate(LocalDateTime.now());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
