package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class SpecDefRespAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final SpecDefRespEmailHelper specRespEmailHelper;

    public SpecDefRespAppSolOneEmailDTOGenerator(SpecDefRespEmailHelper specRespEmailHelper, OrganisationService organisationService) {
        super(organisationService);
        this.specRespEmailHelper = specRespEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return specRespEmailHelper.getAppSolTemplate(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return SpecDefRespEmailHelper.REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec()))
        ) {
            String shouldBePaidBy = caseData.getRespondToClaimAdmitPartLRspec()
                .getWhenWillThisAmountBePaid().getDayOfMonth()
                + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getMonth()
                + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getYear();
            properties.put(WHEN_WILL_BE_PAID_IMMEDIATELY, shouldBePaidBy);
        } else {
            properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        }
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }
}
