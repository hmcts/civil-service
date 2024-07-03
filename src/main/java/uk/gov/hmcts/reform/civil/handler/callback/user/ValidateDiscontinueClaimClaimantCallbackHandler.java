package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.helpers.SettleClaimHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PermissionGranted;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ValidateDiscontinueClaimClaimantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateJudgeNameAndDate,
            callbackKey(ABOUT_TO_SUBMIT), this::submitChanges,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateJudgeNameAndDate(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder()
            .permissionGrantedComplex(
                PermissionGranted.builder()
                    .permissionGrantedJudge(caseData.getPermissionGrantedComplex().getPermissionGrantedJudge())
                    .permissionGrantedDate(caseData.getPermissionGrantedComplex().getPermissionGrantedDate())
                    .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
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

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Claim marked as settled")
            .confirmationBody("<br />")
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
