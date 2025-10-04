package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.notification.handlers.resetpin.ResetPinDefendantLipNotifier;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_PIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPinCUICallbackHandler extends CallbackHandler {

    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final ResetPinDefendantLipNotifier resetPinDefendantLipNotifier;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_DEFENDANT_LIP_EMAIL_MISSING = "The defendant email address is missing." +
        " Please update the defendant details using the manage contact information event and then perform reset pin event.";

    private static final String ERROR_MESSAGE_ACCESS_CODE = "Re set PIN is not required, please ask user to access their claim using ";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefendantInfo,
            callbackKey(ABOUT_TO_SUBMIT), this::resetPinExpiryDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(RESET_PIN);
    }

    private CallbackResponse validateDefendantInfo(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        var caseData = callbackParams.getCaseData();

        String lipAccessCode = Optional.ofNullable(caseData.getRespondent1PinToPostLRspec())
            .map(DefendantPinToPostLRspec::getAccessCode)
            .orElse(null);

        if (caseData.getRespondent1PartyEmail() == null) {
            errors.add(ERROR_MESSAGE_DEFENDANT_LIP_EMAIL_MISSING);
        }

        if (StringUtils.isEmpty(lipAccessCode)) {
            errors.add(ERROR_MESSAGE_ACCESS_CODE + pipInPostConfiguration.getRespondToClaimUrl());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse resetPinExpiryDate(CallbackParams callbackParams) {
        log.info("Resetting pin expiry date for case id: {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData updatedCase = callbackParams.getCaseData().toBuilder()
            .respondent1PinToPostLRspec(defendantPinToPostLRspecService
                .resetPinExpiryDate(callbackParams
                    .getCaseData()
                    .getRespondent1PinToPostLRspec()))

            .build();
        List<String> errors = resetPinDefendantLipNotifier.notifyParties(updatedCase);

        if (!errors.isEmpty()) {
            log.error("Error sending notification for case id {} : {}", updatedCase.getCcdCaseReference(), errors);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.toMap(objectMapper))
            .build();
    }
}
