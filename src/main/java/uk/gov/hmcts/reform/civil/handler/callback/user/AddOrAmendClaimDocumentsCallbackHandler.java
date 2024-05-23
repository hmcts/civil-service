package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;

@Service
@RequiredArgsConstructor
public class AddOrAmendClaimDocumentsCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ADD_OR_AMEND_CLAIM_DOCUMENTS);
    private final ExitSurveyContentService exitSurveyContentService;
    private final DocumentDownloadService service;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaimAddOrAmendDocuments,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# Documents uploaded successfully%n## Claim number: %s",
                callbackParams.getCaseData().getLegacyCaseReference()
            ))
            .confirmationBody(exitSurveyContentService.applicantSurvey())
            .build();
    }

    private CallbackResponse validateParticularsOfClaimAddOrAmendDocuments(CallbackParams callbackParams) {

        List<String> errorsAddOrAmendDocuments = getServedDocumentFiles(callbackParams).getErrorsAddOrAmendDocuments();

        if (!errorsAddOrAmendDocuments.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(getServedDocumentFiles(callbackParams).getErrorsAddOrAmendDocuments())
                .build();
        }
        Long caseId = callbackParams.getCaseData().getCcdCaseReference();
        ServedDocumentFiles servedDocumentFiles = getServedDocumentFiles(callbackParams);
        List<String> errors = new ArrayList<>();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        List<Element<Document>> particularsOfClaimDocument = servedDocumentFiles.getParticularsOfClaimDocument();
        if (particularsOfClaimDocument != null) {
            particularsOfClaimDocument.forEach(o ->
                                                   service.validateEncryptionOnUploadedDocument(
                                                       o.getValue(),
                                                       authToken,
                                                       caseId,
                                                       errors
                                                   ));
        }

        List<Element<DocumentWithRegex>> medicalReport = servedDocumentFiles.getMedicalReport();
        if (medicalReport != null) {
            medicalReport.forEach(o ->
                                      service.validateEncryptionOnUploadedDocument(
                                          o.getValue().getDocument(),
                                          authToken,
                                          caseId,
                                          errors
                                      ));
        }

        List<Element<DocumentWithRegex>> scheduleOfLoss = servedDocumentFiles.getScheduleOfLoss();
        if (scheduleOfLoss != null) {
            scheduleOfLoss.forEach(o ->
                                       service.validateEncryptionOnUploadedDocument(
                                           o.getValue().getDocument(),
                                           authToken,
                                           caseId,
                                           errors
                                       ));
        }

        List<Element<DocumentWithRegex>> certificateOfSuitability = servedDocumentFiles.getCertificateOfSuitability();
        if (certificateOfSuitability != null) {
            certificateOfSuitability.forEach(o ->
                                                 service.validateEncryptionOnUploadedDocument(
                                                     o.getValue().getDocument(),
                                                     authToken,
                                                     caseId,
                                                     errors
                                                 ));
        }

        List<Element<DocumentWithRegex>> other = servedDocumentFiles.getOther();
        if (other != null) {
            other.forEach(o ->
                              service.validateEncryptionOnUploadedDocument(
                                  o.getValue().getDocument(),
                                  authToken,
                                  caseId,
                                  errors
                              ));
        }

        List<Element<Document>> timelineEventUpload = servedDocumentFiles.getTimelineEventUpload();
        if (timelineEventUpload != null) {
            timelineEventUpload.forEach(o ->
                                            service.validateEncryptionOnUploadedDocument(
                                                o.getValue(),
                                                authToken,
                                                caseId,
                                                errors
                                            ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
