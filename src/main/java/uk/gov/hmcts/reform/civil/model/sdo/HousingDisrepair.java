package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HousingDisrepair {

    private String clauseA;
    private String clauseB;
    private LocalDate firstReportDateBy;
    private String clauseCBeforeDate;
    private LocalDate jointStatementDateBy;
    private String clauseCAfterDate;
    private String clauseD;
    private String clauseE;

    private String input1;
    private String input2;
    private String input3;
    private LocalDate date1;
    private String input4;
    private LocalDate date2;
}
