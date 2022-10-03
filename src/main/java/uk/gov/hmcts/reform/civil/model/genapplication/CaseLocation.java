package uk.gov.hmcts.reform.civil.model.genapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseLocation {

    private String region;

    private String baseLocation;
}
