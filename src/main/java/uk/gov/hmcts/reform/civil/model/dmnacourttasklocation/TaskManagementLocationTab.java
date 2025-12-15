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
public class TaskManagementLocationTab {

    private String caseManagementLocation;
    private String cmcListingLocation;
    private String ccmcListingLocation;
    private String ptrListingLocation;
    private String trialListingLocation;

}
