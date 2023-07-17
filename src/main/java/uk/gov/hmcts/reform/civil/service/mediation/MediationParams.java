package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

@Data
@Builder
public class MediationParams {

    private CaseData caseData;
    private Optional<Organisation> applicantOrganisation;
    private Optional<Organisation> defendantOrganisation;
}
