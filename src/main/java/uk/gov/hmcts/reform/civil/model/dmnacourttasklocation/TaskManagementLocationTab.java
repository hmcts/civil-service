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
public class TaskManagementLocationTab {

    @CCD(label = "Case management location")
    private String caseManagementLocation;
    @CCD(label = "Case management conference listings")
    private String cmcListingLocation;
    @CCD(label = "Cost and case management conference listings")
    private String ccmcListingLocation;
    @CCD(label = "Pre-trial review listings")
    private String ptrListingLocation;
    @CCD(label = "Trial listings")
    private String trialListingLocation;

}
