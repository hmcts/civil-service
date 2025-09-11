package uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class JudgementCoverLetter implements MappableObject {

    private final String claimNumber;
    private final String partyName;
    private final Address address;

}
