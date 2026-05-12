package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueDefaultJudgementSpecCallbackHandler extends CallbackHandler {

    private static final String JUDGMENT_GRANTED_HEADER = "# Default Judgment Granted ";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT_SPEC);
    private static final String JUDGMENT_GRANTED = "<br /><a href=\"%s\" target=\"_blank\">Download  default judgment</a> "
        + "%n%n The defendant will be served with the Default Judgment.";
    private static final String CASES_CASE_DETAILS_CLAIM_DOCUMENTS = "/cases/case-details/%s#Claim documents";

    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        caseData.getActiveJudgment()
            .setState(JudgmentState.ISSUED)
            .setIssueDate(LocalDate.now())
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState());

        caseData.setJoIsRegisteredWithRTL(YES);
        caseData.setBusinessProcess(BusinessProcess.ready(DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state(CaseState.All_FINAL_ORDERS_ISSUED.name())
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(JUDGMENT_GRANTED_HEADER)
            .confirmationBody(format(
                JUDGMENT_GRANTED,
                format(CASES_CASE_DETAILS_CLAIM_DOCUMENTS, caseData.getCcdCaseReference())
            ))
            .build();
    }
}
