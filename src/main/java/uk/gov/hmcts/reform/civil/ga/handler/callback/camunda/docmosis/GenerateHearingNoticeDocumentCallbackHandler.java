package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GaHearingFormGenerator;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateHearingNoticeDocumentCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_HEARING_NOTICE_DOCUMENT);
    private static final String TASK_ID = "GenerateHearingNoticeDocument";
    private final GaHearingFormGenerator hearingFormGenerator;
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;

    private final GaForLipService gaForLipService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final SendFinalOrderPrintService sendFinalOrderPrintService;
    private final FeatureToggleService featureToggleService;

    CaseDocument postJudgeOrderToLipApplicant = null;
    CaseDocument postJudgeOrderToLipRespondent = null;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateHearingNoticeDocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse generateHearingNoticeDocument(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Generate hearing notice document for case id: {}", caseData.getCcdCaseReference());
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        buildDocument(callbackParams, caseDataBuilder, caseData);
        postHearingFormWithCoverLetterLip(callbackParams, caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, GeneralApplicationCaseData caseDataBuilder,
                               GeneralApplicationCaseData caseData) {
        if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
            List<Element<CaseDocument>> preTranslatedDocuments =
                Optional.ofNullable(caseData.getPreTranslationGaDocuments())
                    .orElseGet(ArrayList::new);
            preTranslatedDocuments.addAll(wrapElements(hearingFormGenerator.generate(
                callbackParams.getGeneralApplicationCaseData(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            )));
            assignCategoryId.assignCategoryIdToCollection(
                preTranslatedDocuments,
                document -> document.getValue().getDocumentLink(),
                AssignCategoryId.APPLICATIONS
            );
            caseDataBuilder.preTranslationGaDocuments(preTranslatedDocuments);
            caseDataBuilder.preTranslationGaDocumentType(PreTranslationGaDocumentType.HEARING_NOTICE_DOC);
        } else {
            List<Element<CaseDocument>> documents = ofNullable(caseData.getHearingNoticeDocument())
                .orElse(newArrayList());
            documents.addAll(wrapElements(hearingFormGenerator.generate(
                callbackParams.getGeneralApplicationCaseData(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            )));

            assignCategoryId.assignCategoryIdToCollection(documents, document -> document.getValue().getDocumentLink(),
                                                          AssignCategoryId.APPLICATIONS);
            caseDataBuilder.hearingNoticeDocument(documents);
        }

    }

    private void postHearingFormWithCoverLetterLip(CallbackParams callbackParams, GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData civilCaseData = new GeneralApplicationCaseData().build();
        if (gaForLipService.isGaForLip(caseData)) {
            civilCaseData = caseDetailsConverter
                .toGeneralApplicationCaseData(coreCaseDataService
                                .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        }

        /*
         * Generate Judge Request for Information order document with LIP Applicant Post Address
         * */
        if (gaForLipService.isLipApp(caseData)
            && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
            postJudgeOrderToLipApplicant = hearingFormGenerator.generate(
                civilCaseData,
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
            );
        }

        /*
         * Generate Judge Request for Information order document with LIP Respondent Post Address
         * if GA is with notice
         * */
        if (gaForLipService.isLipResp(caseData)
            && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
            postJudgeOrderToLipRespondent = hearingFormGenerator.generate(
                civilCaseData,
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
            );
        }

        /*
         * Send Judge order document to Lip Applicant
         * */
        if (Objects.nonNull(postJudgeOrderToLipApplicant)) {
            sendJudgeFinalOrderPrintService(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                postJudgeOrderToLipApplicant, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);
        }

        /*
         * Send Judge order document to Lip Respondent
         * */
        if (Objects.nonNull(postJudgeOrderToLipRespondent)) {
            sendJudgeFinalOrderPrintService(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                postJudgeOrderToLipRespondent, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);
        }
    }

    private void sendJudgeFinalOrderPrintService(String authorisation,
                                                 CaseDocument decision,
                                                 GeneralApplicationCaseData caseData,
                                                 GeneralApplicationCaseData civilCaseData,
                                                 FlowFlag lipUserType) {
        sendFinalOrderPrintService
            .sendJudgeFinalOrderToPrintForLIP(
                authorisation,
                decision.getDocumentLink(), caseData, civilCaseData, lipUserType);
    }
}
