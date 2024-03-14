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
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_JUDGMENT;

@Service
@RequiredArgsConstructor
public class EditJudgmentCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EDIT_JUDGMENT);
    protected final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::setRTLFieldShowCondition)
            .put(callbackKey(MID, "validateDates"), this::validateDates)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentDetails)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse setRTLFieldShowCondition(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.NO) {
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.YES);
        } else {
            caseData.setJoShowRegisteredWithRTLOption(YesOrNo.NO);
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = JudgmentsOnlineHelper.validateMidCallbackData(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Judgment edited")
            .confirmationBody("The judgment has been edited")
            .build();
    }

    private CallbackResponse saveJudgmentDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        JudgmentStatusDetails judgmentStatusDetails = caseData.getJoJudgmentStatusDetails();
        judgmentStatusDetails.setJudgmentStatusTypes(JudgmentStatusType.MODIFIED);
        judgmentStatusDetails.setLastUpdatedDate(LocalDateTime.now());
        if (caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            if (caseData.getJoShowRegisteredWithRTLOption() == YesOrNo.NO) {
                judgmentStatusDetails.setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.MODIFIED));
            } else {
                judgmentStatusDetails.setJoRtlState(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED));
            }
            caseData.setJoIssuedDate(caseData.getJoOrderMadeDate());
        }
        caseData.setJoJudgmentStatusDetails(judgmentStatusDetails);
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
