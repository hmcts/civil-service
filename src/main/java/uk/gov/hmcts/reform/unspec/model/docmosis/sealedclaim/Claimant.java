package uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.Address;

@Data
@Builder(toBuilder = true)
public class Claimant {

    private final String name;
    private final Address primaryAddress;
}
