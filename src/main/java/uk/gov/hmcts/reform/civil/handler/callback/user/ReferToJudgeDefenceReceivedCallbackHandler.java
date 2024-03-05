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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_JUDGE_DEFENCE_RECEIVED;

@Service
@RequiredArgsConstructor
public class ReferToJudgeDefenceReceivedCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REFER_JUDGE_DEFENCE_RECEIVED);
    protected final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_DATE_MUST_BE_IN_PAST = "Date must be in past";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveReferToJudgeDefenceReceivedInTimeDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
            .confirmationBody(getBody())
            .build();
    }

    private String getHeader() {
        return format("# Refer to judge (defence received in time)");
    }

    private String getBody() {
        return format("# The case has been referred to a judge for a decision");
    }

    private CallbackResponse saveReferToJudgeDefenceReceivedInTimeDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

/*        if (nonNull(caseData.getJoOrderMadeDate())
            && nonNull(caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade())) {
            boolean paidAfter30Days = JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan30Days(caseData.getJoOrderMadeDate(),
                                                                              caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade());
            if (paidAfter30Days) {
                caseData.getJoJudgmentStatusDetails().setJudgmentStatusTypes(JudgmentStatusType.SATISFIED);
                if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
                    caseData.getJoJudgmentStatusDetails().setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SATISFIED));
                }
            } else {
                caseData.getJoJudgmentStatusDetails().setJudgmentStatusTypes(JudgmentStatusType.CANCELLED);
                if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
                    caseData.getJoJudgmentStatusDetails().setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.CANCELLED));
                }
            }
            caseData.getJoJudgmentStatusDetails().setLastUpdatedDate(LocalDateTime.now());
            caseData.setJoIsLiveJudgmentExists(YesOrNo.NO);
        }
*/
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
