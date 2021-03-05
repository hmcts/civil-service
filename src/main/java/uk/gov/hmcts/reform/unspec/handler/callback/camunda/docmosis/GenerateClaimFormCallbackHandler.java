package uk.gov.hmcts.reform.unspec.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.IssueDateCalculator;
import uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.GENERATE_CLAIM_FORM;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class GenerateClaimFormCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM);

    private final SealedClaimFormGenerator sealedClaimFormGenerator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final IssueDateCalculator issueDateCalculator;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        LocalDate claimIssuedDate = calculateIssueDate();

        CaseData caseData = callbackParams.getCaseData();

        //TODO: added deadline as workaround until story is played to add new date logic. CMC-596
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .claimIssuedDate(claimIssuedDate)
            .respondentSolicitor1ResponseDeadline(calculateResponseDeadline(claimIssuedDate));

        CaseDocument sealedClaim = sealedClaimFormGenerator.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealedClaim));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataBuilder.build()))
            .build();
    }

    private LocalDate calculateIssueDate() {
        return issueDateCalculator.calculateIssueDay(LocalDateTime.now());
    }

    private LocalDateTime calculateResponseDeadline(LocalDate issueDate) {
        return deadlinesCalculator.calculateResponseDeadline(issueDate);
    }
}
