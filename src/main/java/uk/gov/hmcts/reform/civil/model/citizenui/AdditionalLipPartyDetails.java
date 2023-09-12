package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalLipPartyDetails {

    private Address correspondenceAddress;
    private String contactPerson;
}
