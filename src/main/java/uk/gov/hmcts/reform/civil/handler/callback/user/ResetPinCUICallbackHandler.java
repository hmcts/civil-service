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
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_PIN;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPinCUICallbackHandler extends CallbackHandler implements NotificationData {

    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final ObjectMapper objectMapper;
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::resetPinExpiryDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(RESET_PIN);
    }

    private CallbackResponse resetPinExpiryDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedCase = caseData.toBuilder()
            .respondent1PinToPostLRspec(defendantPinToPostLRspecService
                                            .resetPinExpiryDate(caseData.getRespondent1PinToPostLRspec()))
            .build();

        if (caseData.getRespondent1() != null
            && caseData.getRespondent1().getPartyEmail() != null) {
            log.info("Start PIP email notification to defendantLip for case id {}", caseData.getLegacyCaseReference());
            generateDefendantLipPIPEmail(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.toMap(objectMapper))
            .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            RESPOND_URL, pipInPostConfiguration.getRespondToClaimUrl(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            PIN, caseData.getRespondent1PinToPostLRspec().getAccessCode(),
            RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline()
                                                   .toLocalDate(), DATE),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl()
        );
    }

    private void generateDefendantLipPIPEmail(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondent1().getPartyEmail(),
            notificationsProperties.getRespondentDefendantResponseForSpec(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }
}
