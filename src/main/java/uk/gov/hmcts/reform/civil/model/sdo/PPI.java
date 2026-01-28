package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import javax.validation.constraints.Future;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PPI {

    @Future(message = "The date entered must be in the future")
    private LocalDate ppiDate;
    private String text;
}
