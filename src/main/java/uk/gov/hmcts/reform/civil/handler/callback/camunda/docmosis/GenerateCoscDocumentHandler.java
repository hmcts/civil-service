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
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.cosc.CertificateOfDebtGenerator;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

import static uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus.PROCESSED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class GenerateCoscDocumentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_COSC_DOCUMENT
    );
    private static final String TASK_ID = "GenerateCoscDocument";

    private final CertificateOfDebtGenerator coscDocumentGenerartor;
    private final ObjectMapper objectMapper;
    private final CivilStitchService civilStitchService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateCoscDocument);
    }

    private CallbackResponse generateCoscDocument(CallbackParams callbackParams) {
        CaseData caseDataInfo = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseDataInfo.toBuilder();
        buildCoscDocument(callbackParams, caseDataBuilder);
        caseDataBuilder.coSCApplicationStatus(PROCESSED);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildCoscDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseData caseData = callbackParams.getCaseData();
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseDocument englishDocument = coscDocumentGenerartor.generateDoc(
            callbackParams.getCaseData(),
            authorisation,
            DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT
        );
        CaseDocument caseDocument = englishDocument;

        if (caseData.isRespondentResponseBilingual()) {
            CaseDocument welshDocument = coscDocumentGenerartor.generateDoc(
                callbackParams.getCaseData(),
                authorisation,
                DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT_WELSH
            );
            List<DocumentMetaData> documentMetaDataList = appendDocToWelshDocument(englishDocument, welshDocument);
            Long caseId = caseData.getCcdCaseReference();
            caseDocument = civilStitchService.generateStitchedCaseDocument(
                documentMetaDataList,
                welshDocument.getDocumentName(),
                caseId,
                DocumentType.CERTIFICATE_OF_DEBT_PAYMENT,
                authorisation
            );
        }

        caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(caseDocument));
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private List<DocumentMetaData> appendDocToWelshDocument(CaseDocument englishDoc, CaseDocument welshDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(
            englishDoc.getDocumentLink(),
            "COSC English Document",
            LocalDate.now().toString()
        ));

        documentMetaDataList.add(new DocumentMetaData(
            welshDocument.getDocumentLink(),
            "COSC Welsh Doc to attach",
            LocalDate.now().toString()
        ));

        return documentMetaDataList;
    }
}
