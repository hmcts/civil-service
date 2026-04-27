package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class RequestJudgementByAdmissionApplicantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "request-judgement-by-admission-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pipInPostConfiguration;

    protected RequestJudgementByAdmissionApplicantEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        PinInPostConfiguration pipInPostConfiguration) {
        this.notificationsProperties = notificationsProperties;
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyApplicantLipRequestJudgementByAdmissionNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLipOneVOne();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl());
        return properties;
    }
}
