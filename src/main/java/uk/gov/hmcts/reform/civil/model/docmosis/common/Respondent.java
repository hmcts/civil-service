package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

@Data
@Builder(toBuilder = true)
public class Respondent {

    private final String name;
    private final Address primaryAddress;
    private final Representative representative;
    private final String litigationFriendName;
}
