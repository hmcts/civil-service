package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsespeccasetransferoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class SpecCaseOfflineRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    private final SpecCaseOfflineHelper caseOfflineHelper;

    public SpecCaseOfflineRespSolTwoEmailDTOGenerator(SpecCaseOfflineHelper caseOfflineHelper, OrganisationService organisationService) {
        super(organisationService);
        this.caseOfflineHelper = caseOfflineHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return caseOfflineHelper.getRespTemplateForSpecClaims(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        String respSolOrgName = getLegalOrganizationNameForRespondent(caseData,
                                                                      isRespondent1, organisationService);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, respSolOrgName);
        properties.put(DEFENDANT_NAME_SPEC, respSolOrgName);
        return properties;
    }
}
