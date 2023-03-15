package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

@Service
@RequiredArgsConstructor
public class AgreedExtensionDateApplicantForSpecNotificationHandler
    extends CallbackHandler implements NotificationData {

    private static final Map<CaseEvent, String> EVENT_TASK_ID_MAP = Map.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC,
        "AgreedExtensionDateNotifyApplicantSolicitor1ForSpec",
        NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC,
        "AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec",
        NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC,
        "AgreedExtensionDateNotifyRespondentSolicitor2CCForSpec"
    );

    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-spec-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForAgreedExtensionDateForSpec
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return EVENT_TASK_ID_MAP.get(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return new ArrayList<>(EVENT_TASK_ID_MAP.keySet());
    }

    private CallbackResponse notifyApplicantSolicitorForAgreedExtensionDateForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC.name())) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else {
            notificationService.sendMail(
                getSolicitorEmailAddress(callbackParams),
                notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec(),
                addPropertiesForRespondent(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getApplicant1OrganisationPolicy()
                    .getOrganisation().getOrganisationID(),
                caseData
            ),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            AGREED_EXTENSION_DATE, formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE),
            DEFENDANT_NAME, fetchDefendantName(caseData)
        );
    }

    public Map<String, String> addPropertiesForRespondent(CaseData caseData) {
        var extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();

        //finding extension date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if ((multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) || (multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP)) {
            if ((caseData.getRespondentSolicitor1AgreedDeadlineExtension() == null)
                && (caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null)) {
                extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
            } else if ((caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null)
                && (caseData.getRespondentSolicitor2AgreedDeadlineExtension() == null)) {
                extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
            } else if ((caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null)
                && (caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null)) {
                if (caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                    .isAfter(caseData.getRespondentSolicitor1AgreedDeadlineExtension())) {
                    extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                } else {
                    extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
                }
            }
        }

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID(), caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE)
        );
    }

    public String getApplicantLegalOrganizationName(String id, CaseData caseData) {

        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private String getSolicitorEmailAddress(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        CaseData caseData = callbackParams.getCaseData();

        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC.name())) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        if (eventId.equals(NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC.name())) {
            return caseData.getRespondentSolicitor2EmailAddress();
        }

        throw new CallbackException(String.format("Callback handler received unexpected event id: %s", eventId));
    }
}
