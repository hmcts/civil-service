package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE;

@Service
@RequiredArgsConstructor
public class UpdateVisibilityNoticeOfDiscontinuanceHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE);
    public static final String TASK_ID = "UpdateVisibilityNoticeOfDiscontinuance";

    private final RuntimeService runTimeService;
    private final AssignCategoryId assignCategoryId;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::updateVisibilityNoticeDiscontinuance
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

    private CallbackResponse updateVisibilityNoticeDiscontinuance(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        updateCamundaVars(caseData);
        if (ConfirmOrderGivesPermission.YES.equals(caseData.getConfirmOrderGivesPermission())) {
            updateVisibilityForAllParties(caseData);
            removeCaseWorkerViewDocuments(caseData);

            assignCategoryIdForAllParties(caseData);

            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseData.toMap(objectMapper))
                    .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void updateVisibilityForAllParties(CaseData caseData) {
        caseData.setApplicant1NoticeOfDiscontinueAllPartyViewDoc(caseData.getApplicant1NoticeOfDiscontinueCWViewDoc());
        caseData.setRespondent1NoticeOfDiscontinueAllPartyViewDoc(caseData.getRespondent1NoticeOfDiscontinueCWViewDoc());

        if (caseData.getRespondent2NoticeOfDiscontinueCWViewDoc() != null) {
            caseData.setRespondent2NoticeOfDiscontinueAllPartyViewDoc(caseData.getRespondent2NoticeOfDiscontinueCWViewDoc());
        }
    }

    private void removeCaseWorkerViewDocuments(CaseData caseData) {
        caseData.setApplicant1NoticeOfDiscontinueCWViewDoc(null);
        caseData.setRespondent1NoticeOfDiscontinueCWViewDoc(null);
        caseData.setRespondent2NoticeOfDiscontinueCWViewDoc(null);
    }

    private void assignCategoryIdForAllParties(CaseData caseData) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseData.getApplicant1NoticeOfDiscontinueAllPartyViewDoc(), DocCategory.NOTICE_OF_DISCONTINUE.getValue());
        assignCategoryId.assignCategoryIdToCaseDocument(caseData.getRespondent1NoticeOfDiscontinueAllPartyViewDoc(), DocCategory.NOTICE_OF_DISCONTINUE.getValue());
        assignCategoryId.assignCategoryIdToCaseDocument(caseData.getRespondent2NoticeOfDiscontinueAllPartyViewDoc(), DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "discontinuanceValidationSuccess",
            ConfirmOrderGivesPermission.YES.equals(caseData.getConfirmOrderGivesPermission())
        );
    }
}
