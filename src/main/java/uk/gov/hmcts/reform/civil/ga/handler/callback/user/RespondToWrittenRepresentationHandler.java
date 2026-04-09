package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RespondToWrittenRepresentationGenerator;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType.WRITTEN_REPS_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils.APPLICANT;
import static uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils.RESPONDENT_ONE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondToWrittenRepresentationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String WRITTEN_REPRESENTATION = "Written representation";

    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final IdamClient idamClient;
    private final RespondToWrittenRepresentationGenerator respondToWrittenRepresentation;
    private final DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    private final GaForLipService gaForLipService;
    private final FeatureToggleService featureToggleService;

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
                      callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
                      callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        SubmissionContext submissionContext = buildSubmissionContext(callbackParams);
        GeneralApplicationCaseData caseData = submissionContext.caseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        List<Element<Document>> responseDocuments = getResponseDocuments(caseData);
        TranslationState translationState = appendGeneratedWrittenRepresentation(
            caseData,
            responseDocuments,
            submissionContext.authToken(),
            submissionContext.role()
        );

        caseDataBuilder.preTranslationGaDocumentType(translationState.documentType());
        storeResponseDocuments(
            caseData,
            caseDataBuilder,
            responseDocuments,
            submissionContext.role(),
            translationState.translationRequired()
        );
        updateResponseFlags(caseDataBuilder, submissionContext.role());
        GeneralApplicationCaseData updatedCaseData = buildUpdatedCaseData(caseDataBuilder);

        sendLipNotifications(
            caseData,
            submissionContext.role(),
            submissionContext.authToken(),
            translationState.translationRequired()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private SubmissionContext buildSubmissionContext(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(
            callbackParams.getRequest().getCaseDetails()
        );
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String userId = idamClient.getUserInfo(authToken).getUid();
        String role = DocUploadUtils.getUserRole(caseData, userId);
        return new SubmissionContext(caseData, authToken, role);
    }

    private List<Element<Document>> getResponseDocuments(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getGeneralAppWrittenRepUpload())
            ? caseData.getGeneralAppWrittenRepUpload() : new ArrayList<>();
    }

    private TranslationState appendGeneratedWrittenRepresentation(GeneralApplicationCaseData caseData,
                                                                  List<Element<Document>> responseDocuments,
                                                                  String authToken,
                                                                  String role) {
        boolean hasGeneratedWrittenRepresentation = hasGeneratedWrittenRepresentation(caseData);
        boolean translationRequired = false;
        PreTranslationGaDocumentType documentType = null;

        if (hasGeneratedWrittenRepresentation) {
            translationRequired = isTranslationRequired(caseData);
            documentType = translationRequired ? WRITTEN_REPS_RESPONSE_DOC : null;
        }

        if (hasGeneratedWrittenRepresentation) {
            CaseDocument caseDocument = respondToWrittenRepresentation.generate(caseData, authToken, role);
            responseDocuments.add(ElementUtils.element(caseDocument.getDocumentLink()));
        }

        return new TranslationState(translationRequired, documentType);
    }

    private boolean hasGeneratedWrittenRepresentation(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getGeneralAppWrittenRepText());
    }

    private boolean isTranslationRequired(GeneralApplicationCaseData caseData) {
        return featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual();
    }

    private void storeResponseDocuments(GeneralApplicationCaseData caseData,
                                        GeneralApplicationCaseData caseDataBuilder,
                                        List<Element<Document>> responseDocuments,
                                        String role,
                                        boolean translationRequired) {
        if (!translationRequired) {
            DocUploadUtils.addDocumentToAddl(
                caseData,
                caseDataBuilder,
                responseDocuments,
                role,
                CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION,
                false
            );
            return;
        }

        DocUploadUtils.addDocumentToPreTranslation(
            caseData,
            caseDataBuilder,
            responseDocuments,
            role,
            CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
        );
    }

    private void updateResponseFlags(GeneralApplicationCaseData caseDataBuilder, String role) {
        if (featureToggleService.isGaForWelshEnabled()) {
            DocUploadUtils.setRespondedValues(caseDataBuilder, role);
        }
    }

    private GeneralApplicationCaseData buildUpdatedCaseData(GeneralApplicationCaseData caseDataBuilder) {
        caseDataBuilder.generalAppWrittenRepUpload(Collections.emptyList());
        caseDataBuilder.generalAppWrittenRepText(null);
        caseDataBuilder.businessProcess(BusinessProcess.readyGa(RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION)).build();
        return caseDataBuilder.build();
    }

    private void sendLipNotifications(GeneralApplicationCaseData caseData,
                                      String role,
                                      String authToken,
                                      boolean translationRequired) {
        if (!gaForLipService.isGaForLip(caseData)) {
            return;
        }

        log.info("General dashboard notification for Lip party for caseId: {}", caseData.getCcdCaseReference());
        boolean sendDashboardNotificationToOtherParty = shouldSendDashboardNotificationToOtherParty(
            caseData,
            role,
            translationRequired
        );
        if (sendDashboardNotificationToOtherParty) {
            docUploadDashboardNotificationService.createDashboardNotification(caseData, role, authToken, false);
        }
        if (role.equals(APPLICANT) || sendDashboardNotificationToOtherParty) {
            docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", authToken);
        }
        if (role.equals(RESPONDENT_ONE) || sendDashboardNotificationToOtherParty) {
            docUploadDashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);
        }
    }

    private boolean shouldSendDashboardNotificationToOtherParty(GeneralApplicationCaseData caseData,
                                                                String role,
                                                                boolean translationRequired) {
        return !(translationRequired
            || DocUploadUtils.uploadedDocumentAwaitingTranslation(caseData, role, WRITTEN_REPRESENTATION));
    }

    private record TranslationState(boolean translationRequired, PreTranslationGaDocumentType documentType) {
    }

    private record SubmissionContext(GeneralApplicationCaseData caseData, String authToken, String role) {
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
