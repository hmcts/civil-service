package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateClaimFormForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM_SPEC);
    private static final String TASK_ID = "GenerateClaimFormForSpec";
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CivilStitchService civilStitchService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimFormForSpec);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimFormForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating claim form for spec claim for caseId {}", caseId);
        if (featureToggleService.isLipVLipEnabled() && caseData.isApplicantNotRepresented()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }

        LocalDate issueDate = time.now().toLocalDate();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate)
            .respondent1ResponseDeadline(
                deadlinesCalculator.plus28DaysAt4pmDeadline(LocalDateTime.now()))
            // .respondent1Represented(YES)
            .claimDismissedDate(null);
        CaseDocument sealedClaim = sealedClaimFormGeneratorForSpec.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        String categoryId = DocCategory.CLAIMANT1_DETAILS_OF_CLAIM.getValue();
        assignCategoryId.assignCategoryIdToCaseDocument(sealedClaim, categoryId);
        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, sealedClaim);
        if (caseData.getSpecClaimDetailsDocumentFiles() != null
            && caseData.getSpecClaimTemplateDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimDetailsDocumentFiles(), PARTICULARS_OF_CLAIM.getValue());
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimTemplateDocumentFiles(), PARTICULARS_OF_CLAIM.getValue());
            ServedDocumentFiles.builder().particularsOfClaimDocument(wrapElements(
                    caseData.getSpecClaimDetailsDocumentFiles()))
                .timelineEventUpload(wrapElements(caseData.getSpecClaimTemplateDocumentFiles()))
                .build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().particularsOfClaimDocument(
                    wrapElements(caseData.getSpecClaimDetailsDocumentFiles()))
                               .timelineEventUpload(wrapElements(caseData.getSpecClaimTemplateDocumentFiles()))
                               .build());
        } else if (caseData.getSpecClaimTemplateDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimTemplateDocumentFiles(), PARTICULARS_OF_CLAIM.getValue());
            ServedDocumentFiles.builder().timelineEventUpload(wrapElements(
                caseData.getSpecClaimTemplateDocumentFiles())).build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().timelineEventUpload(
                wrapElements(caseData.getSpecClaimTemplateDocumentFiles())).build());
        } else if (caseData.getSpecClaimDetailsDocumentFiles() != null) {
            assignCategoryId.assignCategoryIdToDocument(caseData.getSpecClaimDetailsDocumentFiles(), PARTICULARS_OF_CLAIM.getValue());
            ServedDocumentFiles.builder().particularsOfClaimDocument(wrapElements(
                caseData.getSpecClaimDetailsDocumentFiles())).build();
            caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().particularsOfClaimDocument(
                wrapElements(caseData.getSpecClaimDetailsDocumentFiles())).build());
        }

        if (documentMetaDataList.size() > 1) {
            log.info("Calling civil stitch service from spec claim form generation for caseId {}", caseId);
            String auth = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            CaseDocument stitchedDocument =
                civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                                generateDocumentName("sealed_claim_form_%s.pdf",
                                                                                     caseData.getLegacyCaseReference()),
                                                                caseId,
                                                                DocumentType.SEALED_CLAIM,
                                                                auth);
            stitchedDocument.setDocumentName("Stitched document");
            log.info("Civil stitch service spec response {} for caseId {}", stitchedDocument, caseId);
            assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, categoryId);
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(stitchedDocument));
        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));
        }

        // these documents are added servedDocumentFiles, if we do not remove/null the original,
        // case file view will show duplicate documents
        caseDataBuilder.specClaimTemplateDocumentFiles(null);
        caseDataBuilder.specClaimDetailsDocumentFiles(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument) {
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
