package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateDiscontinueClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            GEN_NOTICE_OF_DISCONTINUANCE
    );
    private static final String TASK_ID = "GenerateNoticeOfDiscontinueClaim";
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;
    private final NoticeOfDiscontinuanceFormGenerator formGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateNoticeOfDiscontinueDoc);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse generateNoticeOfDiscontinueDoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        buildDocument(callbackParams, caseDataBuilder);
        CaseData updatedData = caseDataBuilder.build();
        if (isPermissionRequired(caseData)) {
            assignDiscontinuanceCategoryId(updatedData.getNoticeOfDiscontinueCWDoc());
            updatedData.setNoticeOfDiscontinueAllParitiesDoc(null);
            log.info("isPermissionRequired--------if------------------");
        } else {
            assignDiscontinuanceCategoryId(updatedData.getNoticeOfDiscontinueAllParitiesDoc());
            updatedData.setNoticeOfDiscontinueCWDoc(null);
            log.info("isPermissionRequired--------else--------");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper))
                .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument caseDocument = formGenerator.generateDocs(
                callbackParams.getCaseData(),
                callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.noticeOfDiscontinueCWDoc(caseDocument);
        caseDataBuilder.noticeOfDiscontinueAllParitiesDoc(caseDocument);
    }

    private boolean isPermissionRequired(CaseData caseData) {
        return SettleDiscontinueYesOrNoList.YES.equals(caseData.getCourtPermissionNeeded())
                && SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsPermissionGranted());
    }

    private void assignDiscontinuanceCategoryId(CaseDocument caseDocument) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }
}
