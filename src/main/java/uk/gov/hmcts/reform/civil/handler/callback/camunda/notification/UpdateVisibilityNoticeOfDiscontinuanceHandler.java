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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE;

@Service
@RequiredArgsConstructor
public class UpdateVisibilityNoticeOfDiscontinuanceHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE);
    public static final String TASK_ID = "UpdateVisibilityNoticeOfDiscontinuance";

    private final RuntimeService runTimeService;
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;

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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        CaseDocument noticeOfDiscontinuance = caseData.getNoticeOfDiscontinueCWDoc();

        if (nonNull(noticeOfDiscontinuance)) {
            caseDataBuilder
                .noticeOfDiscontinueAllParitiesDoc(noticeOfDiscontinuance)
                .noticeOfDiscontinueCWDoc(null)
                .build();
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getNoticeOfDiscontinueAllParitiesDoc());
        }

        updateCamundaVars(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void assignDiscontinuanceCategoryId(CaseDocument caseDocument) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "discontinuanceValidationSuccess",
            ConfirmOrderGivesPermission.YES.equals(caseData.getConfirmOrderGivesPermission())
        );
    }
}
