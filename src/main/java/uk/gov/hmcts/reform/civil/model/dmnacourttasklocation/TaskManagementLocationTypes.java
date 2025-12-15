package uk.gov.hmcts.reform.civil.model.dmnacourttasklocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskManagementLocationTypes {

    private TaskManagementLocationsModel cmcListingLocation;
    private TaskManagementLocationsModel ccmcListingLocation;
    private TaskManagementLocationsModel ptrListingLocation;
    private TaskManagementLocationsModel trialListingLocation;

}
