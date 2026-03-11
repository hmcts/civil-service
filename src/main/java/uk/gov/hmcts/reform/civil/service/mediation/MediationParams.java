package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

@Data
public class MediationParams {

    private CaseData caseData;
    private Optional<Organisation> applicantOrganisation;
    private Optional<Organisation> defendantOrganisation;
}
