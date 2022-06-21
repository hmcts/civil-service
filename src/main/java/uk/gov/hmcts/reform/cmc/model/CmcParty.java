package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmcParty {

    private String name;
    private CmcAddress address;
    private CmcAddress correspondenceAddress;
    private String phone;
    private String pcqId;
}
