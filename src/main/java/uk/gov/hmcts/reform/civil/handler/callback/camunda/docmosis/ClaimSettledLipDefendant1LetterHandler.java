package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue.ClaimSettledDefendantLiPLetterGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;

@RequiredArgsConstructor
@Service
public class ClaimSettledLipDefendant1LetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1);
    public static final String TASK_ID = "SendClaimSettledLetterLipDef";
    private final ClaimSettledDefendantLiPLetterGenerator lipLetterGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendClaimSettledLetterToLiPDefendant1
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendClaimSettledLetterToLiPDefendant1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (isClaimSettledLetterRequired(caseData)) {
            String auth = callbackParams.getParams().get(BEARER_TOKEN).toString();
            lipLetterGenerator.generateAndPrintClaimSettledLetter(caseData, auth);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private static boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    /*
     * OCCC-297/352 (Def LiP letter): the claim was settled via SETTLE_CLAIM_UNSPEC (Claimant LR, before the
     * defendant has joined/responded), which stashes the state the claim was in immediately before settlement
     * in preStayState. POs agreed the letter must only go out when that state was "Awaiting Claim Details
     * Notification" - not "Awaiting Claim Notification", since the defendant's postal address may not yet be
     * reliable that early.
     *
     * Also evaluated by StartBusinessProcessTaskHandler so the claim settled letter BPMN can skip the
     * SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1 event (and its case history entry) when no letter is due.
     */
    public static boolean isClaimSettledLetterRequired(CaseData caseData) {
        return isRespondent1Lip(caseData)
            && AWAITING_CASE_DETAILS_NOTIFICATION.name().equals(caseData.getPreStayState());
    }
}
