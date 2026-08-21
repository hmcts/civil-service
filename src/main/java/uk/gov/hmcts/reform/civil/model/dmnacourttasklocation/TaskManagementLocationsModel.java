package uk.gov.hmcts.reform.civil.model.dmnacourttasklocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskManagementLocationsModel {

    @CCD(label = "Region")
    private String region;
    @CCD(label = "Region Name")
    private String regionName;
    @CCD(label = "Location")
    private String location;
    @CCD(label = "Location Name")
    private String locationName;

}
