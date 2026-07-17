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
public class TaskManagementLocationTypes {

    @CCD(label = "Case conference location")
    private TaskManagementLocationsModel cmcListingLocation;
    @CCD(label = "Cost conference location")
    private TaskManagementLocationsModel ccmcListingLocation;
    @CCD(label = "Pre Trial Location")
    private TaskManagementLocationsModel ptrListingLocation;
    @CCD(label = "Trial Location")
    private TaskManagementLocationsModel trialListingLocation;

}
