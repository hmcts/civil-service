package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.ResponseMethod;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.attachScannedDocs;
import static uk.gov.hmcts.reform.civil.utils.MapperUtil.hasPaperResponse;

@Slf4j
@Service
public class AttachScannedDocsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(attachScannedDocs);
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public AttachScannedDocsCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::setDefendantResponseMethod,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setDefendantResponseMethod(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (hasPaperResponse(caseData)) {
            log.info("Paper response detected for caseId: {}, setting defendant responseMethod to OFFLINE",
                     callbackParams.getCaseData().getCcdCaseReference());

            if (callbackParams.isCivilCaseType()) {
                caseData.setRespondent1ResponseMethod(ResponseMethod.OFFLINE);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseData))
            .build();
    }
}
