package uk.gov.hmcts.reform.civil.model.docmosis.trialready;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
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
}
