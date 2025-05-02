package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.spec.offline.counterclaimordivergedresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class SpecCaseOfflineClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-applicant-notification-%s";

    private final SpecCaseOfflineHelper caseOfflineHelper;

    public SpecCaseOfflineClaimantEmailDTOGenerator(SpecCaseOfflineHelper caseOfflineHelper) {
        this.caseOfflineHelper = caseOfflineHelper;
    }

    //Case of Lip v LR
    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return caseOfflineHelper.getClaimantTemplateForLipVLRSpecClaims(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        return properties;
    }
}
