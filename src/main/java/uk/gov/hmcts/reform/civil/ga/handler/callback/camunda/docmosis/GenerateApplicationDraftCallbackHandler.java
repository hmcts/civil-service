package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralApplicationDraftGenerator;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class GenerateApplicationDraftCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_DRAFT_DOCUMENT);
    private static final String TASK_ID = "GenerateDraftDocumentId";
    private final ObjectMapper objectMapper;
    private final GeneralApplicationDraftGenerator gaDraftGenerator;
    private final AssignCategoryId assignCategoryId;

    private final GeneralAppFeesService generalAppFeesService;
    private final GaForLipService gaForLipService;
    private final FeatureToggleService featureToggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::createPDFdocument);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // Generate Draft Document if it's an Urgent application and the fee is FREE
    private boolean isApplicationUrgentAndFreeFee(GeneralApplicationCaseData caseData) {
        return generalAppFeesService.isFreeApplication(caseData)
            && Optional.ofNullable(caseData.getGeneralAppUrgencyRequirement())
            .map(GAUrgencyRequirement::getGeneralAppUrgency)
            .filter(YES::equals)
            .isPresent();
    }

    // Generate Draft Document if it's an Urgent application and after fee is paid
    // Initiate General application after a payment camunda task started
    private boolean isApplicationUrgentAndFeePaid(GeneralApplicationCaseData caseData) {
        return isFeePaid(caseData) && Optional.ofNullable(caseData.getGeneralAppUrgencyRequirement())
            .map(GAUrgencyRequirement::getGeneralAppUrgency)
            .filter(YES::equals)
            .isPresent();
    }

    // Generate a Draft Document if it's non-urgent, without notice and after a fee is paid
    private boolean isGANonUrgentWithOutNoticeFeePaid(GeneralApplicationCaseData caseData) {
        return isFeePaid(caseData) && Optional.ofNullable(caseData.getGeneralAppInformOtherParty())
            .map(GAInformOtherParty::getIsWithNotice)
            .filter(NO::equals)
            .isPresent();
    }

    private boolean isFeePaid(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppPBADetails())
            .filter(pba ->
                        pba.getFee() != null && !"FREE".equalsIgnoreCase(pba.getFee().getCode()))
            .map(GeneralApplicationPbaDetails::getPaymentDetails)
            .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
            .isPresent();
    }

    private CallbackResponse createPDFdocument(CallbackParams callbackParams) {
        var caseData = callbackParams.getGeneralApplicationCaseData().copy();

        if (gaForLipService.isGaForLip(caseData)) {
            handleLipDraftGeneration(callbackParams, caseData);
        } else {
            handleNonLipDraftGeneration(callbackParams, caseData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper)).build();
    }

    private void handleLipDraftGeneration(CallbackParams callbackParams,
                                          GeneralApplicationCaseData caseData) {
        if (generalAppFeesService.isFreeApplication(caseData) || isFeePaid(caseData)) {
            var gaDraftDocument = generateDraftDocument(callbackParams, caseData);
            if (shouldUseWelshTranslation(caseData)) {
                updateWelshTranslationDocuments(caseData, gaDraftDocument);
            } else {
                updateDraftApplicationList(caseData, gaDraftDocument);
            }
        }
    }

    private boolean shouldUseWelshTranslation(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForWelshEnabled()
            && ((caseData.getIsGaApplicantLip() == YES && caseData.isApplicantBilingual())
            || (caseData.isRespondentBilingual() && caseData.getIsGaRespondentOneLip() == YES));
    }

    private void updateWelshTranslationDocuments(GeneralApplicationCaseData caseData,
                                                 CaseDocument gaDraftDocument) {
        var preTranslatedDocuments = Optional.ofNullable(
            caseData.getPreTranslationGaDocuments())
            .orElseGet(ArrayList::new);
        preTranslatedDocuments.add(element(gaDraftDocument));

        assignCategoryId.assignCategoryIdToCollection(
            preTranslatedDocuments,
            document -> document.getValue().getDocumentLink(),
            AssignCategoryId.APPLICATIONS
        );

        var docType = (caseData.getRespondentsResponses() != null
            && !caseData.getRespondentsResponses().isEmpty())
            ? PreTranslationGaDocumentType.RESPOND_TO_APPLICATION_SUMMARY_DOC
            : PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC;

        caseData.preTranslationGaDocumentType(docType).preTranslationGaDocuments(preTranslatedDocuments);
    }

    private void updateDraftApplicationList(GeneralApplicationCaseData caseData,
                                            CaseDocument gaDraftDocument) {
        var draftApplicationList = Optional.ofNullable(caseData.getGaDraftDocument()).orElseGet(ArrayList::new);
        draftApplicationList.add(element(gaDraftDocument));

        assignCategoryId.assignCategoryIdToCollection(
            draftApplicationList,
            document -> document.getValue().getDocumentLink(),
            AssignCategoryId.APPLICATIONS
        );
        caseData.gaDraftDocument(draftApplicationList);
    }

    private void handleNonLipDraftGeneration(CallbackParams callbackParams,
                                             GeneralApplicationCaseData caseData) {
        /*
        1. Draft document should not be generated if judge had made the decision on application
        2. Draft document should be generated only if all the respondents responded in Multiparty
        3. Draft document should be generated only if Free fee application
        4. Draft document should be generated only after payment is made for urgent application and without notice
        */
        if (shouldGenerateNonLipDraft(caseData)) {
            var gaDraftDocument = generateDraftDocument(callbackParams, caseData);
            var draftApplicationList = wrapElements(gaDraftDocument);

            assignCategoryId.assignCategoryIdToCollection(
                draftApplicationList,
                document -> document.getValue().getDocumentLink(),
                AssignCategoryId.APPLICATIONS
            );
            caseData.gaDraftDocument(draftApplicationList);
        }
    }

    private boolean shouldGenerateNonLipDraft(GeneralApplicationCaseData caseData) {
        return (isApplicationUrgentAndFreeFee(caseData)
            || isGANonUrgentWithOutNoticeFeePaid(caseData)
            || isApplicationUrgentAndFeePaid(caseData)
            || isRespondentsResponseSatisfied(caseData, caseData))
            && Objects.isNull(caseData.getJudicialDecision());
    }

    private CaseDocument generateDraftDocument(CallbackParams callbackParams, GeneralApplicationCaseData caseData) {
        return gaDraftGenerator.generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

}
