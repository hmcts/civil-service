package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@Builder(toBuilder = true)
public class CaseDataExtension implements MappableObject {

    private SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private YesOrNo applicantMPClaimExpertSpecRequired;
    private PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private PartnerAndDependentsLRspec respondent2PartnerAndDependent;
}
