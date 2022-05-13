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
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineRespondentForSpecNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC);
    public static final String TASK_ID_Respondent1 = "CreateClaimContinuingOnlineNotifyRespondentSolicitor1ForSpec";
    public static final String TASK_ID_Respondent2 = "CreateClaimContinuingOnlineNotifyRespondentSolicitor2ForSpec";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimContinuingOnline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1Event(callbackParams) ? TASK_ID_Respondent1 : TASK_ID_Respondent2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse notifyRespondentSolicitorForClaimContinuingOnline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime claimNotificationDate = time.now();

        final CaseData.CaseDataBuilder caseDataBuilder
            = caseData.toBuilder().claimNotificationDate(claimNotificationDate);
        String targetEmail = isRespondent1Event(callbackParams)
            || caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();

        if (null == targetEmail && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            targetEmail = caseData.getRespondentSolicitor1EmailAddress();
        }

        notificationService.sendMail(
            targetEmail,
            notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec(),
            addPropertiesWithPostCheck(caseData, callbackParams),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        if (!isRespondent1Event(callbackParams) && !YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        } else if (YesOrNo.YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .state("AWAITING_RESPONDENT_ACKNOWLEDGEMENT")
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state("AWAITING_RESPONDENT_ACKNOWLEDGEMENT")
            .build();
    }

    private Map<String, String> addPropertiesWithPostCheck(CaseData caseData, CallbackParams callbackParams) {
        Map<String, String> map = addProperties(caseData);
        if (isRespondent1Event(callbackParams)) {
            map.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()));
        } else {
            if (caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                map.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                    caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()));
            } else {
                map.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                    caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()));
            }
        }
        return map;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> map = new HashMap<>();
        map.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        map.put(CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline()
                                                                         .toLocalDate(), DATE));
        return map;
    }

    public String getRespondentLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }

    private boolean isRespondent1Event(CallbackParams callbackParams) {
        return NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC.name()
            .equals(callbackParams.getRequest().getEventId());
    }
}
