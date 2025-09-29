package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse.SpecCaseOfflineHelper.caseOfflineNotificationProperties;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class SpecCaseOfflineAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-applicant-notification-%s";

    private final SpecCaseOfflineHelper caseOfflineHelper;

    public SpecCaseOfflineAppSolOneEmailDTOGenerator(SpecCaseOfflineHelper caseOfflineHelper,
                                                     OrganisationService organisationService) {
        super(organisationService);
        this.caseOfflineHelper = caseOfflineHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return caseOfflineHelper.getApplicantTemplateForSpecClaims(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      CaseData caseData) {
        String appSolOrgName = getApplicantLegalOrganizationName(caseData, organisationService);
        properties.put(CLAIM_NAME_SPEC, appSolOrgName);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, appSolOrgName);
        properties.putAll(caseOfflineNotificationProperties(caseData));
        return properties;
    }
}
