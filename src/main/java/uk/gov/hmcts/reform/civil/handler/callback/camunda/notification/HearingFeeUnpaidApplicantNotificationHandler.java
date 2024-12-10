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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class HearingFeeUnpaidApplicantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_HEARING_FEE_UNPAID);
    public static final String TASK_ID = "HearingFeeUnpaidNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE =
        "hearing-fee-unpaid-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_CLAIMANT_LIP =
        "hearing-fee-unpaid-claimantLip-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyApplicantSolicitorForHearingFeeUnpaid
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForHearingFeeUnpaid(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        boolean isApplicantLip = isApplicantLip(caseData);
        String recipient = getRecipient(caseData, isApplicantLip);

        if (nonNull(recipient)) {
            notificationService.sendMail(
                getRecipient(caseData, isApplicantLip),
                getTemplate(caseData, isApplicantLip),
                isApplicantLip ? addPropertiesApplicantLip(caseData) : addProperties(caseData),
                getReferenceTemplate(caseData, isApplicantLip)
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private Map<String, String> addPropertiesApplicantLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private boolean isApplicantLip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getApplicant1Represented()));
    }

    private String getRecipient(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    private String getTemplate(CaseData caseData, boolean isApplicantLip) {
        if (isApplicantLip) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                return notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            return notificationsProperties.getApplicantHearingFeeUnpaid();
        }
    }

    private String getReferenceTemplate(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? String.format(REFERENCE_TEMPLATE_CLAIMANT_LIP, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }
}
