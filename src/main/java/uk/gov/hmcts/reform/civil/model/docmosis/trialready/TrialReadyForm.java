package uk.gov.hmcts.reform.civil.model.docmosis.trialready;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialReadyForm implements MappableObject {

    private String caseNumber;
    private String claimant1;
    private String claimant2;
    private String claimantReferenceNumber;
    private String defendant1;
    private String defendant2;
    private String defendantRefNumber;
    private String defendant2RefNumber;
    private String hearingRequirementsCheck;
    private String hearingRequirementsText;
    private String additionalInfo;
    private String date;
    private boolean trialReadyAccepted;
    private boolean trialReadyDeclined;
    private boolean isClaimant2;
    private boolean isDefendant2;
    private boolean isDefendant2RefDiff;

    public TrialReadyForm setIsClaimant2(boolean isClaimant2) {
        this.isClaimant2 = isClaimant2;
        return this;
    }

    public TrialReadyForm setIsDefendant2(boolean isDefendant2) {
        this.isDefendant2 = isDefendant2;
        return this;
    }

    public TrialReadyForm setIsDefendant2RefDiff(boolean isDefendant2RefDiff) {
        this.isDefendant2RefDiff = isDefendant2RefDiff;
        return this;
    }
}
