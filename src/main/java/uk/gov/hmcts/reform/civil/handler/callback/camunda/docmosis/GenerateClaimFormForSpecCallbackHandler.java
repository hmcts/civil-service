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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateClaimFormForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM_SPEC);
    private static final String TASK_ID = "GenerateClaimFormForSpec";

    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final FeatureToggleService toggleService;

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
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse generateClaimFormForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate issueDate = time.now().toLocalDate();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate)
            .respondent1ResponseDeadline(
                deadlinesCalculator.plus14DaysAt4pmDeadline(LocalDateTime.now()))
            // .respondent1Represented(YES)
            .claimDismissedDate(null);
        CaseDocument sealedClaim = sealedClaimFormGeneratorForSpec.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, sealedClaim);
        if (documentMetaDataList.size() > 1) {
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                sealedClaim.getDocumentName(),
                sealedClaim.getDocumentName(),
                caseData
            );
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(stitchedDocument));
        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        DocumentMetaData documentMetaData = new DocumentMetaData(
            caseData.getSpecClaimTemplateDocumentFiles(),
            "doc1",
            "doc1"
        );

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
