package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class JudgmentByAdmissionLiPDefendantLetter implements MappableObject {

    private final Party defendant;
    private final String claimantName;
    private final String claimReferenceNumber;
    private final String respondToClaimUrl;
    private final String pin;
    private final String varyJudgmentFee;
    private final String certifOfSatisfactionFee;

}
