package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.RequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateClaimFormForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM_RECONSIDERATION);
    private static final String TASK_ID = "GenerateClaimFormForRecon";

    private static final String BUNDLE_NAME = "Claim Form for Judge Reconsideration";
    private final RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final FeatureToggleService toggleService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    @Value("${stitching.enabled}")
    private boolean stitchEnabled;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimFormForReconsideration);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimFormForReconsideration(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();


        LocalDate issueDate = time.now().toLocalDate();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate);
        CaseDocument sealedClaim = requestReconsiderationGeneratorService.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        assignCategoryId.assignCategoryIdToCaseDocument(sealedClaim, "detailsOfClaim");
        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, sealedClaim,
                                                                                 caseDataBuilder, callbackParams);
        if (caseData.getSpecClaimDetailsDocumentFiles() != null
            && caseData.getSpecClaimTemplateDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimDetailsDocumentFiles(), "detailsOfClaim");
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimTemplateDocumentFiles(), "detailsOfClaim");
            ServedDocumentFiles.builder().particularsOfClaimDocument(wrapElements(
                    caseData.getSpecClaimDetailsDocumentFiles()))
                .timelineEventUpload(wrapElements(caseData.getSpecClaimTemplateDocumentFiles()))
                .build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().particularsOfClaimDocument(
                    wrapElements(caseData.getSpecClaimDetailsDocumentFiles()))
                               .timelineEventUpload(wrapElements(caseData.getSpecClaimTemplateDocumentFiles()))
                               .build());
        } else if (caseData.getSpecClaimTemplateDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimTemplateDocumentFiles(), "detailsOfClaim");
            ServedDocumentFiles.builder().timelineEventUpload(wrapElements(
                caseData.getSpecClaimTemplateDocumentFiles())).build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().timelineEventUpload(
                wrapElements(caseData.getSpecClaimTemplateDocumentFiles())).build());
        } else if (caseData.getSpecClaimDetailsDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimDetailsDocumentFiles(), "detailsOfClaim");
            ServedDocumentFiles.builder().particularsOfClaimDocument(wrapElements(
                caseData.getSpecClaimDetailsDocumentFiles())).build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().particularsOfClaimDocument(
                wrapElements(caseData.getSpecClaimDetailsDocumentFiles())).build());
        }

        if (documentMetaDataList.size() > 1) {
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                sealedClaim.getDocumentName(),
                sealedClaim.getDocumentName(),
                caseData
            );
            assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, "detailsOfClaim");
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(stitchedDocument));
        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));
        }

        // these documents are added servedDocumentFiles, if we do not remove/null the original,
        // case file view will show duplicate documents
        if (featureToggleService.isCaseFileViewEnabled()) {
            caseDataBuilder.specClaimTemplateDocumentFiles(null);
            caseDataBuilder.specClaimDetailsDocumentFiles(null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument,
                                  CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CallbackParams callbackParams) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(),
                                                      "Sealed Claim form",
                                                      LocalDate.now().toString()));

        if (caseData.getSpecClaimTemplateDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimTemplateDocumentFiles(),
                "Claim timeline",
                LocalDate.now().toString()
            ));
        }
        if (caseData.getSpecClaimDetailsDocumentFiles() != null) {
            documentMetaDataList.add(new DocumentMetaData(
                caseData.getSpecClaimDetailsDocumentFiles(),
                "Supported docs",
                LocalDate.now().toString()
            ));
        }

        return documentMetaDataList;
    }
}
