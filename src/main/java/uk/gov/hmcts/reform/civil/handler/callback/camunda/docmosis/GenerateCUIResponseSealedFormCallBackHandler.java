package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_RESPONSE_CUI_SEALED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateCUIResponseSealedFormCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_RESPONSE_CUI_SEALED);

    private final ObjectMapper objectMapper;
    private final SealedClaimLipResponseFormGenerator formGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final AssignCategoryId assignCategoryId;
    private final CivilStitchService civilStitchService;
    private final FeatureToggleService featureToggleService;
    @Value("${stitching.enabled}")
    private boolean stitchEnabled;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::prepareSealedForm
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareSealedForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Long caseId = caseData.getCcdCaseReference();

        log.info("Generating response cui sealed form for case {}", caseId);
        String authToken = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CaseDocument sealedForm = formGenerator.generate(caseData, authToken);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (stitchEnabled && caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            List<DocumentMetaData> documentMetaDataList = fetchDocumentsToStitch(caseData, sealedForm);
            log.info("no of document sending for stitch {} for caseId {}", documentMetaDataList.size(), caseId);
            if (documentMetaDataList.size() > 1) {
                log.info("Calling civil stitch service from response cui sealed form for caseId {}", caseId);
                CaseDocument stitchedDocument =
                    civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                                    sealedForm.getDocumentName(),
                                                                    caseId,
                                                                    DocumentType.DEFENDANT_DEFENCE,
                                                                    authToken);
                log.info("Civil stitch service for response cui sealed form {} for caseId {}", stitchedDocument, caseId);
                addToSystemGeneratedDocuments(caseDataBuilder, stitchedDocument, caseData);
            }
        }
        addToSystemGeneratedDocuments(caseDataBuilder, sealedForm, caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void addToSystemGeneratedDocuments(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseDocument document, CaseData caseData) {
        if (featureToggleService.isGaForWelshEnabled() && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual()
            || caseData.isLipDefendantSpecifiedBilingualDocuments())) {
            caseDataBuilder.respondent1ClaimResponseDocumentSpec(document)
                .preTranslationDocuments(List.of(ElementUtils.element(document)));
        } else {
            caseDataBuilder.respondent1ClaimResponseDocumentSpec(document)
                .systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                    document,
                    caseData
                ));
            assignCategoryId.assignCategoryIdToCaseDocument(document, DocCategory.DEF1_DEFENSE_DQ.getValue());
        }
    }

    private List<DocumentMetaData> fetchDocumentsToStitch(CaseData caseData, CaseDocument sealedForm) {
        List<DocumentMetaData> documents = new ArrayList<>();

        documents.add(new DocumentMetaData(
                sealedForm.getDocumentLink(),
                "Sealed Claim form",
                LocalDate.now().toString()
        ));
        ElementUtils.unwrapElements(caseData.getSystemGeneratedCaseDocuments()).stream()
                .filter(cd -> DIRECTIONS_QUESTIONNAIRE.equals(cd.getDocumentType()))
                .map(cd ->
                        new DocumentMetaData(
                                cd.getDocumentLink(),
                                "Directions Questionnaire",
                                LocalDate.now().toString()
                        )
                ).forEach(documents::add);

        return documents;
    }
}
