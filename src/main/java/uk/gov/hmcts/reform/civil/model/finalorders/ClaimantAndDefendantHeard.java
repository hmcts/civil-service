package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantAndDefendantHeard {

    private FinalOrdersClaimantRepresentationList typeRepresentationClaimantList;
    private TrialNoticeProcedure trialProcedureClaimantComplex;
    private FinalOrdersDefendantRepresentationList typeRepresentationDefendantList;
    private TrialNoticeProcedure trialProcedureComplex;
    private String detailsRepresentationText;
    private String typeRepresentationClaimantOneDynamic;
    private String typeRepresentationClaimantTwoDynamic;
    private String typeRepresentationDefendantOneDynamic;
    private String typeRepresentationDefendantTwoDynamic;
}
