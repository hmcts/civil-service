package uk.gov.hmcts.reform.civil.model.defaultjudgment;

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
public class SdoDJR2TrialCreditHire {

    private String input1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    private String input6;
    private String input7;
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
    private String input8;
    private List<AddOrRemoveToggle> detailsShowToggle;
    private SdoDJR2TrialCreditHireDetails sdoDJR2TrialCreditHireDetails;
}
