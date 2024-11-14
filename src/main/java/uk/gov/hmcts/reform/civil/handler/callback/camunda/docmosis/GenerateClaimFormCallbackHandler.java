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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateClaimFormCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM);
    private static final String TASK_ID = "GenerateClaimForm";

    private final BundleCreationService bundleCreationService;
    private final LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    private final SealedClaimFormGenerator sealedClaimFormGenerator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final AssignCategoryId assignCategoryId;

    @Value("${stitching.enabled}")
    public boolean stitchEnabled;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate issueDate = time.now().toLocalDate();
        Long caseId = caseData.getCcdCaseReference();
        log.info("Handling un-spec claim form generation for caseId {}", caseId);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate);

        CaseDocument sealedClaim = sealedClaimFormGenerator.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        assignCategoryId.assignCategoryIdToCaseDocument(sealedClaim, "detailsOfClaim");

        log.info("Is stitch enabled {} caseId {}", stitchEnabled, caseId);
        if (stitchEnabled
            && (YesOrNo.NO.equals(caseData.getRespondent1Represented())
            || YesOrNo.NO.equals(caseData.getRespondent2Represented()))) {

            CaseDocument lipForm = litigantInPersonFormGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            List<DocumentMetaData> documents = Arrays.asList(
                new DocumentMetaData(
                    sealedClaim.getDocumentLink(),
                    "Sealed Claim Form",
                    LocalDate.now().toString()
                ),
                new DocumentMetaData(
                    lipForm.getDocumentLink(),
                    "Litigant in person claim form",
                    LocalDate.now().toString()
                )
            );

            log.info("Calling async stitch api for caseId {}", caseId);
            BundleCreateResponse bundleCreateResponse = bundleCreationService.createBundle(
                caseId,
                documents,
                sealedClaim.getDocumentName()
            );
            log.info("Received async stitch api response with document task id {} for caseId {}", bundleCreateResponse.getDocumentTaskId(),  caseId);
            Bundle bundle = bundleCreateResponse.getData().getCaseBundles().get(0);
            CaseDocument caseDocument = CaseDocument.builder()
                .documentLink(Optional.ofNullable(bundle.getValue().getStitchedDocument()).orElse(Document.builder().build()))
                .documentName(bundle.getValue().getFileName())
                .createdDatetime(bundle.getValue().getCreatedOn())
                .build();
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(caseDocument));
        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
