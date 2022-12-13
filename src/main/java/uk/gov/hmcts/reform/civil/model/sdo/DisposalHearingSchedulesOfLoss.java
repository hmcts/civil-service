package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import javax.validation.constraints.Future;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DisposalHearingSchedulesOfLoss {

    /**
     * First paragraph. Meaning depends on disposal template and ccd labels.
     *
     * @deprecated since CIV-6302
     */
    @Deprecated
    private String input1;
    /**
     * First date. Meaning depends on disposal template and ccd labels.
     *
     * @deprecated since CIV-6302
     */
    @Deprecated
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input2;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input3;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
}
