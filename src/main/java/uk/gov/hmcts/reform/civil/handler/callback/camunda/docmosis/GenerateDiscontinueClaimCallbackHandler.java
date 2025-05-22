package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateDiscontinueClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GEN_NOTICE_OF_DISCONTINUANCE
    );
    private static final String TASK_ID = "GenerateNoticeOfDiscontinueClaim";
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;
    private final NoticeOfDiscontinuanceFormGenerator formGenerator;
    private final RuntimeService runTimeService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        updateCamundaVars(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        buildDocuments(callbackParams, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private void buildDocuments(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = callbackParams.getCaseData();

        CaseDocument applicant1DiscontinueDoc = generateForm(caseData.getApplicant1(), callbackParams);
        CaseDocument respondent1DiscontinueDoc = generateForm(caseData.getRespondent1(), callbackParams);
        CaseDocument respondent2DiscontinueDoc = null;

        if (YES.equals(caseData.getAddRespondent2()) && caseData.getRespondent2() != null) {
            respondent2DiscontinueDoc = generateForm(caseData.getRespondent2(), callbackParams);
        }
        if (featureToggleService.isGaForWelshEnabled()
            && caseData.isRespondent1LiP() &&
            caseData.getTypeOfDiscontinuance().equals(DiscontinuanceTypeList.PART_DISCONTINUANCE) &&
            (caseData.isRespondentResponseBilingual()
                || caseData.isLipDefendantSpecifiedBilingualDocuments())) {
            List<Element<CaseDocument>> translatedDocuments = callbackParams.getCaseData()
                .getPreTranslationDocuments();
            applicant1DiscontinueDoc.setDocumentType(NOTICE_OF_DISCONTINUANCE_CLAIMANT);
            respondent1DiscontinueDoc.setDocumentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT);
            assignDiscontinuanceCategoryId(applicant1DiscontinueDoc);
            assignDiscontinuanceCategoryId(respondent1DiscontinueDoc);
            translatedDocuments.add(element(applicant1DiscontinueDoc));
            translatedDocuments.add(element(respondent1DiscontinueDoc));
            caseDataBuilder.preTranslationDocuments(translatedDocuments);
            caseDataBuilder.preTranslationDocumentType(PreTranslationDocumentType.NOTICE_OF_DISCONTINUANCE);
        } else if (caseData.isJudgeOrderVerificationRequired()) {
            caseDataBuilder.applicant1NoticeOfDiscontinueCWViewDoc(applicant1DiscontinueDoc);
            caseDataBuilder.respondent1NoticeOfDiscontinueCWViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getApplicant1NoticeOfDiscontinueCWViewDoc());
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent1NoticeOfDiscontinueCWViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseDataBuilder.respondent2NoticeOfDiscontinueCWViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent2NoticeOfDiscontinueCWViewDoc());
            }
        } else {
            caseDataBuilder.applicant1NoticeOfDiscontinueAllPartyViewDoc(applicant1DiscontinueDoc);
            caseDataBuilder.respondent1NoticeOfDiscontinueAllPartyViewDoc(respondent1DiscontinueDoc);
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getApplicant1NoticeOfDiscontinueAllPartyViewDoc());
            assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent1NoticeOfDiscontinueAllPartyViewDoc());

            if (respondent2DiscontinueDoc != null) {
                caseDataBuilder.respondent2NoticeOfDiscontinueAllPartyViewDoc(respondent2DiscontinueDoc);
                assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent2NoticeOfDiscontinueAllPartyViewDoc());
            }
        }
    }

    private CaseDocument generateForm(Party party, CallbackParams callbackParams) {
        return formGenerator.generateDocs(
            callbackParams.getCaseData(), party, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private void assignDiscontinuanceCategoryId(CaseDocument caseDocument) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "JUDGE_ORDER_VERIFICATION_REQUIRED",
            caseData.isJudgeOrderVerificationRequired()
        );
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "WELSH_ENABLED",
            featureToggleService.isGaForWelshEnabled()
                && caseData.isRespondent1LiP() &&
                caseData.getTypeOfDiscontinuance().equals(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                && (caseData.isRespondentResponseBilingual()
                || caseData.isLipDefendantSpecifiedBilingualDocuments())
        );
    }
}
