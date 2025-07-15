package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class DefendantChangeOfAddressClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private final PinInPostConfiguration pipInPostConfiguration;

    private static final String REFERENCE_TEMPLATE = "defendant-contact-details-change-applicant-notification-%s";

    public DefendantChangeOfAddressClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties, PinInPostConfiguration pipInPostConfiguration) {
        this.notificationsProperties = notificationsProperties;
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLiPClaimantDefendantChangedContactDetails();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl());
        properties.put(EXTERNAL_ID, caseData.getCcdCaseReference().toString());
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isApplicantLiP() && NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired()) ? Boolean.TRUE : Boolean.FALSE;
    }
}
