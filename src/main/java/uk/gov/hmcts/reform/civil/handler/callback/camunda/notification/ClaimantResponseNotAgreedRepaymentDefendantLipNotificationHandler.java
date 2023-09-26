package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseNotAgreedRepaymentDefendantLipNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT);
    private static final String REFERENCE_TEMPLATE_LIP = "claimant-reject-repayment-respondent-notification-%s";
    public static final String TASK_ID_LIP = "ClaimantDisAgreedRepaymentPlanNotifyLip";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantLip
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_LIP;
    }

    private CallbackResponse notifyDefendantLip(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String defendantEmailId = addEmail(caseData);
        if (Objects.nonNull(defendantEmailId)) {
            notificationService.sendMail(
                defendantEmailId,
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (isRespondentNotRepresented(caseData)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        } else {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData)
            );
        }
    }

    private String addEmail(CaseData caseData) {
        if (isRespondentNotRepresented(caseData)) {
            return caseData.getRespondent1().getPartyEmail();
        }
        if (isRespondentSolicitorRegistered(caseData)) {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
        return null;
    }

    private String addTemplate(CaseData caseData) {
        if (isRespondentNotRepresented(caseData)) {
            if (caseData.isRespondentResponseBilingual()) {
                return notificationsProperties.getNotifyDefendantLipWelshTemplate();
            } else {
                return notificationsProperties.getNotifyDefendantLipTemplate();
            }
        } else {
            return notificationsProperties.getNotifyDefendantLrTemplate();
        }
    }

    public boolean isRespondentNotRepresented(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getSpecRespondent1Represented());
    }

    public boolean isRespondentSolicitorRegistered(CaseData caseData) {
        return YesOrNo.YES.equals(caseData.getRespondent1OrgRegistered());
    }

    private String getRespondentLegalOrganizationName(CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(
            caseData.getRespondent1OrganisationId());
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }
}
