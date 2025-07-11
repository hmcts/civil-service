package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@Primary
public class SpecDefRespRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final SpecDefRespEmailHelper specRespEmailHelper;

    public SpecDefRespRespSolTwoEmailDTOGenerator(SpecDefRespEmailHelper specRespEmailHelper, OrganisationService organisationService) {
        super(organisationService);
        this.specRespEmailHelper = specRespEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return specRespEmailHelper.getRespondentTemplate(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return SpecDefRespEmailHelper.REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                        isRespondent1, organisationService));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
        return properties;
    }
}
