package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

import java.util.Map;

@Component
public class SpecDefRespClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";
    private final SpecDefRespEmailHelper specRespEmailHelper;

    public SpecDefRespClaimantEmailDTOGenerator(SpecDefRespEmailHelper specRespEmailHelper) {
        this.specRespEmailHelper = specRespEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return specRespEmailHelper.getLipTemplate(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return SpecDefRespEmailHelper.REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        return properties;
    }
}
