package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousingDisrepair {

    private String clauseA;
    private String clauseB;
    @Future(message = "The date entered must be in the future")
    private LocalDate firstReportDateBy;
    private String clauseCBeforeDate;
    @Future(message = "The date entered must be in the future")
    private LocalDate jointStatementDateBy;
    private String clauseCAfterDate;
    private String clauseD;
    private String clauseE;
}
