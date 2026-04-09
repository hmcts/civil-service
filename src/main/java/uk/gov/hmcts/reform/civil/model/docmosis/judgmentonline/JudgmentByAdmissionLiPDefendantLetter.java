package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgmentByAdmissionLiPDefendantLetter implements MappableObject {

    private Party defendant;
    private String claimantName;
    private String claimReferenceNumber;
    private String respondToClaimUrl;
    private String pin;
    private String varyJudgmentFee;
    private String certifOfSatisfactionFee;

}
