package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_ADDL_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadAdditionalDocumentsCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String CONFIRMATION_MESSAGE = "### File has been uploaded successfully.";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPLOAD_ADDL_DOCUMENTS);
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    private final GaForLipService gaForLipService;

    private final IdamClient idamClient;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::submitDocuments,
                      callbackKey(SUBMITTED), this::submittedConfirmation
        );
    }

    private CallbackResponse submitDocuments(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        String userId = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString()).getUid();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        String role = DocUploadUtils.getUserRole(caseData, userId);
        DocUploadUtils.addUploadDocumentByTypeToAddl(caseData, caseDataBuilder,
                                                     caseData.getUploadDocument(), role, true);

        caseDataBuilder.uploadDocument(null);
        caseDataBuilder.businessProcess(BusinessProcess.readyGa(UPLOAD_ADDL_DOCUMENTS)).build();
        GeneralApplicationCaseData updatedCaseData = caseDataBuilder.build();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        // Generate Dashboard Notification for Lip Party
        if (gaForLipService.isGaForLip(caseData)) {
            docUploadDashboardNotificationService.createDashboardNotification(caseData, role, authToken, true);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submittedConfirmation(CallbackParams callbackParams) {
        String body = "<br/> <br/>";
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_MESSAGE)
            .confirmationBody(body)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
