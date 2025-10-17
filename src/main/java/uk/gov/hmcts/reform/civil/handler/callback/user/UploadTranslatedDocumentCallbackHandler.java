package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UploadTranslatedDocumentService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.civil.utils.IdamUserUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_TRANSLATED_DOCUMENT;

@Service
@RequiredArgsConstructor
public class UploadTranslatedDocumentCallbackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final IdamClient idamClient;
    private final UploadTranslatedDocumentService uploadTranslatedDocumentService;
    private final GaCaseDataEnricher gaCaseDataEnricher;

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPLOAD_TRANSLATED_DOCUMENT);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::submitUploadTranslatedDocuments,
                      callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    protected CallbackResponse submitUploadTranslatedDocuments(CallbackParams callbackParams) {
        CaseData caseData = gaCaseDataEnricher.enrich(
            callbackParams.getCaseData(),
            callbackParams.getGaCaseData()
        );
        UserInfo userDetails = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String translator = IdamUserUtils.getIdamUserFullName(userDetails);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        uploadTranslatedDocumentService.updateGADocumentsWithOriginalDocuments(caseDataBuilder);
        caseDataBuilder = uploadTranslatedDocumentService.processTranslatedDocument(caseDataBuilder.build(), translator);
        CaseEvent businessProcessEvent = uploadTranslatedDocumentService.getBusinessProcessEvent(caseData);
        if (businessProcessEvent != null) {
            caseDataBuilder = caseDataBuilder.businessProcess(BusinessProcess.ready(businessProcessEvent));
        }
        caseDataBuilder.preTranslationGaDocumentType(null);
        CaseData updatedCaseData = caseDataBuilder.build();
        uploadTranslatedDocumentService.sendUserUploadNotification(caseData, updatedCaseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
