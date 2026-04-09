package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgementCoverLetter implements MappableObject {

    private String claimNumber;
    private String partyName;
    private Address address;

}
