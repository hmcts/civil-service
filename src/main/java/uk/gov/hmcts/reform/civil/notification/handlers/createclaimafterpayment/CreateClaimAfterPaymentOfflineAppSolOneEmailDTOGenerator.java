package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    public CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            return caseData.getApplicant1Email();
        }
        return super.getEmailAddress(caseData);
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            return caseData.isClaimantBilingual()
                    ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()
                    : notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        }
        return notificationsProperties.getSolicitorCaseTakenOffline();
    }

    @Override
    protected String getReferenceTemplate() {
        return "case-proceeds-in-caseman-applicant-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                    CLAIMANT_NAME,         caseData.getApplicant1().getPartyName()
            );
        }
        return super.addProperties(caseData);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            return properties;
        }
        return super.addCustomProperties(properties, caseData);
    }
}
