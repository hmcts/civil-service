package uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.Address;

@Data
@Builder(toBuilder = true)
public class Representative {

    private final String contactName;
    private final String organisationName;
    private final String phoneNumber;
    private final String dxAddress;
    private final String emailAddress;
    private final Address serviceAddress;
}
