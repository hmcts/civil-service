package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseLocationCivil {

    private String region;
    private String siteName;
    private String baseLocation;
    private String address;
    private String postcode;
}
