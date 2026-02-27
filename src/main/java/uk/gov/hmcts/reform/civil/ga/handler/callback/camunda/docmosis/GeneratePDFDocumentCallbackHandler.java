package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.consentorder.ConsentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.directionorder.DirectionOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DismissalOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.AssistedOrderFormGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.HearingOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationConcurrentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationSequentialOrderGenerator;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGES_FORM;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratePDFDocumentCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_JUDGES_FORM);
    private static final String TASK_ID = "CreatePDFDocument";
    private final GeneralOrderGenerator generalOrderGenerator;
    private final RequestForInformationGenerator requestForInformationGenerator;
    private final DirectionOrderGenerator directionOrderGenerator;
    private final DismissalOrderGenerator dismissalOrderGenerator;
    private final HearingOrderGenerator hearingOrderGenerator;
    private final WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentialOrderGenerator;
    private final WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;
    private final FreeFormOrderGenerator freeFormOrderGenerator;
    private final AssistedOrderFormGenerator assistedOrderFormGenerator;
    private final ConsentOrderGenerator consentOrderGenerator;
    private final ObjectMapper objectMapper;
    private final SendFinalOrderPrintService sendFinalOrderPrintService;

    private final AssignCategoryId assignCategoryId;
    private final GaForLipService gaForLipService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggleService featureToggleService;

    @Value("${print.service.enabled}")
    public String printServiceEnabled;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::createPDFDocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse createPDFDocument(CallbackParams callbackParams) {

        /*
         * Setting up the CategoryID for Order documents will be covered under CIV-8316
         * as it has dependency on CIV-7926.
         *
         * Uncomment the assignCategoryID code for setting up the categoryID after CIV-7926 is merged in Civil repo
         * */

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Create PDF document for case: {}", caseData.getCcdCaseReference());

        GeneralApplicationCaseData civilCaseData = new GeneralApplicationCaseData().build();
        if (gaForLipService.isGaForLip(caseData)) {
            civilCaseData = caseDetailsConverter
                .toGeneralApplicationCaseData(coreCaseDataService
                                .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        }

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        CaseDocument decision = null;
        CaseDocument postJudgeOrderToLipApplicant = null;
        CaseDocument postJudgeOrderToLipRespondent = null;
        if (Objects.nonNull(caseData.getApproveConsentOrder())) {
            decision = consentOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            List<Element<CaseDocument>> consentOrderDocumentList =
                ofNullable(caseData.getConsentOrderDocument()).orElse(newArrayList());

            consentOrderDocumentList.addAll(wrapElements(decision));

            assignCategoryId.assignCategoryIdToCollection(consentOrderDocumentList,
                                                          document -> document.getValue().getDocumentLink(),
                                                          AssignCategoryId.ORDER_DOCUMENTS);
            caseDataBuilder.consentOrderDocument(consentOrderDocumentList);
        } else if (Objects.nonNull(caseData.getFinalOrderSelection())) {
            if (caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER)) {
                decision = freeFormOrderGenerator.generate(
                        caseDataBuilder.build(),
                        callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

                /*
                 * Generate Judge Request for Information order document with LIP Applicant Post Address
                 * */
                if (gaForLipService.isLipApp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {

                    postJudgeOrderToLipApplicant = generateFreeFormSendLetterDocForApplicant(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString());
                }

                /*
                 * Generate Judge Request for Information order document with LIP Respondent Post Address
                 * */
                if (gaForLipService.isLipResp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {

                    postJudgeOrderToLipRespondent = generateFreeFormSendLetterDocForRespondent(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString());
                }

            } else if (caseData.getFinalOrderSelection().equals(ASSISTED_ORDER)) {

                decision = assistedOrderFormGenerator.generate(
                        caseDataBuilder.build(),
                        callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

                /*
                 * Generate Judge Request for Information order document with LIP Applicant Post Address
                 * */
                if (gaForLipService.isLipApp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                    postJudgeOrderToLipApplicant = assistedOrderFormGenerator.generate(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString(),
                        FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
                    );
                }

                /*
                 * Generate Judge Request for Information order document with LIP Respondent Post Address
                 * */
                if (gaForLipService.isLipResp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                    postJudgeOrderToLipRespondent = assistedOrderFormGenerator.generate(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString(),
                        FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
                }
            }
            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.FINAL_ORDER_DOC
                );
            } else {
                List<Element<CaseDocument>> newGeneralOrderDocumentList =
                    ofNullable(caseData.getGeneralOrderDocument()).orElse(newArrayList());

                newGeneralOrderDocumentList.addAll(wrapElements(decision));

                assignCategoryId.assignCategoryIdToCollection(
                    newGeneralOrderDocumentList,
                    document -> document.getValue().getDocumentLink(),
                    AssignCategoryId.ORDER_DOCUMENTS
                );
                caseDataBuilder.generalOrderDocument(newGeneralOrderDocumentList);
            }
        } else if (isGeneralOrder(caseData)) {
            decision = generalOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = generalOrderGenerator.generate(civilCaseData,
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString(), FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
                );
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = generalOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                );
            }
            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.GENERAL_ORDER_DOC
                );
            } else {

                assignCategoryId.assignCategoryIdToCaseDocument(
                    decision,
                    AssignCategoryId.ORDER_DOCUMENTS
                );

                caseDataBuilder.generalOrderDocument(wrapElements(decision));
            }
        } else if (isDirectionOrder(caseData)) {
            decision = directionOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = directionOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
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
                postJudgeOrderToLipRespondent = directionOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
            }

            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC
                );
            } else {
                List<Element<CaseDocument>> newDirectionOrderDocumentList =
                    ofNullable(caseData.getDirectionOrderDocument()).orElse(newArrayList());

                newDirectionOrderDocumentList.addAll(wrapElements(decision));

                assignCategoryId.assignCategoryIdToCollection(
                    newDirectionOrderDocumentList,
                    document -> document.getValue().getDocumentLink(),
                    AssignCategoryId.ORDER_DOCUMENTS
                );
                caseDataBuilder.directionOrderDocument(newDirectionOrderDocumentList);
            }

        } else if (isDismissalOrder(caseData)) {
            decision = dismissalOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * if GA is with notice
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = dismissalOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
                );
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = dismissalOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
            }
            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.DISMISSAL_ORDER_DOC
                );
            } else {
                assignCategoryId.assignCategoryIdToCaseDocument(decision,
                                                                AssignCategoryId.ORDER_DOCUMENTS);

                caseDataBuilder.dismissalOrderDocument(wrapElements(decision));
            }

        } else if (isHearingOrder(caseData)) {
            decision = hearingOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = hearingOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * if GA is with notice
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = hearingOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
            }

            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.HEARING_ORDER_DOC
                );
            } else {

                assignCategoryId.assignCategoryIdToCaseDocument(
                    decision,
                    AssignCategoryId.APPLICATIONS
                );
                caseDataBuilder.hearingOrderDocument(wrapElements(decision));
            }
        } else if (isWrittenRepSeqOrder(caseData)) {
            decision = writtenRepresentationSequentialOrderGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * if GA is with notice
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = writtenRepresentationSequentialOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
                );
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = writtenRepresentationSequentialOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
            }
            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC
                );
            } else {
                List<Element<CaseDocument>> newWrittenRepSequentialDocumentList =
                    ofNullable(caseData.getWrittenRepSequentialDocument()).orElse(newArrayList());

                newWrittenRepSequentialDocumentList.addAll(wrapElements(decision));

                assignCategoryId.assignCategoryIdToCollection(
                    newWrittenRepSequentialDocumentList,
                    document -> document.getValue().getDocumentLink(),
                    AssignCategoryId.APPLICATIONS
                );
                caseDataBuilder.writtenRepSequentialDocument(newWrittenRepSequentialDocumentList);
            }
        } else if (isWrittenRepConOrder(caseData)) {
            decision = writtenRepresentationConcurrentOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            /*
             * Generate Judge Request for Information order document with LIP Applicant Post Address
             * */
            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = writtenRepresentationConcurrentOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = writtenRepresentationConcurrentOrderGenerator
                    .generate(civilCaseData,
                              caseDataBuilder.build(),
                              callbackParams.getParams().get(BEARER_TOKEN).toString(),
                              FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                    );
            }

            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC
                );
            } else {
                List<Element<CaseDocument>> newWrittenRepConcurrentDocumentList =
                    ofNullable(caseData.getWrittenRepConcurrentDocument()).orElse(newArrayList());

                newWrittenRepConcurrentDocumentList.addAll(wrapElements(decision));
                assignCategoryId.assignCategoryIdToCollection(
                    newWrittenRepConcurrentDocumentList,
                    document -> document.getValue().getDocumentLink(),
                    AssignCategoryId.APPLICATIONS
                );

                caseDataBuilder.writtenRepConcurrentDocument(newWrittenRepConcurrentDocumentList);
            }

        } else if (isRequestMoreInfo(caseData) || isRequestMoreInfoAndSendAppToOtherParty(caseData)) {
            GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption = Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo()).map(
                GAJudicialRequestMoreInfo::getRequestMoreInfoOption).orElse(null);
            decision = requestForInformationGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()
                && gaJudgeRequestMoreInfoOption != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY) {
                setPreTranslationDocument(
                    caseData,
                    caseDataBuilder,
                    decision,
                    PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC
                );
            } else {
                List<Element<CaseDocument>> newRequestForInfoDocumentList =
                    ofNullable(caseData.getRequestForInformationDocument()).orElse(newArrayList());

                newRequestForInfoDocumentList.addAll(wrapElements(decision));

                assignCategoryId.assignCategoryIdToCollection(
                    newRequestForInfoDocumentList,
                    document -> document.getValue().getDocumentLink(),
                    AssignCategoryId.APPLICATIONS
                );
                caseDataBuilder.requestForInformationDocument(newRequestForInfoDocumentList);
            }
            /*
            * Generate Judge Request for Information order document with LIP Applicant Post Address
            * */

            if (gaForLipService.isLipApp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipApplicant = requestForInformationGenerator.generate(
                    civilCaseData,
                    caseData,
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
                );
            }

            /*
             * Generate Judge Request for Information order document with LIP Respondent Post Address
             * */
            if (gaForLipService.isLipResp(caseData)
                && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {
                postJudgeOrderToLipRespondent = requestForInformationGenerator.generate(
                    civilCaseData,
                    caseData,
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                );
            }

        } else if (Objects.nonNull(caseData.getJudicialDecision())) {
            if (caseData.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.FREE_FORM_ORDER)) {

                /*
                 * Generate Judge Request for Information order document with LIP Respondent Post Address
                 * */
                if (gaForLipService.isLipResp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {

                    postJudgeOrderToLipRespondent = generateFreeFormSendLetterDocForRespondent(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString());
                }

                /*
                 * Generate Judge Request for Information order document with LIP Applicant Post Address
                 * */
                if (gaForLipService.isLipApp(caseData)
                    && (!featureToggleService.isGaForWelshEnabled() || !caseData.isApplicationBilingual())) {

                    postJudgeOrderToLipApplicant = generateFreeFormSendLetterDocForApplicant(
                        civilCaseData,
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString());
                }

                List<Element<CaseDocument>> documentList =
                    ofNullable(caseData.getGeneralOrderDocument()).orElse(newArrayList());

                decision = freeFormOrderGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );
                if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                    setPreTranslationDocument(
                        caseData,
                        caseDataBuilder,
                        decision,
                        PreTranslationGaDocumentType.GENERAL_ORDER_DOC
                    );
                } else {
                    documentList.addAll(wrapElements(decision));
                    assignCategoryId.assignCategoryIdToCollection(
                        documentList,
                        document -> document.getValue().getDocumentLink(),
                        AssignCategoryId.ORDER_DOCUMENTS
                    );
                    caseDataBuilder.generalOrderDocument(documentList);
                }
            }
        }

        /*
        * Send Judge order document to Lip Applicant
        * */
        if (printServiceEnabled.equals("true") && Objects.nonNull(postJudgeOrderToLipApplicant)) {
            sendJudgeFinalOrderPrintService(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                postJudgeOrderToLipApplicant, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);
        }

        /*
         * Send Judge order document to Lip Respondent
         * */
        if (printServiceEnabled.equals("true") && Objects.nonNull(postJudgeOrderToLipRespondent)) {
            sendJudgeFinalOrderPrintService(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                postJudgeOrderToLipRespondent, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void setPreTranslationDocument(GeneralApplicationCaseData caseData, GeneralApplicationCaseData caseDataBuilder,
                                           CaseDocument decision, PreTranslationGaDocumentType doctype) {
        List<Element<CaseDocument>> preTranslatedDocuments =
            Optional.ofNullable(caseData.getPreTranslationGaDocuments())
                .orElseGet(ArrayList::new);
        preTranslatedDocuments.add(element(decision));
        assignCategoryId.assignCategoryIdToCollection(
            preTranslatedDocuments,
            document -> document.getValue().getDocumentLink(),
            AssignCategoryId.APPLICATIONS
        );
        caseDataBuilder.preTranslationGaDocuments(preTranslatedDocuments);
        caseDataBuilder.preTranslationGaDocumentType(doctype);
    }

    private CaseDocument generateFreeFormSendLetterDocForApplicant(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String auth) {

        /*
         * Generate Judge Request for Information order document with LIP Applicant Post Address
         * */
        log.info("Generate Judge Request for Information order document with LIP Applicant Post Address for case: {}", caseData.getCcdCaseReference());
        return freeFormOrderGenerator.generate(
            civilCaseData,
            caseData,
            auth,
            FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
        );
    }

    private CaseDocument generateFreeFormSendLetterDocForRespondent(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String auth) {

        /*
         * Generate Judge Request for Information order document with LIP Respondent Post Address
         * if GA is with notice
         * */
        log.info("Generate Judge Request for Information order document with LIP Respondent Post Address if GA is with notice for case: {}", caseData.getCcdCaseReference());
        return freeFormOrderGenerator.generate(
            civilCaseData,
            caseData,
            auth,
            FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
        );
    }

    private void sendJudgeFinalOrderPrintService(String authorisation,
                                                 CaseDocument decision,
                                                 GeneralApplicationCaseData caseData,
                                                 GeneralApplicationCaseData civilCaseData,
                                                 FlowFlag lipUserType) {
        log.info("Send judge final order print service for case: {}", caseData.getCcdCaseReference());
        sendFinalOrderPrintService
            .sendJudgeFinalOrderToPrintForLIP(
                authorisation,
                decision.getDocumentLink(), caseData, civilCaseData, lipUserType);
    }

    private boolean isRequestMoreInfo(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(REQUEST_MORE_INFO)
                && caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate() != null
                && caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoText() != null;
    }

    private boolean isRequestMoreInfoAndSendAppToOtherParty(final GeneralApplicationCaseData caseData) {
        GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption = Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo()).map(
            GAJudicialRequestMoreInfo::getRequestMoreInfoOption).orElse(null);

        return gaForLipService.isLipApp(caseData) && caseData.getJudicialDecision().getDecision() == REQUEST_MORE_INFO
            && gaJudgeRequestMoreInfoOption == GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
    }

    private boolean isWrittenRepConOrder(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
                && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                .getWrittenConcurrentRepresentationsBy() != null
                 && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                .getWrittenOption().equals(CONCURRENT_REPRESENTATIONS)
                ;
    }

    private boolean isWrittenRepSeqOrder(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
                && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                .getWrittenSequentailRepresentationsBy() != null
                 && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                .getSequentialApplicantMustRespondWithin() != null
                 && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                .getWrittenOption().equals(SEQUENTIAL_REPRESENTATIONS);
    }

    private boolean isHearingOrder(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(LIST_FOR_A_HEARING)
                && caseData.getJudicialListForHearing() != null;
    }

    private boolean isDismissalOrder(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(DISMISS_THE_APPLICATION);
    }

    private boolean isDirectionOrder(final GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                && caseData.getJudicialDecisionMakeOrder().getDirectionsText() != null
                && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(GIVE_DIRECTIONS_WITHOUT_HEARING);
    }

    private boolean isGeneralOrder(GeneralApplicationCaseData caseData) {
        return caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                && caseData.getJudicialDecisionMakeOrder().getOrderText() != null
                && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(APPROVE_OR_EDIT);
    }
}
