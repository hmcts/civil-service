package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.pip.PiPLetterGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineRespondentPartyForSpecNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    public static final String TASK_ID_Respondent1 = "CreateClaimContinuingOnlineNotifyRespondent1ForSpec";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";
    private static final String FIRST_CONTACT_PACK_LETTER_TYPE = "first-contact-pack";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final PiPLetterGenerator pipLetterGenerator;
    private final FeatureToggleService featureToggleService;
    private final BulkPrintService bulkPrintService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentForClaimContinuingOnline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_Respondent1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentForClaimContinuingOnline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime claimNotificationDate = time.now();
        final CaseData.CaseDataBuilder caseDataBuilder
            = caseData.toBuilder().claimNotificationDate(claimNotificationDate);

        if (caseData.getRespondent1() != null && caseData.getRespondent1().getPartyEmail() != null) {
            generatePIPEmail(caseData);
        }

        generatePIPLetter(callbackParams);

        String updateCaseState = setClaimState(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(updateCaseState)
            .build();
    }

    private String setClaimState(CaseData caseData) {
        if (!isBilingualForLipvsLip(caseData)) {
            return CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        }
        return caseData.getCcdState().name();
    }

    private boolean isBilingualForLipvsLip(CaseData caseData) {
        return caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
                && caseData.isClaimantBilingual();
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

    private void generatePIPLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        byte[] letterContent = pipLetterGenerator.downloadLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> recipients = Arrays.asList(caseData.getRespondent1().getPartyName());
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), FIRST_CONTACT_PACK_LETTER_TYPE, recipients);
    }

    private void generatePIPEmail(CaseData caseData) {
        notificationService.sendMail(
            caseData.getRespondent1().getPartyEmail(),
            notificationsProperties.getRespondentDefendantResponseForSpec(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }
}
