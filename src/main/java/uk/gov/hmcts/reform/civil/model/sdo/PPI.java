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
public class PPI {

    private String label;
    private String subheading;
    private String introText;
    @Future(message = "The date entered must be in the future")
    private LocalDate ppiDate;
    private String clauses;
}
