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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimResponseFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateResponseSealedSpec extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.GENERATE_RESPONSE_SEALED);

    private final ObjectMapper objectMapper;
    private final SealedClaimResponseFormGeneratorForSpec formGenerator;

    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final FeatureToggleService toggleService;
    private final AssignCategoryId assignCategoryId;

    @Value("${stitching.enabled:true}")
    private boolean stitchEnabled;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::prepareSealedForm,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::prepareSealedForm
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareSealedForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        CaseDocument sealedForm = formGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        assignCategoryId.assignCategoryIdToCaseDocument(sealedForm, "defendant1DefenseDirectionsQuestionnaire");
        if (nonNull(caseData.getRespondent2DocumentGeneration()) && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")) {
            assignCategoryId.assignCategoryIdToCaseDocument(sealedForm, "defendant2DefenseDirectionsQuestionnaire");
        }

        if (stitchEnabled) {
            List<DocumentMetaData> documentMetaDataList = fetchDocumentsToStitch(caseData, sealedForm);
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                sealedForm.getDocumentName(),
                sealedForm.getDocumentName(),
                caseData
            );
            assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, "defendant1DefenseDirectionsQuestionnaire");
            if (nonNull(caseData.getRespondent2DocumentGeneration()) && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")) {
                assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, "defendant2DefenseDirectionsQuestionnaire");
            }
            if (V_1.equals(callbackParams.getVersion()) && toggleService.isPinInPostEnabled()) {
                builder.respondent1ClaimResponseDocumentSpec(stitchedDocument);
            }
            caseData.getSystemGeneratedCaseDocuments().add(ElementUtils.element(stitchedDocument));
        } else {
            if (V_1.equals(callbackParams.getVersion()) && toggleService.isPinInPostEnabled()) {
                builder.respondent1ClaimResponseDocumentSpec(sealedForm);
            }
            caseData.getSystemGeneratedCaseDocuments().add(ElementUtils.element(sealedForm));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    /**
     * The sealed claim form should include files uploaded during the response process.
     *
     * @param caseData    the case data
     * @param sealedClaim the sealed claim document
     * @return list of the document metadata for the response DQ (which should be already generated),
     *     and all files uploaded during the response
     */
    private List<DocumentMetaData> fetchDocumentsToStitch(CaseData caseData, CaseDocument sealedClaim) {
        List<DocumentMetaData> documents = new ArrayList<>();

        documents.add(new DocumentMetaData(
            sealedClaim.getDocumentLink(),
            "Sealed Claim form",
            LocalDate.now().toString()
        ));
        if (caseData.getSpecResponseTimelineDocumentFiles() != null) {
            documents.add(new DocumentMetaData(
                caseData.getSpecResponseTimelineDocumentFiles().getFile(),
                "Claim timeline",
                LocalDate.now().toString()
            ));
        }
        if (caseData.getRespondent1SpecDefenceResponseDocument() != null) {
            documents.add(new DocumentMetaData(
                caseData.getRespondent1SpecDefenceResponseDocument().getFile(),
                "Supported docs",
                LocalDate.now().toString()
            ));
        }
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
