package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondToClaimCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE_CUI);

    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData updatedData = getUpdatedCaseData(callbackParams);

        boolean responseLanguageIsBilingual = updatedData.isRespondentResponseBilingual();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder().data(updatedData.toMap(objectMapper));

        if (!responseLanguageIsBilingual) {
            responseBuilder.state(CaseState.AWAITING_APPLICANT_INTENTION.name());
        }

        return responseBuilder.build();
    }

    private CaseData getUpdatedCaseData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument dummyDocument = new CaseDocument(null, null, null, 0, null, null, null);
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        CaseData updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_CUI))
            .respondent1ResponseDate(responseDate)
            .respondent1GeneratedResponseDocument(dummyDocument)
            .respondent1ClaimResponseDocumentSpec(dummyDocument)
            .applicant1ResponseDeadline(deadlinesCalculator.calculateApplicantResponseDeadline(
                responseDate,
                allocatedTrack
            ))
            .build();
        return updatedData;
    }
}
