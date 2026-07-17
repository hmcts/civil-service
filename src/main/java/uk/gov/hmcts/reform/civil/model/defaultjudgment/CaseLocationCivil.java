package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseLocationCivil {

    //RegionId
    @CCD(
            label = "Region",
            showCondition = "baseLocation = \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            retainHiddenValue = true
    )
    private String region;

    //EpimmsId
    @CCD(
            label = "Base location",
            showCondition = "baseLocation = \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            retainHiddenValue = true
    )
    private String baseLocation;
}
