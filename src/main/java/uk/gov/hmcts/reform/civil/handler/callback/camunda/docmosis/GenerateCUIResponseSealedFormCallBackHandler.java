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
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_RESPONSE_CUI_SEALED;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenerateCUIResponseSealedFormCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_RESPONSE_CUI_SEALED);

    private final ObjectMapper objectMapper;
    private final SealedClaimLipResponseFormGenerator formGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final AssignCategoryId assignCategoryId;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final FeatureToggleService featureToggleService;
    @Value("${stitching.enabled:true}")
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
        log.info("-------- stitchEnabled ----------- {}", stitchEnabled);
        log.info("-------- featureToggleService.isLipVLipEnabled() ----------- {}", featureToggleService.isLipVLipEnabled());
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument sealedForm = formGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        assignCategoryId.assignCategoryIdToCaseDocument(sealedForm, DocCategory.DEF1_DEFENSE_DQ.getValue());

        if (stitchEnabled && featureToggleService.isLipVLipEnabled()) {
            log.info("-------- stitched enabled and it's lip vs lip -----------");
            List<DocumentMetaData> documentMetaDataList = fetchDocumentsToStitch(caseData, sealedForm);
            log.info("-------- Document stitched ----------- {}", documentMetaDataList.size());
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                    documentMetaDataList,
                    callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                    sealedForm.getDocumentName(),
                    sealedForm.getDocumentName(),
                    caseData
            );
            log.info("-------- final stitched doc-----------");
            assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, DocCategory.DEF1_DEFENSE_DQ.getValue());
            caseDataBuilder.respondent1ClaimResponseDocumentSpec(stitchedDocument)
                    .systemGeneratedCaseDocuments(ElementUtils.wrapElements(stitchedDocument));
            log.info("-------- final stitched doc------end-----");

        } else {
            log.info("-------- else part-----");
            caseDataBuilder.respondent1ClaimResponseDocumentSpec(sealedForm)
                    .systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                            sealedForm,
                            caseData
                    ));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<DocumentMetaData> fetchDocumentsToStitch(CaseData caseData, CaseDocument sealedForm) {
        List<DocumentMetaData> documents = new ArrayList<>();

        documents.add(new DocumentMetaData(
                sealedForm.getDocumentLink(),
                "Sealed Claim form",
                LocalDate.now().toString()
        ));
        ElementUtils.unwrapElements(caseData.getSystemGeneratedCaseDocuments()).stream()
                .filter(cd -> DocumentType.DIRECTIONS_QUESTIONNAIRE.equals(cd.getDocumentType()))
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
