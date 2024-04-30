package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;

import javax.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2FastTrackCreditHire {

    private String input1;
    private String input5;
    private String input6;
    private String input7;
    private String input8;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
    private List<AddOrRemoveToggle> detailsShowToggle;
    private SdoR2FastTrackCreditHireDetails sdoR2FastTrackCreditHireDetails;
}
