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
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralApplicationDraftGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;
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

    // Generate Draft Document if it's Urgent application and fee is FREE
    private boolean isApplicationUrgentAndFreeFee(GeneralApplicationCaseData caseData) {
        return generalAppFeesService.isFreeApplication(caseData)
            && caseData.getGeneralAppUrgencyRequirement() != null
            && YES.equals(caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency());
    }

    // Generate Draft Document if it's Urgent application and after fee is paid
    // Initiate General application after payment camunda task started
    private boolean isApplicationUrgentAndFeePaid(GeneralApplicationCaseData caseData) {
        return isFeePaid(caseData) && caseData.getGeneralAppUrgencyRequirement() != null
            && YES.equals(caseData.getGeneralAppUrgencyRequirement()
                              .getGeneralAppUrgency());

    }

    // Generate Draft Document if it's non-urgent, without notice and after fee is paid
    private boolean isGANonUrgentWithOutNoticeFeePaid(GeneralApplicationCaseData caseData) {
        return isFeePaid(caseData) && caseData.getGeneralAppInformOtherParty() != null
            && NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
    }

    private boolean isFeePaid(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppPBADetails() != null
            && !caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")
            && caseData.getGeneralAppPBADetails().getPaymentDetails() != null
            && caseData.getGeneralAppPBADetails().getPaymentDetails().getStatus().equals(PaymentStatus.SUCCESS);
    }

    private CallbackResponse createPDFdocument(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        CaseDocument gaDraftDocument;

        if (gaForLipService.isGaForLip(caseData)) {
            if (generalAppFeesService.isFreeApplication(caseData) || isFeePaid(caseData)) {

                List<Element<CaseDocument>> draftApplicationList = caseData.getGaDraftDocument();
                if (Objects.isNull(draftApplicationList)) {
                    draftApplicationList = newArrayList();
                }

                gaDraftDocument = gaDraftGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

                if (featureToggleService.isGaForWelshEnabled()
                    && (((caseData.getIsGaApplicantLip() == YES
                    && caseData.isApplicantBilingual())
                    || (caseData.isRespondentBilingual() && caseData.getIsGaRespondentOneLip() == YES)))) {
                    List<Element<CaseDocument>> preTranslatedDocuments =
                        Optional.ofNullable(caseData.getPreTranslationGaDocuments())
                            .orElseGet(ArrayList::new);
                    preTranslatedDocuments.add(element(gaDraftDocument));
                    assignCategoryId.assignCategoryIdToCollection(
                        preTranslatedDocuments,
                        document -> document.getValue().getDocumentLink(),
                        AssignCategoryId.APPLICATIONS
                    );

                    if (caseData.getRespondentsResponses() != null
                        && !caseData.getRespondentsResponses().isEmpty()) {
                        caseDataBuilder.preTranslationGaDocumentType(PreTranslationGaDocumentType.RESPOND_TO_APPLICATION_SUMMARY_DOC);
                    } else {
                        caseDataBuilder.preTranslationGaDocumentType(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC);
                    }
                    caseDataBuilder.preTranslationGaDocuments(preTranslatedDocuments);
                } else {
                    draftApplicationList.add(element(gaDraftDocument));
                    assignCategoryId.assignCategoryIdToCollection(
                        draftApplicationList,
                        document -> document.getValue().getDocumentLink(),
                        AssignCategoryId.APPLICATIONS
                    );
                    caseDataBuilder.gaDraftDocument(draftApplicationList);
                }
            }
        } else {
            /*
            1. Draft document should not be generated if judge had made the decision on application
            2. Draft document should be generated only if all the respondents responded in Multiparty
            3. Draft document should be generated only if Free fee application
            4. Draft document should be generated only after payment is made for urgent application and without notice
            */
            if ((isApplicationUrgentAndFreeFee(caseData)
                || isGANonUrgentWithOutNoticeFeePaid(caseData)
                || isApplicationUrgentAndFeePaid(caseData)
                || isRespondentsResponseSatisfied(caseData, caseDataBuilder.build()))
                && isNull(caseData.getJudicialDecision())) {

                gaDraftDocument = gaDraftGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

                List<Element<CaseDocument>> draftApplicationList = newArrayList();
                draftApplicationList.addAll(wrapElements(gaDraftDocument));

                assignCategoryId.assignCategoryIdToCollection(draftApplicationList,
                                                              document -> document.getValue().getDocumentLink(),
                                                              AssignCategoryId.APPLICATIONS);
                caseDataBuilder.gaDraftDocument(draftApplicationList);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
