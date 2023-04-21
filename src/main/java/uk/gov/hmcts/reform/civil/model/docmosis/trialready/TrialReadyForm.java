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

    private final String caseNumber;
    private final String claimant1;
    private final String claimant2;
    private final String claimantReferenceNumber;
    private final String defendant1;
    private final String defendant2;
    private final String defendantRefNumber;
    private final String defendant2RefNumber;
    private final String hearingRequirementsCheck;
    private final String hearingRequirementsText;
    private final String additionalInfo;
    private final String date;
    private final boolean trialReadyAccepted;
    private final boolean trialReadyDeclined;
    private final boolean isClaimant2;
    private final boolean isDefendant2;
    private final boolean isDefendant2RefDiff;
}
