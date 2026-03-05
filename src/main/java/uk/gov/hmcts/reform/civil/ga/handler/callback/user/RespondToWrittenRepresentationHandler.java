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

        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String userId = idamClient.getUserInfo(authToken).getUid();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        String role = DocUploadUtils.getUserRole(caseData, userId);
        List<Element<Document>> responseDocumentToBeAdded = caseData.getGeneralAppWrittenRepUpload();
        if (Objects.isNull(responseDocumentToBeAdded)) {
            responseDocumentToBeAdded = new ArrayList<>();
        }
        boolean translationRequired = false;
        PreTranslationGaDocumentType waDocumentType = null;
        if (Objects.nonNull(caseData.getGeneralAppWrittenRepText())) {
            if (featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual()) {
                translationRequired = true;
                waDocumentType = WRITTEN_REPS_RESPONSE_DOC;
            }
            CaseDocument caseDocument = respondToWrittenRepresentation.generate(caseData,
                                                                                callbackParams.getParams().get(
                                                                                    BEARER_TOKEN).toString(), role
            );
            responseDocumentToBeAdded.add(ElementUtils.element(caseDocument.getDocumentLink()));
        }
        caseDataBuilder.preTranslationGaDocumentType(waDocumentType);
        if (!translationRequired) {
            DocUploadUtils.addDocumentToAddl(
                caseData,
                caseDataBuilder,
                responseDocumentToBeAdded,
                role,
                CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION,
                false
            );
        } else {
            DocUploadUtils.addDocumentToPreTranslation(
                caseData,
                caseDataBuilder,
                responseDocumentToBeAdded,
                role,
                CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION
            );
        }
        if (featureToggleService.isGaForWelshEnabled()) {
            DocUploadUtils.setRespondedValues(caseDataBuilder, role);
        }
        caseDataBuilder.generalAppWrittenRepUpload(Collections.emptyList());
        caseDataBuilder.generalAppWrittenRepText(null);
        caseDataBuilder.businessProcess(BusinessProcess.readyGa(RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION)).build();
        GeneralApplicationCaseData updatedCaseData = caseDataBuilder.build();

        // Generate Dashboard Notification for Lip Party
        if (gaForLipService.isGaForLip(caseData)) {
            log.info("General dashboard notification for Lip party for caseId: {}", caseData.getCcdCaseReference());
            boolean sendDashboardNotificationToOtherParty =
                !(translationRequired || DocUploadUtils.uploadedDocumentAwaitingTranslation(caseData, role, "Written representation"));
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
