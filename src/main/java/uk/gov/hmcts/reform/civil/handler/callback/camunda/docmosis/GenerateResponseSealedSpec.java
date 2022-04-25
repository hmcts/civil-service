package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimResponseFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Service
@RequiredArgsConstructor
public class GenerateResponseSealedSpec extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.GENERATE_RESPONSE_SEALED);

    private final ObjectMapper objectMapper;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final SealedClaimResponseFormGeneratorForSpec formGenerator;

    // TODO true if stitching is online
    @Value("${stitching.enabled:true}")
    private boolean stitchEnabled;
    /* TODO add a configuration flag so we are able to locally test without depending on stitching,
            since stitching is not in the default docker images and it's still blocked.
            Default should possibly be true though, but that may require changes on demo env,
            we need to see it on Monday
Check that the above flag works as expected. Since we don't have stitching url in local, value should be false
         */

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::prepareSealedForm
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareSealedForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        CaseDocument sealedForm = generateSealedClaim(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        if (stitchEnabled) {
            List<DocumentMetaData> documentMetaDataList = fetchDocumentsToStitch(caseData, sealedForm);
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                sealedForm.getDocumentName(),
                sealedForm.getDocumentName(),
                caseData
            );
            caseData.getSystemGeneratedCaseDocuments().add(ElementUtils.element(stitchedDocument));
        } else {
            caseData.getSystemGeneratedCaseDocuments().add(ElementUtils.element(sealedForm));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private CaseDocument generateSealedClaim(CaseData caseData, String authorization) {
        return formGenerator.generate(caseData, authorization);
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
        // TODO select documents uploaded during response AND DQ; see as example GenerateClaimFormForSpecCallbackHandler.fetchDocumentsFromCaseData()
        return Collections.emptyList();
    }
}
