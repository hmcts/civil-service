package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BreathingSpaceEnterNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER,
        CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER,
        CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER
    );

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::notifyRespondentSolicitor
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitor(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String templateId;
        String recipient;
        Map<String, String> templateProperties = addProperties(caseData);
        if (CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER.name()
            .equals(callbackParams.getRequest().getEventId())) {
            templateId = notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate();
            recipient = caseData.getRespondentSolicitor1EmailAddress();
            String defendantLR = getOrganisationName(caseData.getRespondent1OrganisationPolicy(), null);
            templateProperties.put("defendantLR", defendantLR);
            templateProperties.put("defendantName", caseData.getRespondent1().getPartyName());
        } else if (CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER.name()
            .equals(callbackParams.getRequest().getEventId())) {
            // TODO tbd in the future, when we include MP in BS, same template for the time being
            templateId = notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate();
            recipient = caseData.getRespondentSolicitor2EmailAddress();
            String defendantLR = getOrganisationName(caseData.getRespondent2OrganisationPolicy(), null);
            templateProperties.put("defendantLR", defendantLR);
            templateProperties.put("defendantName", caseData.getRespondent2().getPartyName());
        } else if (CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER.name()
            .equals(callbackParams.getRequest().getEventId())) {
            templateId = notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate();
            recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
            String claimantLR = getOrganisationName(
                caseData.getApplicant1OrganisationPolicy(),
                caseData.getApplicantSolicitor1ClaimStatementOfTruth()::getName
            );
            templateProperties.put("claimantLR", claimantLR);
        } else {
            throw new UnsupportedOperationException("Unexpected value "
                                                        + callbackParams.getRequest().getEventId()
                                                        + " for case event field");
        }

        notificationService.sendMail(
            recipient,
            templateId,
            templateProperties,
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getOrganisationName(OrganisationPolicy organisationPolicy, Supplier<String> defaultValue) {
        Optional<String> calculated = Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(o -> o.map(Organisation::getName));
        if (defaultValue == null) {
            return calculated.orElse(null);
        } else {
            return calculated.orElseGet(defaultValue);
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, PartyUtils.buildPartiesReferences(caseData)
        );
    }
}
