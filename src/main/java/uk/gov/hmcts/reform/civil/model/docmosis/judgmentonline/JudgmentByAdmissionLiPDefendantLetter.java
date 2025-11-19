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

    private Party defendant;
    private String claimantName;
    private String claimReferenceNumber;
    private String respondToClaimUrl;
    private String pin;
    private String varyJudgmentFee;
    private String certifOfSatisfactionFee;

}
