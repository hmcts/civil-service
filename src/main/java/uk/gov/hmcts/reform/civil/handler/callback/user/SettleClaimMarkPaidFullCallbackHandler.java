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
import uk.gov.hmcts.reform.civil.helpers.settlediscontinue.SettleClaimHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARK_PAID_FULL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class SettleClaimMarkPaidFullCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SETTLE_CLAIM_MARK_PAID_FULL);
    public static final String REQUEST_BEING_REVIEWED_NEXT_STEPS = """
            ### Next step

             The case will now proceed offline and your online account will not be updated for this claim. Any updates will be sent by post.""";
    public static final String CLOSED_NEXT_STEPS = """
            ### Next step

             Any hearing listed will be vacated.\s

             The defendants will be notified.""";
    public static final String REQUEST_BEING_REVIEWED_HEADER = "### Request is being reviewed";
    public static final String CLOSED_HEADER = "### This claim has been marked as settled";
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateState)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitChanges)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateState(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        final var caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        SettleClaimHelper.checkState(caseData, errors);
        if (errors.isEmpty() && (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES))) {
            List<String> claimantNames = new ArrayList<>();
            claimantNames.add(caseData.getApplicant1().getPartyName());
            claimantNames.add(caseData.getApplicant2().getPartyName());
            caseDataBuilder.claimantWhoIsSettling(DynamicList.fromList(claimantNames));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitChanges(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        AboutToStartOrSubmitCallbackResponse
            .AboutToStartOrSubmitCallbackResponseBuilder aboutToStartOrSubmitCallbackResponseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (caseData.getMarkPaidForAllClaimants() == null || YES.equals(caseData.getMarkPaidForAllClaimants())) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(SETTLE_CLAIM_MARKED_PAID_IN_FULL));
            aboutToStartOrSubmitCallbackResponseBuilder.state(CaseState.CLOSED.name());
        }
        return aboutToStartOrSubmitCallbackResponseBuilder.data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private static String getBody(CaseData caseData) {
        if (NO.equals(caseData.getMarkPaidForAllClaimants())) {
            return format(REQUEST_BEING_REVIEWED_NEXT_STEPS);
        }
        return format(CLOSED_NEXT_STEPS);
    }

    private static String getHeader(CaseData caseData) {
        if (NO.equals(caseData.getMarkPaidForAllClaimants())) {
            return format(REQUEST_BEING_REVIEWED_HEADER);
        }
        return format(CLOSED_HEADER);
    }
}
