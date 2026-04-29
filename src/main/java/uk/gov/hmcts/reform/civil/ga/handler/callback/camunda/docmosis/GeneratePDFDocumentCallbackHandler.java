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
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Create PDF document for case: {}", caseData.getCcdCaseReference());
        GenerationContext context = buildGenerationContext(callbackParams, caseData);
        GenerationResult generationResult = generateDecisionDocuments(context);

        sendPrintIfEnabled(context, generationResult.postJudgeOrderToLipApplicant(), FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);
        sendPrintIfEnabled(context, generationResult.postJudgeOrderToLipRespondent(), FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(context.caseDataBuilder().build().toMap(objectMapper))
            .build();
    }

    private GenerationContext buildGenerationContext(CallbackParams callbackParams, GeneralApplicationCaseData caseData) {
        return new GenerationContext(
            caseData,
            getCivilCaseData(caseData),
            caseData.copy(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
    }

    private GeneralApplicationCaseData getCivilCaseData(GeneralApplicationCaseData caseData) {
        if (!gaForLipService.isGaForLip(caseData)) {
            return new GeneralApplicationCaseData().build();
        }
        return caseDetailsConverter.toGeneralApplicationCaseData(
            coreCaseDataService.getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference()))
        );
    }

    private GenerationResult generateDecisionDocuments(GenerationContext context) {
        return handlePreDecisionOrders(context)
            .or(() -> handleMakeOrderDecisions(context))
            .or(() -> handleHearingAndRepresentationOrders(context))
            .or(() -> handleInformationAndFreeFormOrders(context))
            .orElseGet(GenerationResult::empty);
    }

    private Optional<GenerationResult> handlePreDecisionOrders(GenerationContext context) {
        GeneralApplicationCaseData caseData = context.caseData();
        if (Objects.nonNull(caseData.getApproveConsentOrder())) {
            return Optional.of(handleConsentOrder(context));
        }
        if (Objects.nonNull(caseData.getFinalOrderSelection())) {
            return Optional.of(handleFinalOrder(context));
        }
        return Optional.empty();
    }

    private Optional<GenerationResult> handleMakeOrderDecisions(GenerationContext context) {
        GeneralApplicationCaseData caseData = context.caseData();
        if (isGeneralOrder(caseData)) {
            return Optional.of(handleGeneralOrder(context));
        }
        if (isDirectionOrder(caseData)) {
            return Optional.of(handleDirectionOrder(context));
        }
        if (isDismissalOrder(caseData)) {
            return Optional.of(handleDismissalOrder(context));
        }
        return Optional.empty();
    }

    private Optional<GenerationResult> handleHearingAndRepresentationOrders(GenerationContext context) {
        GeneralApplicationCaseData caseData = context.caseData();
        if (isHearingOrder(caseData)) {
            return Optional.of(handleHearingOrder(context));
        }
        if (isWrittenRepSeqOrder(caseData)) {
            return Optional.of(handleWrittenRepSequentialOrder(context));
        }
        if (isWrittenRepConOrder(caseData)) {
            return Optional.of(handleWrittenRepConcurrentOrder(context));
        }
        return Optional.empty();
    }

    private Optional<GenerationResult> handleInformationAndFreeFormOrders(GenerationContext context) {
        GeneralApplicationCaseData caseData = context.caseData();
        if (isRequestMoreInfo(caseData) || isRequestMoreInfoAndSendAppToOtherParty(caseData)) {
            return Optional.of(handleRequestMoreInfoOrder(context));
        }
        if (isJudicialFreeFormOrder(caseData)) {
            return Optional.of(handleJudicialFreeFormOrder(context));
        }
        return Optional.empty();
    }

    private GenerationResult handleConsentOrder(GenerationContext context) {
        CaseDocument decision = consentOrderGenerator.generate(context.caseDataBuilder().build(), context.authorisation());
        context.caseDataBuilder().consentOrderDocument(
            appendDocument(context.caseData().getConsentOrderDocument(), decision, AssignCategoryId.ORDER_DOCUMENTS)
        );
        return GenerationResult.empty();
    }

    private GenerationResult handleFinalOrder(GenerationContext context) {
        if (FREE_FORM_ORDER.equals(context.caseData().getFinalOrderSelection())) {
            return handleFreeFormFinalOrder(context);
        }
        if (ASSISTED_ORDER.equals(context.caseData().getFinalOrderSelection())) {
            return handleAssistedFinalOrder(context);
        }
        return GenerationResult.empty();
    }

    private GenerationResult handleFreeFormFinalOrder(GenerationContext context) {
        CaseDocument decision = freeFormOrderGenerator.generate(context.caseDataBuilder().build(), context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateFreeFormLipApplicantDocument(context);
        CaseDocument postJudgeOrderToLipRespondent = generateFreeFormLipRespondentDocument(context);
        storeFinalOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private GenerationResult handleAssistedFinalOrder(GenerationContext context) {
        CaseDocument decision = assistedOrderFormGenerator.generate(context.caseDataBuilder().build(), context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            context.caseData(),
                assistedOrderFormGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            context.caseData(),
                assistedOrderFormGenerator::generate
        );
        storeFinalOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeFinalOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.FINAL_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().generalOrderDocument(
            appendDocument(context.caseData().getGeneralOrderDocument(), decision, AssignCategoryId.ORDER_DOCUMENTS)
        );
    }

    private GenerationResult handleGeneralOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = generalOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                generalOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                generalOrderGenerator::generate
        );
        storeGeneralOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeGeneralOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.GENERAL_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().generalOrderDocument(wrapDocument(decision, AssignCategoryId.ORDER_DOCUMENTS));
    }

    private GenerationResult handleDirectionOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = directionOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                directionOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                directionOrderGenerator::generate
        );
        storeDirectionOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeDirectionOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().directionOrderDocument(
            appendDocument(context.caseData().getDirectionOrderDocument(), decision, AssignCategoryId.ORDER_DOCUMENTS)
        );
    }

    private GenerationResult handleDismissalOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = dismissalOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                dismissalOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                dismissalOrderGenerator::generate
        );
        storeDismissalOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeDismissalOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.DISMISSAL_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().dismissalOrderDocument(wrapDocument(decision, AssignCategoryId.ORDER_DOCUMENTS));
    }

    private GenerationResult handleHearingOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = hearingOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                hearingOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                hearingOrderGenerator::generate
        );
        storeHearingOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeHearingOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.HEARING_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().hearingOrderDocument(wrapDocument(decision, AssignCategoryId.APPLICATIONS));
    }

    private GenerationResult handleWrittenRepSequentialOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = writtenRepresentationSequentialOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                writtenRepresentationSequentialOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                writtenRepresentationSequentialOrderGenerator::generate
        );
        storeWrittenRepSequentialDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeWrittenRepSequentialDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().writtenRepSequentialDocument(
            appendDocument(context.caseData().getWrittenRepSequentialDocument(), decision, AssignCategoryId.APPLICATIONS)
        );
    }

    private GenerationResult handleWrittenRepConcurrentOrder(GenerationContext context) {
        GeneralApplicationCaseData caseDataBuilder = context.caseDataBuilder().build();
        CaseDocument decision = writtenRepresentationConcurrentOrderGenerator.generate(caseDataBuilder, context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            caseDataBuilder,
                writtenRepresentationConcurrentOrderGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            caseDataBuilder,
                writtenRepresentationConcurrentOrderGenerator::generate
        );
        storeWrittenRepConcurrentDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeWrittenRepConcurrentDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().writtenRepConcurrentDocument(
            appendDocument(context.caseData().getWrittenRepConcurrentDocument(), decision, AssignCategoryId.APPLICATIONS)
        );
    }

    private GenerationResult handleRequestMoreInfoOrder(GenerationContext context) {
        CaseDocument decision = requestForInformationGenerator.generate(context.caseDataBuilder().build(), context.authorisation());
        CaseDocument postJudgeOrderToLipApplicant = generateLipApplicantDocument(
            context,
            context.caseData(),
                requestForInformationGenerator::generate
        );
        CaseDocument postJudgeOrderToLipRespondent = generateLipRespondentDocument(
            context,
            context.caseData(),
                requestForInformationGenerator::generate
        );
        storeRequestForInformationDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeRequestForInformationDocument(GenerationContext context, CaseDocument decision) {
        GAJudgeRequestMoreInfoOption requestMoreInfoOption = getRequestMoreInfoOption(context.caseData());
        if (isWelshBilingual(context.caseData())
            && requestMoreInfoOption != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().requestForInformationDocument(
            appendDocument(context.caseData().getRequestForInformationDocument(), decision, AssignCategoryId.APPLICATIONS)
        );
    }

    private GenerationResult handleJudicialFreeFormOrder(GenerationContext context) {
        CaseDocument postJudgeOrderToLipRespondent = generateFreeFormLipRespondentDocument(context);
        CaseDocument postJudgeOrderToLipApplicant = generateFreeFormLipApplicantDocument(context);
        CaseDocument decision = freeFormOrderGenerator.generate(context.caseDataBuilder().build(), context.authorisation());
        storeJudicialFreeFormOrderDocument(context, decision);
        return new GenerationResult(postJudgeOrderToLipApplicant, postJudgeOrderToLipRespondent);
    }

    private void storeJudicialFreeFormOrderDocument(GenerationContext context, CaseDocument decision) {
        if (isWelshBilingual(context.caseData())) {
            setPreTranslationDocument(
                context.caseData(),
                context.caseDataBuilder(),
                decision,
                PreTranslationGaDocumentType.GENERAL_ORDER_DOC
            );
            return;
        }
        context.caseDataBuilder().generalOrderDocument(
            appendDocument(context.caseData().getGeneralOrderDocument(), decision, AssignCategoryId.ORDER_DOCUMENTS)
        );
    }

    private List<Element<CaseDocument>> appendDocument(List<Element<CaseDocument>> documents,
                                                       CaseDocument decision,
                                                       String categoryId) {
        List<Element<CaseDocument>> updatedDocuments = ofNullable(documents).orElse(newArrayList());
        updatedDocuments.addAll(wrapElements(decision));
        assignCategoryId.assignCategoryIdToCollection(
            updatedDocuments,
            document -> document.getValue().getDocumentLink(),
            categoryId
        );
        return updatedDocuments;
    }

    private List<Element<CaseDocument>> wrapDocument(CaseDocument decision, String categoryId) {
        assignCategoryId.assignCategoryIdToCaseDocument(decision, categoryId);
        return wrapElements(decision);
    }

    private boolean isJudicialFreeFormOrder(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getJudicialDecision())
            && caseData.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.FREE_FORM_ORDER);
    }

    private boolean isWelshBilingual(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual();
    }

    private CaseDocument generateFreeFormLipApplicantDocument(GenerationContext context) {
        if (shouldGenerateLipPost(context.caseData()) || !gaForLipService.isLipApp(context.caseData())) {
            return null;
        }
        return generateFreeFormSendLetterDocForApplicant(
            context.civilCaseData(),
            context.caseData(),
            context.authorisation()
        );
    }

    private CaseDocument generateFreeFormLipRespondentDocument(GenerationContext context) {
        if (shouldGenerateLipPost(context.caseData()) || !gaForLipService.isLipResp(context.caseData())) {
            return null;
        }
        return generateFreeFormSendLetterDocForRespondent(
            context.civilCaseData(),
            context.caseData(),
            context.authorisation()
        );
    }

    private CaseDocument generateLipApplicantDocument(GenerationContext context,
                                                      GeneralApplicationCaseData sourceCaseData,
                                                      PostOrderGenerator generator) {
        if (shouldGenerateLipPost(context.caseData()) || !gaForLipService.isLipApp(context.caseData())) {
            return null;
        }
        return generator.generate(
            context.civilCaseData(),
            sourceCaseData,
            context.authorisation(),
            FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT
        );
    }

    private CaseDocument generateLipRespondentDocument(GenerationContext context,
                                                       GeneralApplicationCaseData sourceCaseData,
                                                       PostOrderGenerator generator) {
        if (shouldGenerateLipPost(context.caseData()) || !gaForLipService.isLipResp(context.caseData())) {
            return null;
        }
        return generator.generate(
            context.civilCaseData(),
            sourceCaseData,
            context.authorisation(),
            FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
        );
    }

    private boolean shouldGenerateLipPost(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual();
    }

    private GAJudgeRequestMoreInfoOption getRequestMoreInfoOption(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo())
            .map(GAJudicialRequestMoreInfo::getRequestMoreInfoOption)
            .orElse(null);
    }

    private void sendPrintIfEnabled(GenerationContext context, CaseDocument decision, FlowFlag lipUserType) {
        if (isPrintServiceEnabled() && Objects.nonNull(decision)) {
            sendJudgeFinalOrderPrintService(
                context.authorisation(),
                decision,
                context.caseData(),
                context.civilCaseData(),
                lipUserType
            );
        }
    }

    private boolean isPrintServiceEnabled() {
        return "true".equals(printServiceEnabled);
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

    @FunctionalInterface
    private interface PostOrderGenerator {
        CaseDocument generate(GeneralApplicationCaseData civilCaseData,
                              GeneralApplicationCaseData caseData,
                              String authorisation,
                              FlowFlag flowFlag);
    }

    private record GenerationContext(GeneralApplicationCaseData caseData,
                                     GeneralApplicationCaseData civilCaseData,
                                     GeneralApplicationCaseData caseDataBuilder,
                                     String authorisation) {
    }

    private record GenerationResult(CaseDocument postJudgeOrderToLipApplicant,
                                    CaseDocument postJudgeOrderToLipRespondent) {
        private static GenerationResult empty() {
            return new GenerationResult(null, null);
        }
    }
}
