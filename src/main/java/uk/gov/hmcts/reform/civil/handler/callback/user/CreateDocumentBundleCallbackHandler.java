package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.stitching.BundlingService;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_BUNDLE;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class CreateDocumentBundleCallbackHandler extends CallbackHandler{

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_BUNDLE);
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final ObjectMapper objectMapper;
    private final BundlingService bundlingService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::createServiceRequest)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::createBundle)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    public CallbackResponse createServiceRequest(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        moveExistingCaseBundlesToHistoricalBundles(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    public CallbackResponse createBundle(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("*** Creating Bundle for the case reference : {}", caseData.getCcdCaseReference());
        CaseDocument sealedClaim = sealedClaimFormGeneratorForSpec.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, sealedClaim,
                                                                                 caseDataBuilder, callbackParams);
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

    private void moveExistingCaseBundlesToHistoricalBundles(CaseData caseData) {
        List<Bundle> historicalBundles = new ArrayList<>();
        BundlingInformation existingBundleInformation = caseData.getBundleInformation();
        if (nonNull(existingBundleInformation)) {
            if (nonNull(existingBundleInformation.getHistoricalBundles())) {
                historicalBundles.addAll(existingBundleInformation.getHistoricalBundles());
            }
            if (nonNull(existingBundleInformation.getCaseBundles())) {
                historicalBundles.addAll(existingBundleInformation.getCaseBundles());
            }
            existingBundleInformation.setHistoricalBundles(historicalBundles);
            existingBundleInformation.setCaseBundles(null);
        } else {
            caseData.setBundleInformation(BundlingInformation.builder().build());
        }
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

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
