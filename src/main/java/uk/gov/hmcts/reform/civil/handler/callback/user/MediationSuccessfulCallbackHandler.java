package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.model.citizenui.DocumentTypeMapper;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class MediationSuccessfulCallbackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MEDIATION_SUCCESSFUL);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::submitSuccessfulMediation,
            callbackKey(CallbackType.SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitSuccessfulMediation(CallbackParams callbackParams) {
        List<Element<ManageDocument>> updatedManageDocumentsList =
            updateManageDocumentsListWithMediationAgreementDocument(callbackParams);

        CaseData caseDataUpdated = callbackParams.getCaseData();
        caseDataUpdated.setBusinessProcess(BusinessProcess.ready(MEDIATION_SUCCESSFUL));
        caseDataUpdated.setManageDocuments(updatedManageDocumentsList);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .state(CASE_STAYED.name())
            .build();
    }

    private List<Element<ManageDocument>> updateManageDocumentsListWithMediationAgreementDocument(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<ManageDocument>> manageDocumentsList = caseData.getManageDocumentsList();
        Optional<MediationAgreementDocument> mediationAgreementDocument = Optional.ofNullable(caseData.getMediation())
            .map(Mediation::getMediationSuccessful)
            .map(MediationSuccessful::getMediationAgreement);

        mediationAgreementDocument.ifPresent(document -> {
            ManageDocument manageDocument = new ManageDocument();
            manageDocument.setDocumentLink(document.getDocument());
            manageDocument.setDocumentName(document.getName());
            manageDocument.setDocumentType(DocumentTypeMapper.mapDocumentTypeToManageDocumentType(document.getDocumentType()));
            manageDocumentsList.add(element(manageDocument));
        });
        return manageDocumentsList;
    }
}
