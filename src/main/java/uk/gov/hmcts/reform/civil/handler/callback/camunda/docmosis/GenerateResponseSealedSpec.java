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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimResponseFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private final CivilStitchService civilStitchService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

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
        Long caseId = caseData.getCcdCaseReference();
        log.info("Preparing response seal form for caseId {}", caseId);
        CaseDocument sealedForm = formGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(
            sealedForm, "");
        assignCategoryId.assignCategoryIdToCaseDocument(sealedForm, DocCategory.DEF1_DEFENSE_DQ.getValue());
        assignCategoryId.assignCategoryIdToCaseDocument(copy, DocCategory.DQ_DEF1.getValue());
        if (nonNull(caseData.getRespondent2DocumentGeneration()) && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")) {
            assignCategoryId.assignCategoryIdToCaseDocument(sealedForm, DocCategory.DEF2_DEFENSE_DQ.getValue());
            assignCategoryId.assignCategoryIdToCaseDocument(copy, DocCategory.DQ_DEF2.getValue());
        }
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
        if (stitchEnabled) {
            List<DocumentMetaData> documentMetaDataList = fetchDocumentsToStitch(caseData, sealedForm);
            log.info("Calling civil stitch service for generate response sealed form for caseId {}", caseId);
            String auth = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            CaseDocument stitchedDocument =
                civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                                sealedForm.getDocumentName(),
                                                                caseId,
                                                                DocumentType.DEFENDANT_DEFENCE,
                                                                auth);
            log.info("Civil stitch service for generate response sealed form {} for caseId {}", stitchedDocument, caseId);
            assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, DocCategory.DEF1_DEFENSE_DQ.getValue());
            CaseDocument stitchedDocumentCopy = assignCategoryId.copyCaseDocumentWithCategoryId(stitchedDocument, DocCategory.DQ_DEF1.getValue());
            if (nonNull(caseData.getRespondent2DocumentGeneration()) && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")) {
                assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocument, DocCategory.DEF2_DEFENSE_DQ.getValue());
                assignCategoryId.assignCategoryIdToCaseDocument(stitchedDocumentCopy, DocCategory.DQ_DEF2.getValue());
            }
            isLipWelshApplicant(caseData, builder, stitchedDocument, stitchedDocumentCopy);
        } else {
            isLipWelshApplicant(caseData, builder, sealedForm, copy);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private void isLipWelshApplicant(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder,
                                     CaseDocument sealedForm,
                                     CaseDocument copy) {
        if (featureToggleService.isWelshEnabledForMainCase() && caseData.isLipvLROneVOne()
            && caseData.isClaimantBilingual()
            && CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState())) {
            List<Element<CaseDocument>> preTranslationDocs =
                Optional.ofNullable(caseData.getPreTranslationDocuments()).orElseGet(ArrayList::new);
            preTranslationDocs.add(ElementUtils.element(sealedForm));
            builder.preTranslationDocuments(preTranslationDocs);
            builder.bilingualHint(YesOrNo.YES);
            builder.preTranslationDocumentType(PreTranslationDocumentType.DEFENDANT_SEALED_CLAIM_FORM_FOR_LIP_VS_LR);
        } else {
            caseData.getSystemGeneratedCaseDocuments().add(ElementUtils.element(sealedForm));
            if (Objects.nonNull(copy)) {
                caseData.getDuplicateSystemGeneratedCaseDocs().add(ElementUtils.element(copy));
            }
        }
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
                caseData.getSpecResponseTimelineDocumentFiles(),
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
        if (featureToggleService.isWelshEnabledForMainCase() && caseData.isLipvLROneVOne()
            && caseData.isClaimantBilingual() && caseData.getRespondent1OriginalDqDoc() != null
            && CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState())) {
            documents.add(
                new DocumentMetaData(
                    caseData.getRespondent1OriginalDqDoc().getDocumentLink(),
                    "Directions Questionnaire",
                    LocalDate.now().toString()
                )
            );

        } else {
            ElementUtils.unwrapElements(caseData.getSystemGeneratedCaseDocuments()).stream()
                .filter(cd -> DocumentType.DIRECTIONS_QUESTIONNAIRE.equals(cd.getDocumentType()))
                .map(cd ->
                         new DocumentMetaData(
                             cd.getDocumentLink(),
                             "Directions Questionnaire",
                             LocalDate.now().toString()
                         )
                ).forEach(documents::add);
        }
        return documents;
    }
}
