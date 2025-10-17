package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseLocationCivil {

    //RegionId
    private String region;

    //EpimmsId
    private String baseLocation;
    private String siteName;
    private String address;
    private String postcode;
}
