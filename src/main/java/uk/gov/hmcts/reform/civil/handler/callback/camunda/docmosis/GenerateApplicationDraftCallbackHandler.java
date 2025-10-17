package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.applicationdraft.GeneralApplicationDraftGenerator;
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
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;

@Service
@RequiredArgsConstructor
public class GenerateApplicationDraftCallbackHandler extends CallbackHandler {

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
    private boolean isApplicationUrgentAndFreeFee(GeneralApplicationCaseData gaCaseData) {
        return generalAppFeesService.isFreeApplication(gaCaseData)
                && gaCaseData.getGeneralAppUrgencyRequirement() != null
                && YES.equals(gaCaseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency());
    }

    // Generate Draft Document if it's Urgent application and after fee is paid
    // Initiate General application after payment camunda task started
    private boolean isApplicationUrgentAndFeePaid(GeneralApplicationCaseData gaCaseData) {
        return isFeePaid(gaCaseData) && gaCaseData.getGeneralAppUrgencyRequirement() != null
                && YES.equals(gaCaseData.getGeneralAppUrgencyRequirement()
                .getGeneralAppUrgency());

    }

    // Generate Draft Document if it's non-urgent, without notice and after fee is paid
    private boolean isGANonUrgentWithOutNoticeFeePaid(GeneralApplicationCaseData gaCaseData) {
        return isFeePaid(gaCaseData) && gaCaseData.getGeneralAppInformOtherParty() != null
                && NO.equals(gaCaseData.getGeneralAppInformOtherParty().getIsWithNotice());
    }

    private boolean isFeePaid(GeneralApplicationCaseData gaCaseData) {
        return gaCaseData.getGeneralAppPBADetails() != null
                && gaCaseData.getGeneralAppPBADetails().getFee() != null
                && !gaCaseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")
                && gaCaseData.getGeneralAppPBADetails().getPaymentDetails() != null
                && gaCaseData.getGeneralAppPBADetails().getPaymentDetails().getStatus().equals(PaymentStatus.SUCCESS);
    }

    private CallbackResponse createPDFdocument(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        Objects.requireNonNull(gaCaseData, "gaCaseData must be present on CallbackParams");

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseDocument gaDraftDocument;
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (gaForLipService.isGaForLip(gaCaseData)) {
            if (generalAppFeesService.isFreeApplication(gaCaseData) || isFeePaid(gaCaseData)) {

                List<Element<CaseDocument>> draftApplicationList = caseData.getGaDraftDocument();
                if (Objects.isNull(draftApplicationList)) {
                    draftApplicationList = newArrayList();
                }

                GeneralApplicationCaseData snapshotGaData = objectMapper.convertValue(
                    caseDataBuilder.build(), GeneralApplicationCaseData.class);

                gaDraftDocument = gaDraftGenerator.generate(
                        snapshotGaData,
                        authToken
                );

                if (featureToggleService.isGaForWelshEnabled()
                        && (((gaCaseData.getIsGaApplicantLip() == YES
                        && gaCaseData.isApplicantBilingual())
                        || (gaCaseData.isRespondentBilingual() && gaCaseData.getIsGaRespondentOneLip() == YES)))) {
                    List<Element<CaseDocument>> preTranslatedDocuments =
                            Optional.ofNullable(caseData.getPreTranslationGaDocuments())
                                    .orElseGet(ArrayList::new);
                    preTranslatedDocuments.add(element(gaDraftDocument));
                    assignCategoryId.assignCategoryIdToCollection(
                            preTranslatedDocuments,
                            document -> document.getValue().getDocumentLink(),
                            AssignCategoryId.APPLICATIONS
                    );

                    if (gaCaseData.getRespondentsResponses() != null
                            && !gaCaseData.getRespondentsResponses().isEmpty()
                            && caseData.getCcdState().equals(CaseState.AWAITING_RESPONDENT_RESPONSE)) {
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
            if ((isApplicationUrgentAndFreeFee(gaCaseData)
                    || isGANonUrgentWithOutNoticeFeePaid(gaCaseData)
                    || isApplicationUrgentAndFeePaid(gaCaseData)
                    || isRespondentsResponseSatisfied(
                        gaCaseData,
                        objectMapper.convertValue(caseDataBuilder.build(), GeneralApplicationCaseData.class)))
                    && isNull(caseData.getJudicialDecision())) {

                GeneralApplicationCaseData snapshotGaData = objectMapper.convertValue(
                    caseDataBuilder.build(), GeneralApplicationCaseData.class);

                gaDraftDocument = gaDraftGenerator.generate(
                        snapshotGaData,
                        authToken
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
