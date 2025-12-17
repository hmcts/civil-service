package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseLocationCivil {

    //RegionId
    private String region;

    //EpimmsId
    private String baseLocation;
}
