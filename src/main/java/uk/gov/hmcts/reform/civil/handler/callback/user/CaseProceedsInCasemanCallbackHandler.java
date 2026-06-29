package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.validation.groups.CasemanTransferDateGroup;

import java.util.List;
import java.util.Map;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDGMENT_REQUESTED;

@Service
@RequiredArgsConstructor
public class CaseProceedsInCasemanCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CASE_PROCEEDS_IN_CASEMAN);

    private final Validator validator;
    private final Time time;
    private final ObjectMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "transfer-date"), this::validateTransferDate,
            callbackKey(ABOUT_TO_SUBMIT), this::addTakenOfflineDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateTransferDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validator.validate(caseData, CasemanTransferDateGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse addTakenOfflineDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseState previousCaseState = getPreviousCaseSate(callbackParams);

        caseData.setBusinessProcess(BusinessProcess.ready(CASE_PROCEEDS_IN_CASEMAN));
        caseData.setTakenOfflineByStaffDate(time.now());
        caseData.setCoSCApplicationStatus(updateCoScApplicationStatus(callbackParams));
        caseData.setPreviousCCDState(previousCaseState);
        clearPendingJudgmentRequestIfRequired(caseData, previousCaseState);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .build();
    }

    private void clearPendingJudgmentRequestIfRequired(CaseData caseData, CaseState previousCaseState) {
        if (JUDGMENT_REQUESTED.equals(previousCaseState)) {
            JudgmentsOnlineHelper.clearJOCaseData(caseData);
        }
    }

    private CaseState getPreviousCaseSate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String previousState = getPreviousCaseDetailsState(callbackParams);
        return previousState != null
            && (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne() || caseData.isLipvLROneVOne())
                ? CaseState.valueOf(previousState)
                : null;
    }

    private String getPreviousCaseDetailsState(CallbackParams callbackParams) {
        return callbackParams.getRequest() != null
            && callbackParams.getRequest().getCaseDetailsBefore() != null
            ? callbackParams.getRequest().getCaseDetailsBefore().getState()
            : null;
    }

    private CoscApplicationStatus updateCoScApplicationStatus(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne()) {
            return CoscApplicationStatus.ACTIVE.equals(caseData.getCoSCApplicationStatus())
                ? CoscApplicationStatus.INACTIVE
                : caseData.getCoSCApplicationStatus();
        }
        return null;
    }
}
