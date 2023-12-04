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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateClaimFormCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM);
    private static final String TASK_ID = "GenerateClaimForm";
    private static final String BUNDLE_NAME = "Sealed Claim Form with LiP Claim Form";

    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    private final SealedClaimFormGenerator sealedClaimFormGenerator;
    private final ObjectMapper objectMapper;
    private final Time time;
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
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate issueDate = time.now().toLocalDate();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate);

        CaseDocument sealedClaim = sealedClaimFormGenerator.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        assignCategoryId.assignCategoryIdToCaseDocument(sealedClaim, "detailsOfClaim");

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

            CaseDocument sealedClaimFormWithLiPForm =
                civilDocumentStitchingService.bundle(
                    documents,
                    callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                    BUNDLE_NAME,
                    sealedClaim.getDocumentName(),
                    caseData
                );

            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaimFormWithLiPForm));

        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
