package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrialHousingDisrepair {

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

    private String input1;
    private String input2;
    private String input3;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
}
